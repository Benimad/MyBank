@echo off
echo.
echo ============================================
echo  VERIFY APP CHECK ENFORCEMENT DISABLED
echo ============================================
echo.
echo This script will help you verify if App Check
echo enforcement has been successfully disabled.
echo.
echo --------------------------------------------
echo  Step 1: Check Firebase Console
echo --------------------------------------------
echo.
echo Opening Firebase Console in your browser...
start https://console.firebase.google.com/project/mybank-8deeb/appcheck
echo.
echo In the Firebase Console:
echo 1. Click on "APIs" tab
echo 2. Find "Cloud Functions for Firebase"
echo 3. Check if status shows "Unenforced"
echo.
pause
echo.
echo --------------------------------------------
echo  Step 2: Test Send Money in App
echo --------------------------------------------
echo.
echo Now test Send Money in your app:
echo 1. Open the MyBank app on your device/emulator
echo 2. Click "Send Money" icon
echo 3. Enter amount: 50
echo 4. Search for recipient by email
echo 5. Click Continue, then Send
echo.
echo Did the transaction succeed? (Y/N)
set /p success="Enter your answer: "
echo.
if /i "%success%"=="Y" (
    echo ✅ SUCCESS! App Check enforcement is disabled.
    echo ✅ Send Money is now working correctly.
    echo.
    echo You can now use all features of the app.
) else (
    echo ❌ Still not working?
    echo.
    echo Troubleshooting:
    echo 1. Make sure you clicked "Unenforced" for Cloud Functions
    echo 2. Wait 30 seconds and try again
    echo 3. Check Logcat for error messages
    echo.
    echo Opening troubleshooting guide...
    start DISABLE_APP_CHECK_NOW.md
)
echo.
echo --------------------------------------------
echo  Next Steps
echo --------------------------------------------
echo.
echo ✅ For development: Keep App Check unenforced
echo ⚠️  For production: Re-enable with Play Integrity
echo.
pause
