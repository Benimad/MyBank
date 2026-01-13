# Deploy Firestore Rules - Fix Permission Denied Error

## Issue Fixed
The Firestore security rules were too strict and didn't allow:
1. Account balance updates during deposits
2. Transaction creation without userId field

## Changes Made
1. **Accounts collection**: Added validation for balance updates (must be >= 0)
2. **Transactions collection**: Made userId field optional for backward compatibility

## Deploy Instructions

### Option 1: Using Firebase CLI
```bash
# 1. Login to Firebase (if not already logged in)
firebase login

# 2. Deploy the updated rules
firebase deploy --only firestore:rules
```

### Option 2: Using Firebase Console
1. Go to: https://console.firebase.google.com
2. Select your project
3. Navigate to: Firestore Database → Rules
4. Copy the content from `firestore.rules` file
5. Paste into the Firebase Console
6. Click "Publish"

## Verification
After deploying, test the deposit functionality:
1. Open the app
2. Navigate to "Add Money"
3. Enter amount: $500.00
4. Select payment method
5. Click "Add Money Now"
6. Should succeed without permission error ✅

## What Was Fixed
- ✅ Account balance updates now allowed with validation
- ✅ Transaction creation works with or without userId
- ✅ Maintains security (only account owner can update)
- ✅ Balance cannot go negative

## Next Steps
After deploying rules, also fix the Transaction model to include userId field (see TECHNICAL_ISSUES.md)
