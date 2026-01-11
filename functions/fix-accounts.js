const admin = require('firebase-admin');

admin.initializeApp();

const db = admin.firestore();

function generateAccountNumber() {
  const timestamp = Date.now().toString().slice(-8);
  const random = Math.floor(Math.random() * 10000).toString().padStart(4, '0');
  return `${timestamp}${random}`;
}

async function fixUsersWithoutAccounts() {
  console.log('Starting to fix users without accounts...');
  
  try {
    // Get all users
    const usersSnapshot = await db.collection('users').get();
    console.log(`Found ${usersSnapshot.size} users`);
    
    for (const userDoc of usersSnapshot.docs) {
      const userId = userDoc.id;
      const userData = userDoc.data();
      
      // Check if user has any accounts
      const accountsSnapshot = await db.collection('accounts')
        .where('userId', '==', userId)
        .limit(1)
        .get();
      
      if (accountsSnapshot.empty) {
        console.log(`User ${userData.name || userId} has no account. Creating one...`);
        
        // Create account
        const accountRef = db.collection('accounts').doc();
        const accountNumber = generateAccountNumber();
        
        await accountRef.set({
          id: accountRef.id,
          userId: userId,
          accountNumber: accountNumber,
          accountName: 'Main Checking',
          accountType: 'CHECKING',
          balance: 0,
          currency: 'USD',
          iban: null,
          isActive: true,
          createdAt: admin.firestore.FieldValue.serverTimestamp(),
          dailyTransferTotal: 0,
          dailyLimitResetDate: 0
        });
        
        console.log(`✓ Created account ${accountNumber} for user ${userData.name || userId}`);
      } else {
        console.log(`✓ User ${userData.name || userId} already has an account`);
      }
    }
    
    console.log('\n✅ All users now have accounts!');
    process.exit(0);
  } catch (error) {
    console.error('Error:', error);
    process.exit(1);
  }
}

fixUsersWithoutAccounts();
