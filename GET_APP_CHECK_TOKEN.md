# Get Firebase App Check Debug Token

## Steps to Register Debug Token

### 1. Run the App
Build and run the app in Android Studio on your emulator or device.

### 2. Find the Debug Token in Logcat
After the app starts, search Logcat for "AppCheck":
```
tag:FirebaseAppCheck
```

You should see a message like:
```
Firebase App Check debug token: XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
```

### 3. Register the Token in Firebase Console

1. Go to: https://console.firebase.google.com/project/mybank-8deeb/appcheck
2. Click on your Android app
3. Click "Manage debug tokens"
4. Click "Add debug token"
5. Paste the token from Logcat
6. Click "Save"

### 4. Test Send Money
After registering the token, the UNAUTHENTICATED error should be fixed!

## Quick Alternative: Disable App Check Enforcement

If you need to test immediately without registering tokens:

1. Go to Firebase Console > App Check
2. Click on "APIs" tab
3. Find "Cloud Functions"
4. Click the three dots menu
5. Select "Unenforced" (for development only)

⚠️ **Note**: Re-enable enforcement before deploying to production!
