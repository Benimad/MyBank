# Disable Firebase App Check Enforcement

## CRITICAL FIX for UNAUTHENTICATED Error

The Send Money feature is failing because App Check enforcement is enabled in Firebase Console.

## Steps to Disable App Check Enforcement

### 1. Open Firebase Console
https://console.firebase.google.com/project/mybank-8deeb/appcheck

### 2. Navigate to App Check Section
- In the left sidebar, click on "App Check"
- You should see "Cloud Functions for Firebase" listed

### 3. Disable Enforcement for Cloud Functions
- Find "Cloud Functions for Firebase" in the APIs list
- Click the three-dot menu (⋮) next to it
- Select **"Unenforced"**
- Confirm the change

### 4. Verify the Change
After disabling enforcement:
- Go back to your app
- Try Send Money again
- It should work immediately (no rebuild needed)

## Why This Fixes the Issue

App Check was blocking authenticated requests because:
1. Your app doesn't have App Check configured
2. Firebase rejects requests without valid App Check tokens
3. Even though auth token is valid, App Check takes precedence

## For Production

Before deploying to production, you should:
1. Enable App Check in the Android app
2. Use Play Integrity provider (not Debug provider)
3. Re-enable enforcement in Firebase Console

## Alternative: Enable App Check in App (More Complex)

If you prefer to enable App Check instead:
1. Uncomment App Check code in MyBankApplication.kt
2. Run the app
3. Get the debug token from Logcat (search for "AppCheck")
4. Register the debug token in Firebase Console → App Check → Manage debug tokens
