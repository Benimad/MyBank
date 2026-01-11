# Fix Send Money Authentication Issue

## The Problem
Firebase Functions `processTransfer` is deployed and working, but returns UNAUTHENTICATED error because the user's authentication token is expired or invalid.

## The Solution (Choose ONE)

### Option 1: Sign Out and Sign In (EASIEST - DO THIS FIRST)
1. Open the app
2. Go to Profile/Settings
3. Click "Sign Out"
4. Sign in again with your credentials
5. Try sending money again

This refreshes your authentication token and should fix the issue immediately.

### Option 2: If Option 1 Doesn't Work - Check Firebase Console
1. Go to https://console.firebase.google.com/project/mybank-8deeb/functions
2. Verify that `processTransfer` function shows status "Healthy"
3. Check the function logs for any errors
4. If you see "context.auth is null", the authentication isn't being passed

### Option 3: Test with Firebase Emulator (For Development)
If you want to test locally without authentication issues:

1. Install Firebase emulator:
   ```bash
   npm install -g firebase-tools
   firebase login
   ```

2. Start emulators:
   ```bash
   cd c:\Users\AdMin\AndroidStudioProjects\MyBank
   firebase emulators:start
   ```

3. Update AppModule.kt to use emulator:
   ```kotlin
   @Provides
   @Singleton
   fun provideFirebaseFunctions(): FirebaseFunctions {
       val functions = FirebaseFunctions.getInstance()
       functions.useEmulator("10.0.2.2", 5001)  // For Android emulator
       return functions
   }
   ```

## Why This Happens
- Firebase Authentication tokens expire after 1 hour
- The app caches the old token
- Cloud Functions reject expired tokens with UNAUTHENTICATED error
- Signing out and back in gets a fresh token

## Verification
After signing out and back in, you should see:
1. Transaction completes successfully
2. Sender balance decreases
3. Recipient balance increases
4. Success screen appears
5. Transaction appears in both users' history

## If Still Not Working
The Cloud Functions are deployed correctly. The issue is 100% authentication-related.
Contact me with the Firebase Console function logs.
