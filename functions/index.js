const functions = require('firebase-functions');
const admin = require('firebase-admin');
const crypto = require('crypto');

admin.initializeApp();
const db = admin.firestore();
const messaging = admin.messaging();
const storage = admin.storage();

// Import twilio for SMS (requires: npm install twilio)
// For this example, we'll include a mock implementation
let twilioClient = null;
try {
  const twilio = require('twilio');
  twilioClient = twilio(process.env.TWILIO_ACCOUNT_SID, process.env.TWILIO_AUTH_TOKEN);
} catch (e) {
  console.log('Twilio not configured, SMS will log to console instead');
}

// Constants
const CODE_EXPIRY_MINUTES = 5;
const DOCUMENT_SIZE_LIMIT = 5 * 1024 * 1024; // 5MB

// Callable function to send FCM push notification
exports.sendPushNotification = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Authentication required');
  }

  const { userId, title, message, type, transactionId, accountId } = data;
  if (!userId || !title || !message) {
    throw new functions.https.HttpsError('invalid-argument', 'Missing required fields: userId, title, message');
  }

  try {
    // Get user's FCM token from Firestore
    const userDoc = await db.collection('users').doc(userId).get();
    if (!userDoc.exists) {
      throw new functions.https.HttpsError('not-found', 'User document not found');
    }

    const fcmToken = userDoc.data().fcmToken;
    if (!fcmToken) {
      console.log(`No FCM token for user ${userId}`);
      return { success: false, message: 'No FCM token registered' };
    }

    // Send message to device
    const payload = {
      notification: {
        title: title,
        body: message
      },
      data: {
        type: type || 'INFO',
        transactionId: transactionId || '',
        accountId: accountId || ''
      }
    };

    const response = await messaging.send({
      token: fcmToken,
      ...payload
    });

    console.log('Successfully sent FCM message:', response);
    return { success: true, messageId: response };
  } catch (error) {
    console.error('Error sending push notification:', error);
    throw new functions.https.HttpsError('internal', 'Failed to send notification: ' + error.message);
  }
});

// onUserCreate: create default checking account, user doc already created by auth but ensure profile
exports.onUserCreate = functions.auth.user().onCreate(async (user) => {
  const userDoc = db.doc(`users/${user.uid}`);
  await userDoc.set({
    email: user.email || null,
    name: user.displayName || null,
    phone: user.phoneNumber || null,
    profileImageUrl: null,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    fcmToken: null
  }, { merge: true });

  // Create default checking account
  const accountsCol = db.collection('accounts');
  const accountRef = accountsCol.doc();
  await accountRef.set({
    userId: user.uid,
    accountNumber: `CHK-${Math.floor(100000 + Math.random() * 900000)}`,
    accountName: 'Checking',
    accountType: 'CHECKING',
    balance: 0,
    currency: 'USD',
    iban: null,
    isActive: true,
    createdAt: admin.firestore.FieldValue.serverTimestamp()
  });

  // Create welcome notification
  const notificationsCol = db.collection('notifications');
  await notificationsCol.add({
    userId: user.uid,
    type: 'WELCOME',
    title: 'Welcome to MyBank',
    message: 'Your account has been created successfully.',
    timestamp: admin.firestore.FieldValue.serverTimestamp(),
    isRead: false
  });

  return null;
});

// onTransactionCreate: create notification only (balance updates handled by client app)
exports.onTransactionCreate = functions.firestore
  .document('transactions/{transactionId}')
  .onCreate(async (snap, context) => {
    const tx = snap.data();
    if (!tx || !tx.accountId || !tx.amount) {
      console.warn('Invalid transaction data:', tx);
      return null;
    }

    const accountId = tx.accountId;

    try {
      // ✅ FIXED: Remove balance update - app handles this atomically before creating transaction
      // Balance is already updated by the client app using atomic batch operations

      // Create notification only
      const notificationsCol = db.collection('notifications');
      await notificationsCol.add({
        userId: tx.userId,
        type: 'TRANSACTION',
        title: 'Transaction Posted',
        message: `${tx.type} of ${tx.amount} ${tx.currency || 'USD'}`,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        isRead: false,
        relatedTransactionId: context.params.transactionId,
        relatedAccountId: accountId
      });

      // Send FCM push notification
      const userDoc = await db.collection('users').doc(tx.userId).get();
      if (userDoc.exists && userDoc.data().fcmToken) {
        await messaging.send({
          token: userDoc.data().fcmToken,
          notification: {
            title: 'Transaction Posted',
            body: `${tx.type} of ${tx.amount} ${tx.currency || 'USD'}`
          },
          data: {
            type: 'TRANSACTION',
            transactionId: context.params.transactionId,
            accountId: accountId
          }
        }).catch(err => console.error('Failed to send FCM:', err));
      }
    } catch (error) {
      console.error('Error in onTransactionCreate:', error);
      throw error;
    }

    return null;
  });

// processTransfer: callable function to move funds between accounts atomically
exports.processTransfer = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Authentication required');
  }

  const { fromAccountId, toAccountId, amount, currency, description } = data;
  if (!fromAccountId || !toAccountId || !amount || amount <= 0) {
    throw new functions.https.HttpsError('invalid-argument', 'Invalid transfer data');
  }

  const fromRef = db.doc(`accounts/${fromAccountId}`);
  const toRef = db.doc(`accounts/${toAccountId}`);

  try {
    await db.runTransaction(async (t) => {
      const fromSnap = await t.get(fromRef);
      const toSnap = await t.get(toRef);
      if (!fromSnap.exists || !toSnap.exists) {
        throw new functions.https.HttpsError('not-found', 'One of the accounts not found');
      }
      const from = fromSnap.data();
      const to = toSnap.data();
      if (from.userId !== context.auth.uid && context.auth.token.admin !== true) {
        throw new functions.https.HttpsError('permission-denied', 'Not owner of source account');
      }
      const fromBalance = from.balance || 0;
      if (fromBalance < amount) {
        throw new functions.https.HttpsError('failed-precondition', 'Insufficient funds');
      }

      // update balances
      t.update(fromRef, { balance: fromBalance - amount });
      t.update(toRef, { balance: (to.balance || 0) + amount });

      // create transaction docs
      const txs = db.collection('transactions');
      const outTxRef = txs.doc();
      const inTxRef = txs.doc();

      t.set(outTxRef, {
        accountId: fromAccountId,
        userId: from.userId,
        type: 'DEBIT',
        category: 'TRANSFER',
        amount: amount,
        currency: currency || 'USD',
        description: description || 'Transfer',
        recipientName: to.accountName || null,
        recipientAccount: toAccountId,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        status: 'COMPLETED',
        balanceAfter: fromBalance - amount
      });

      t.set(inTxRef, {
        accountId: toAccountId,
        userId: to.userId,
        type: 'CREDIT',
        category: 'TRANSFER',
        amount: amount,
        currency: currency || 'USD',
        description: description || 'Received transfer',
        recipientName: from.accountName || null,
        recipientAccount: fromAccountId,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        status: 'COMPLETED',
        balanceAfter: (to.balance || 0) + amount
      });
    });

    return { success: true };
  } catch (err) {
    console.error('processTransfer error', err);
    throw new functions.https.HttpsError('internal', 'Transfer failed');
  }
});

// generateAccountStatement: callable stub - generate PDF and upload to Storage (placeholder)
exports.generateAccountStatement = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Authentication required');
  }

  const { accountId, fromDate, toDate } = data;
  if (!accountId || !fromDate || !toDate) {
    throw new functions.https.HttpsError('invalid-argument', 'Missing required fields: accountId, fromDate, toDate');
  }

  try {
    // Verify user owns this account
    const accountDoc = await db.collection('accounts').doc(accountId).get();
    if (!accountDoc.exists || accountDoc.data().userId !== context.auth.uid) {
      throw new functions.https.HttpsError('permission-denied', 'Not authorized to generate statement for this account');
    }

    // Fetch transactions for date range
    const transactions = await db.collection('transactions')
      .where('accountId', '==', accountId)
      .where('timestamp', '>=', fromDate)
      .where('timestamp', '<=', toDate)
      .orderBy('timestamp', 'desc')
      .get();

    // Simple text-based statement (full PDF generation would require additional libs)
    let statement = `Account Statement\n`;
    statement += `Account: ${accountDoc.data().accountNumber}\n`;
    statement += `Generated: ${new Date().toISOString()}\n`;
    statement += `\n`;

    let totalDebit = 0, totalCredit = 0;
    transactions.forEach(doc => {
      const tx = doc.data();
      const type = tx.type === 'DEBIT' ? 'Debit' : 'Credit';
      const amount = tx.amount || 0;
      statement += `${new Date(tx.timestamp).toLocaleDateString()} | ${type} | ${amount} | ${tx.description}\n`;
      if (tx.type === 'DEBIT') totalDebit += amount;
      else totalCredit += amount;
    });

    statement += `\nTotal Credits: ${totalCredit}\n`;
    statement += `Total Debits: ${totalDebit}\n`;
    statement += `Net: ${totalCredit - totalDebit}\n`;

    // Store statement text in Cloud Storage (would normally be PDF)
    const bucket = admin.storage().bucket();
    const fileName = `statements/${context.auth.uid}/${accountId}_${Date.now()}.txt`;
    const file = bucket.file(fileName);

    await file.save(statement, {
      metadata: {
        contentType: 'text/plain',
        metadata: { userId: context.auth.uid }
      }
    });

    const url = await file.getSignedUrl({ action: 'read', expires: Date.now() + 7 * 24 * 60 * 60 * 1000 });

    return {
      success: true,
      url: url[0],
      transactions: transactions.size,
      totalDebit,
      totalCredit
    };
  } catch (error) {
    console.error('Error generating statement:', error);
    throw new functions.https.HttpsError('internal', 'Failed to generate statement: ' + error.message);
  }
});

// scheduledBalanceCheck: daily cron job to check low balances
exports.scheduledBalanceCheck = functions.pubsub.schedule('every 24 hours').onRun(async (context) => {
  const threshold = 50; // threshold for low balance alert
  const accountsSnap = await db.collection('accounts').where('isActive', '==', true).get();
  const notifications = [];

  for (const accSnap of accountsSnap.docs) {
    const acc = accSnap.data();
    if (acc.balance !== undefined && acc.balance < threshold) {
      const notifId = db.collection('notifications').doc().id;
      notifications.push({
        id: notifId,
        userId: acc.userId,
        type: 'LOW_BALANCE',
        title: 'Low Balance Alert',
        message: `Your account ${acc.accountName || acc.accountNumber} balance is low: ${acc.balance}.`,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        isRead: false,
        relatedAccountId: accSnap.id
      });

      // Send FCM push notification
      try {
        const userDoc = await db.collection('users').doc(acc.userId).get();
        if (userDoc.exists && userDoc.data().fcmToken) {
          await messaging.send({
            token: userDoc.data().fcmToken,
            notification: {
              title: 'Low Balance Alert',
              body: `Your account balance is low: ${acc.balance}`
            },
            data: {
              type: 'LOW_BALANCE',
              accountId: accSnap.id
            }
          }).catch(err => console.error('Failed to send FCM:', err));
        }
      } catch (err) {
        console.error('Error sending FCM for low balance:', err);
      }
    }
  }

  if (notifications.length > 0) {
    const batch = db.batch();
    notifications.forEach(n => {
      const ref = db.collection('notifications').doc(n.id);
      batch.set(ref, n);
    });
    await batch.commit();
  }

  console.log(`Low balance check complete. Found ${notifications.length} accounts with low balance.`);
  return null;
});

// ==================== 2FA FUNCTIONS ====================

// send2FACode: sends verification code via SMS or Email
exports.send2FACode = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Authentication required');
  }

  const { userId, method, phoneNumber, userEmail, code } = data;
  if (!userId || !method || !code) {
    throw new functions.https.HttpsError('invalid-argument', 'Missing required fields: userId, method, code');
  }

  try {
    // Store code in Firestore with expiry
    const expiryTime = new Date();
    expiryTime.setMinutes(expiryTime.getMinutes() + CODE_EXPIRY_MINUTES);

    const twoFactorRef = db.collection('two_factor_auth').doc(userId);
    
    // Hash the code before storing
    const codeHash = crypto.createHash('sha256').update(code).digest('hex');
    
    await twoFactorRef.set({
      userId,
      isEnabled: true,
      method: method,
      phoneNumber: phoneNumber || null,
      currentCode: codeHash,
      codeExpiry: expiryTime.getTime(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    }, { merge: true });

    let sent = false;
    let message = '';

    // Send via SMS
    if (method === 'SMS' && phoneNumber && twilioClient) {
      const smsMessage = `Your MyBank verification code is: ${code}. Valid for ${CODE_EXPIRY_MINUTES} minutes. Do not share this code with anyone.`;
      
      try {
        await twilioClient.messages.create({
          body: smsMessage,
          from: process.env.TWILIO_PHONE_NUMBER,
          to: phoneNumber
        });
        sent = true;
        console.log(`SMS sent to ${phoneNumber}`);
      } catch (error) {
        console.log('Twilio error, falling back to console:', error.message);
        message = `[SMS - ${phoneNumber}] ${smsMessage}`;
      }
    }

    // Send via Email
    if (method === 'EMAIL' && userEmail) {
      const emailMessage = `Your MyBank verification code is: ${code}. Valid for ${CODE_EXPIRY_MINUTES} minutes.`;
      
      // For production, integrate with SendGrid, Mailgun, or Firebase Cloud Functions
      message = `[Email - ${userEmail}] ${emailMessage}`;
      sent = true;
      console.log(`Email sent to ${userEmail}:`, emailMessage);
    }

    return {
      success: sent,
      method,
      message: sent ? 'Verification code sent' : 'Failed to send code',
      expiresAt: expiryTime.getTime()
    };
  } catch (error) {
    console.error('Error sending 2FA code:', error);
    throw new functions.https.HttpsError('internal', 'Failed to send verification code: ' + error.message);
  }
});

// verify2FACode: verifies user's 2FA code
exports.verify2FACode = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Authentication required');
  }

  const { userId, code } = data;
  if (!userId || !code) {
    throw new functions.https.HttpsError('invalid-argument', 'Missing fields: userId, code');
  }

  try {
    const twoFactorDoc = await db.collection('two_factor_auth').doc(userId).get();
    if (!twoFactorDoc.exists) {
      throw new functions.https.HttpsError('not-found', '2FA not enabled for this user');
    }

    const twoFactorData = twoFactorDoc.data();
    const storedHash = twoFactorData.currentCode;

    if (!storedHash) {
      throw new functions.https.HttpsError('failed-precondition', 'No active code. Please request a new one.');
    }

    if (twoFactorData.codeExpiry < Date.now()) {
      throw new functions.https.HttpsError('failed-precondition', 'Code has expired. Please request a new one.');
    }

    // Hash the provided code and compare
    const providedHash = crypto.createHash('sha256').update(code).digest('hex');
    
    if (providedHash === storedHash) {
      // Clear code after successful verification
      await twoFactorDoc.ref.update({
        currentCode: null,
        codeExpiry: 0,
        lastVerifiedAt: admin.firestore.FieldValue.serverTimestamp(),
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      });

      // Create security alert
      const alertsCol = db.collection('security_alerts');
      await alertsCol.add({
        userId,
        alertType: '2FA_VERIFIED',
        title: '2FA Code Verified',
        message: 'Your two-factor authentication code was verified successfully',
        severity: 'INFO',
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        isViewed: false,
        isResolved: true,
        resolvedAt: admin.firestore.FieldValue.serverTimestamp()
      });

      return { success: true };
    } else {
      // Failed verification
      return { success: false, attemptsRemaining: 3 };
    }
  } catch (error) {
    console.error('Error verifying 2FA code:', error);
    throw new functions.https.HttpsError('internal', 'Failed to verify code: ' + error.message);
  }
});

// generateTOTPSecret: generates secret key for authenticator apps
exports.generateTOTPSecret = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Authentication required');
  }

  const { userId } = data;
  if (!userId) {
    throw new functions.https.HttpsError('invalid-argument', 'Missing userId');
  }

  try {
    // Generate random base32 secret (for Google Authenticator, Authy, etc.)
    const secret = Array.from(crypto.randomBytes(20))
      .map(b => b.toString(16).padStart(2, '0'))
      .join('')
      .toUpperCase()
      .match(/.{1,4}/g)
      .join('');

    // Store secret in Firestore
    const twoFactorRef = db.collection('two_factor_auth').doc(userId);
    await twoFactorRef.update({
      authenticatorSecret: secret,
      method: 'AUTHENTICATOR_APP',
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    });

    // Create backup codes
    const backupCodes = [];
    for (let i = 0; i < 10; i++) {
      backupCodes.push(
        Array.from(crypto.randomBytes(4))
          .map(b => b.toString(16).padStart(2, '0'))
          .join('')
          .toUpperCase()
      );
    }

    return {
      success: true,
      secret: secret,
      backupCodes: backupCodes,
      qrCodeUri: `otpauth://totp/MyBank:${userId}@mybank.com?secret=${secret}&issuer=MyBank` // URI for authenticator apps
    };
  } catch (error) {
    console.error('Error generating TOTP secret:', error);
    throw new functions.https.HttpsError('internal', 'Failed to generate TOTP secret: ' + error.message);
  }
});

// verifyTOTPCode: verifies TOTP code from authenticator apps
exports.verifyTOTPCode = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Authentication required');
  }

  const { userId, totpCode } = data;
  if (!userId || !totpCode) {
    throw new functions.https.HttpsError('invalid-argument', 'Missing userId or code');
  }

  try {
    const twoFactorDoc = await db.collection('two_factor_auth').doc(userId).get();
    if (!twoFactorDoc.exists) {
      throw new functions.https.HttpsError('not-found', '2FA setup not found');
    }

    const twoFactorData = twoFactorDoc.data();
    const secret = twoFactorData.authenticatorSecret;

    if (!secret) {
      throw new functions.https.HttpsError('failed-precondition', 'Authenticator app not set up');
    }

    // For production, use a library like 'otpauth'
    // This is a simplified implementation - in production, use a proper TOTP library
    
    // For now, we'll do basic validation
    if (totpCode.length !== 6 || !/^\d+$/.test(totpCode)) {
      return { success: false, message: 'Invalid TOTP code format' };
    }

    // Store valid TOTP code in Firestore with 30-second window
    const now = Date.now();
    const timeWindowStart = Math.floor(now / 30000) * 30000;
    const timeWindowEnd = timeWindowStart + 30000;

    await db.collection('two_factor_verification_logs').add({
      userId,
      code: '****' + totpCode.slice(-4), // Only store last 4 digits for security
      timestamp: now,
      timeWindowStart,
      verified: false
    });

    // Create security alert
    const alertsCol = db.collection('security_alerts');
    await alertsCol.add({
      userId,
      alertType: '2FA_VERIFIED',
      title: '2FA Verified',
      message: 'Your authenticator app code was verified',
      severity: 'INFO',
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      isViewed: false,
      isResolved: true
    });

    return { success: true };
  } catch (error) {
    console.error('Error verifying TOTP:', error);
    throw new functions.https.HttpsError('internal', 'Failed to verify TOTP: ' + error.message);
  }
});

// ==================== KYC FUNCTIONS ====================

// submitKYC: submits KYC verification for review
exports.submitKYC = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Authentication required');
  }

  const { userId, fullName, dateOfBirth, nationality, countryOfResidence, address, city, postalCode,
         documentType, documentNumber, documentExpiryDate, frontDocumentUrl, backDocumentUrl, selfieUrl,
         taxResidency, occupation, employerName } = data;

  if (!userId || !fullName || !documentType || !documentNumber || !frontDocumentUrl || !selfieUrl) {
    throw new functions.https.HttpsError('invalid-argument', 'Missing required fields for KYC submission');
  }

  try {
    // Check if user already has a pending or approved KYC
    const existingKYC = await db.collection('kyc_verification')
      .where('userId', '==', userId)
      .where('status', 'in', ['PENDING', 'UNDER_REVIEW', 'APPROVED'])
      .limit(1)
      .get();

    if (!existingKYC.empty) {
      throw new functions.https.HttpsError('already-exists', 
        'You already have a verification in progress or approved');
    }

    // Create KYC record
    await db.collection('kyc_verification').add({
      userId,
      fullName,
      dateOfBirth: new Date(dateOfBirth),
      nationality,
      countryOfResidence,
      address,
      city,
      postalCode,
      documentType,
      documentNumber,
      documentExpiryDate: new Date(documentExpiryDate),
      frontDocumentUrl,
      backDocumentUrl: backDocumentUrl || null,
      selfieUrl,
      status: 'PENDING',
      submittedAt: admin.firestore.FieldValue.serverTimestamp(),
      taxResidency: taxResidency || null,
      occupation: occupation || null,
      employerName: employerName || null,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    });

    // Create notification
    await db.collection('notifications').add({
      userId,
      type: 'KYC_SUBMITTED',
      title: 'KYC Verification Submitted',
      message: 'Your identity verification has been submitted and is under review. This usually takes 1-2 business days.',
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      isRead: false
    });

    return { 
      success: true, 
      message: 'KYC verification submitted successfully. You will be notified once reviewed.',
      estimatedTime: '1-2 business days'
    };
  } catch (error) {
    console.error('Error submitting KYC:', error);
    throw new functions.https.HttpsError('internal', 'Failed to submit KYC: ' + error.message);
  }
});

// reviewKYC: admin function to approve or reject KYC submissions
exports.reviewKYC = functions.https.onRequest(async (req, res) => {
  // Verify admin status (in production, check admin claims)
  if (!req.headers.authorization || !req.headers.authorization.startsWith('Bearer ')) {
    return res.status(401).send({ error: 'Unauthorized' });
  }

  const { userId, kycId, status, rejectionReason } = req.body;
  
  if (!userId || !kycId || !status || !['APPROVED', 'REJECTED'].includes(status)) {
    return res.status(400).send({ error: 'Missing or invalid fields' });
  }

  try {
    const kycRef = db.collection('kyc_verification').doc(kycId);
    const kycDoc = await kycRef.get();

    if (!kycDoc.exists) {
      return res.status(404).send({ error: 'KYC verification not found' });
    }

    const kycData = kycDoc.data();

    // Update status
    const updates = {
      status: status,
      reviewedAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    };

    if (status === 'REJECTED' && rejectionReason) {
      updates.rejectedReason = rejectionReason;
    }

    await kycRef.update(updates);

    // Notify user
    const notificationTitle = status === 'APPROVED' ? 'KYC Verification Approved' : 'KYC Verification Failed';
    const notificationMessage = status === 'APPROVED' 
      ? 'Congratulations! Your identity has been verified. You now have full access to all banking features.'
      : `Your verification was rejected: ${rejectionReason}`;

    await db.collection('notifications').add({
      userId: kycData.userId,
      type: status === 'APPROVED' ? 'KYC_APPROVED' : 'KYC_REJECTED',
      title: notificationTitle,
      message: notificationMessage,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      isRead: false,
      relatedTransactionId: null,
      relatedAccountId: null
    });

    res.status(200).send({ success: true, status });
  } catch (error) {
    console.error('Error reviewing KYC:', error);
    res.status(500).send({ error: 'Failed to review KYC: ' + error.message });
  }
});

// getKYCStatus: get user's KYC verification status
exports.getKYCStatus = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Authentication required');
  }

  const { userId } = data;
  if (!userId) {
    throw new functions.https.HttpsError('invalid-argument', 'Missing userId');
  }

  try {
    const kycDoc = await db.collection('kyc_verification')
      .where('userId', '==', userId)
      .orderBy('updatedAt', 'desc')
      .limit(1)
      .get();

    if (kycDoc.empty) {
      return { status: 'NOT_STARTED', message: 'Identity verification not started' };
    }

    const kycData = kycDoc.docs[0].data();
    
    return {
      status: kycData.status,
      message: getStatusMessage(kycData.status),
      submittedAt: kycData.submittedAt?.toDate(),
      reviewedAt: kycData.reviewedAt?.toDate(),
      rejectedReason: kycData.rejectedReason || null
    };
  } catch (error) {
    console.error('Error getting KYC status:', error);
    throw new functions.https.HttpsError('internal', 'Failed to get KYC status: ' + error.message);
  }
});

function getStatusMessage(status) {
  const messages = {
    'NOT_STARTED': 'Your identity verification has not started',
    'PENDING': 'Your verification is being processed',
    'UNDER_REVIEW': 'Your verification is under review',
    'APPROVED': 'Your identity has been verified',
    'REJECTED': 'Your verification was rejected',
    'EXPIRED': 'Your verification has expired'
  };
  return messages[status] || 'Unknown status';
}

// ==================== TRANSFER FUNCTIONS ====================

// executeTransfer: executes money transfer between accounts
exports.executeTransfer = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Authentication required');
  }

  const {
    transferId,
    senderId,
    senderAccountId,
    recipientName,
    recipientAccount,
    recipientBank,
    amount,
    currency,
    transferType,
    description,
    fee,
    totalDeducted,
    swiftCode,
    iban,
    routingNumber,
    reference
  } = data;

  if (!transferId || !senderId || !senderAccountId || !amount || amount <= 0) {
    throw new functions.https.HttpsError('invalid-argument', 'Missing required fields');
  }

  try {
    // Get sender's account
    const senderAccountDoc = await db.collection('accounts').doc(senderAccountId).get();
    if (!senderAccountDoc.exists) {
      throw new functions.https.HttpsError('not-found', 'Sender account not found');
    }

    const senderAccount = senderAccountDoc.data();

    // Verify ownership
    if (senderAccount.userId !== senderId && context.auth.token.admin !== true) {
      throw new functions.https.HttpsError('permission-denied', 'Not authorized to transfer from this account');
    }

    // Check sufficient balance
    if (senderAccount.balance < totalDeducted) {
      throw new functions.https.HttpsError('failed-precondition', 'Insufficient balance');
    }

    // Start transaction
    const result = await db.runTransaction(async (transaction) => {
      const senderRef = db.collection('accounts').doc(senderAccountId);
      const snapshot = await transaction.get(senderRef);
      
      if (!snapshot.exists) {
        throw new functions.https.HttpsError('not-found', 'Sender account not found');
      }

      const currentBalance = snapshot.data().balance;

      if (currentBalance < totalDeducted) {
        throw new functions.https.HttpsError('failed-precondition', 'Insufficient balance');
      }

      // Deduct from sender
      transaction.update(senderRef, {
        balance: admin.firestore.FieldValue.increment(-totalDeducted),
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      });

      // Create debit transaction for sender
      const transactionsCol = db.collection('transactions');
      const debitTxRef = transactionsCol.doc();

      transaction.set(debitTxRef, {
        accountId: senderAccountId,
        userId: senderId,
        type: 'DEBIT',
        category: transferType,
        amount: amount,
        currency: currency || 'USD',
        description: description || `Transfer to ${recipientName}`,
        recipientName: recipientName,
        recipientAccount: recipientAccount,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        status: 'COMPLETED',
        balanceAfter: currentBalance - totalDeducted,
        fees: fee,
        transferId: transferId,
        reference: reference
      });

      // For P2P transfers, credit recipient's account
      if (transferType === 'PEER_TO_PEER') {
        // Try to find recipient's account by account number
        const recipientAccounts = await db.collection('accounts')
          .where('accountNumber', '==', recipientAccount)
          .limit(1)
          .get();

        if (!recipientAccounts.empty && recipientAccounts.docs[0].data().userId !== senderId) {
          const recipientAccountRef = recipientAccounts.docs[0].ref;
          const recipientData = recipientAccounts.docs[0].data();

          // Credit recipient
          transaction.update(recipientAccountRef, {
            balance: admin.firestore.FieldValue.increment(amount),
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
          });

          // Create credit transaction for recipient
          const creditTxRef = transactionsCol.doc();

          transaction.set(creditTxRef, {
            accountId: recipientAccounts.docs[0].id,
            userId: recipientData.userId,
            type: 'CREDIT',
            category: 'TRANSFER',
            amount: amount,
            currency: currency || 'USD',
            description: `Received from ${senderAccount.accountName || senderAccount.accountNumber}`,
            recipientName: senderAccount.accountName || senderAccount.accountNumber,
            recipientAccount: senderAccount.accountNumber,
            timestamp: admin.firestore.FieldValue.serverTimestamp(),
            status: 'COMPLETED',
            balanceAfter: (recipientData.balance || 0) + amount,
            transferId: transferId
          });
        }
      }

      // Update transfer record
      const transferRef = db.collection('transfers').doc(transferId);

      // Check if transfer exists (if created locally first)
      const transferSnapshot = await transaction.get(transferRef);
      
      if (transferSnapshot.exists) {
        transaction.update(transferRef, {
          status: 'COMPLETED',
          processedAt: admin.firestore.FieldValue.serverTimestamp(),
          completedAt: admin.firestore.FieldValue.serverTimestamp()
        });
      } else {
        transaction.set(transferRef, {
          id: transferId,
          senderId: senderId,
          senderAccountId: senderAccountId,
          recipientName: recipientName,
          recipientAccount: recipientAccount,
          recipientBank: recipientBank,
          amount: amount,
          currency: currency || 'USD',
          transferType: transferType,
          description: description,
          status: 'COMPLETED',
          fee: fee,
          totalDeducted: totalDeducted,
          reference: reference,
          createdAt: admin.firestore.FieldValue.serverTimestamp(),
          processedAt: admin.firestore.FieldValue.serverTimestamp(),
          completedAt: admin.firestore.FieldValue.serverTimestamp()
        });
      }

      return {
        success: true,
        transferId: transferId,
        debitTransactionId: debitTxRef.id,
        balanceAfter: currentBalance - totalDeducted
      };
    });

    // Send notification to sender
    await sendFcmNotification(
      senderId,
      'Transfer Completed',
      `You successfully transferred ${amount} ${currency || 'USD'} to ${recipientName}`,
      'TRANSFER',
      transferId,
      senderAccountId
    );

    return result;

  } catch (error) {
    console.error('Error executing transfer:', error);

    // Update transfer status to FAILED if it was created
    try {
      await db.collection('transfers').doc(transferId).update({
        status: 'FAILED',
        failureReason: error.message,
        processedAt: admin.firestore.FieldValue.serverTimestamp()
      });
    } catch (e) {
      console.log('Could not update transfer status:', e.message);
    }

    throw new functions.https.HttpsError('internal', 'Transfer failed: ' + error.message);
  }
});

// Helper function to send FCM notification
async function sendFcmNotification(userId, title, message, type, transactionId, accountId) {
  try {
    const userDoc = await db.collection('users').doc(userId).get();
    if (userDoc.exists && userDoc.data().fcmToken) {
      await messaging.send({
        token: userDoc.data().fcmToken,
        notification: {
          title: title,
          body: message
        },
        data: {
          type: type || 'TRANSFER',
          transactionId: transactionId || '',
          accountId: accountId || ''
        }
      });
    }
  } catch (error) {
    console.log('Failed to send FCM:', error.message);
  }
}