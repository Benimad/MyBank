# Fix: No Active Account Found

## Problem

The user `ooyMN0SsR9aDE5JdnwpwTUbR7yz1` (imad@gmail.com) doesn't have any accounts in Firestore.

**Error in logs:**
```
00:15:59.969  D  Found 0 active accounts in Firestore
```

**Error shown in app:**
```
No active account found. Please create an account first.
```

---

## Root Cause

The Cloud Function `onUserCreate` (in `functions/index.js` lines 75-100) is supposed to automatically create a default checking account when a user signs up. This either:

1. **Wasn't deployed** to Firebase
2. **Failed to execute** when the user signed up
3. **User was created before** the Cloud Function was deployed

---

## ✅ Quick Fix: Manually Create Account via Firebase Console

### Step-by-Step:

1. **Go to Firebase Console**: https://console.firebase.google.com

2. **Select your MyBank project**

3. **Navigate to Firestore Database** (left sidebar)

4. **Click "Start collection"** (or if you already have `accounts` collection, click into it and add a document)

5. **Collection ID**: `accounts`

6. **Click "Auto-ID"** to generate a random document ID (or click "Add document")

7. **Add these exact fields:**

   | Field | Type | Value |
   |-------|------|-------|
   | `userId` | string | `ooyMN0SsR9aDE5JdnwpwTUbR7yz1` |
   | `accountNumber` | string | `CHK-123456` |
   | `accountName` | string | `Checking` |
   | `accountType` | string | `CHECKING` |
   | `balance` | number | `0` |
   | `currency` | string | `USD` |
   | `isActive` | boolean | `true` |
   | `createdAt` | timestamp | Click "timestamp" → current time |
   | `iban` | null | (leave as null) |

8. **Click "Save"**

9. **Test the app** - try the deposit again. It should now work!

---

## 🔧 Permanent Fix: Deploy Cloud Functions

To prevent this from happening to future users, deploy the Cloud Functions:

### Option A: Firebase Console (Recommended if CLI doesn't work)

1. Go to https://console.firebase.google.com
2. Select **MyBank** project
3. Click **"Functions"** in left sidebar
4. If you see `onUserCreate` function listed:
   - It's deployed ✅
   - Check the logs to see why it failed
5. If you DON'T see it:
   - You need to deploy it (see Option B)

### Option B: Deploy via CLI

```bash
# In the project root directory
cd C:\Users\AdMin\AndroidStudioProjects\MyBank

# Login to Firebase (if not already)
firebase login

# Deploy all functions
firebase deploy --only functions

# Or deploy just the onUserCreate function
firebase deploy --only functions:onUserCreate
```

---

## 🔍 Check if Cloud Function is Working

After deploying, test it:

1. **Create a new test user** in Firebase Authentication
2. **Check Firestore** - a new account should be automatically created in the `accounts` collection
3. **Check Function logs** in Firebase Console → Functions → Logs

---

## 📝 Why This Happened

Looking at your Cloud Function (`functions/index.js`), the `onUserCreate` function (lines 75-100) should create a default account:

```javascript
exports.onUserCreate = functions.auth.user().onCreate(async (user) => {
  // ... creates user doc ...
  
  // Create default checking account
  const accountsCol = db.collection('accounts');
  const accountRef = accountsCol.doc();  // Auto-generates ID
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
});
```

**This function is NOT deployed** or **failed to execute** for the existing user.

---

## ✅ After Creating the Account

Once you've manually created the account in Firestore:

1. **Close and reopen the app**
2. **Try the deposit again**
3. **You should see:**
   - No more "No active account found" error
   - The deposit should proceed
   - If there are still errors, check the Firestore index issue (see below)

---

## ⚠️ Additional Issue: Missing Firestore Index

I also noticed this error in your logs:

```
00:15:20.421  W  Listen for Query(target=Query(transactions where accountId==a61cb3b1-8708-4e97-8c06-feb85a044a10 order by -timestamp, -__name__);limitType=LIMIT_TO_FIRST) failed: Status{code=FAILED_PRECONDITION, description=The query requires an index.
```

**To fix this:**

1. The error message includes a link - click it or paste it in your browser
2. It will take you to Firebase Console to create the required index automatically
3. Wait 2-5 minutes for the index to build
4. The transaction history screen should then work

**Or manually create the index:**

1. Firebase Console → Firestore Database → Indexes tab
2. Click "Create Index"
3. Collection: `transactions`
4. Fields to index:
   - `accountId`: Ascending
   - `timestamp`: Descending
5. Click "Create"

---

## 📊 Summary

| Issue | Status | Fix |
|-------|--------|-----|
| No account for user | 🔴 Critical | Create manually via Firebase Console |
| Cloud Function not deployed | ⚠️ Warning | Deploy `onUserCreate` function |
| Missing Firestore index | ⚠️ Warning | Click link in error or create manually |

---

## 🎯 Next Steps

1. ✅ **Immediately**: Create the account manually in Firebase Console (5 minutes)
2. ✅ **Short-term**: Deploy Cloud Functions so future users get accounts automatically (10 minutes)
3. ✅ **Optional**: Create the missing Firestore index for transaction queries (5 minutes)

---

**After completing Step 1, test the deposit again - it should work!** 🚀
