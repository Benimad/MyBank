# Script to verify App Check is properly disabled
Write-Host "=== App Check Verification Script ===" -ForegroundColor Cyan

$firebaseConsoleUrl = "https://console.firebase.google.com/project/mybank-8deeb/appcheck"

Write-Host ""
Write-Host "STEP 1: Open Firebase Console" -ForegroundColor Yellow
Write-Host "Opening browser to: $firebaseConsoleUrl" -ForegroundColor White
Start-Process $firebaseConsoleUrl

Write-Host ""
Write-Host "STEP 2: Disable App Check Enforcement" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Gray
Write-Host "1. In the browser, look for the 'APIs' tab (should be next to 'Apps')" -ForegroundColor White
Write-Host "2. Click on 'APIs' tab" -ForegroundColor White
Write-Host "3. Find 'Cloud Functions for Firebase' in the list" -ForegroundColor White
Write-Host "4. Click the three-dot menu (⋮) next to it" -ForegroundColor White
Write-Host "5. Select 'Unenforced'" -ForegroundColor Green
Write-Host "6. Click 'Confirm' if prompted" -ForegroundColor White
Write-Host "================================================" -ForegroundColor Gray

Write-Host ""
Write-Host "STEP 3: Verify the Change" -ForegroundColor Yellow
Write-Host "After changing, the status should show: 'Unenforced'" -ForegroundColor White
Write-Host ""

Write-Host "When done, come back and press any key to continue..." -ForegroundColor Cyan
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

Write-Host ""
Write-Host "=== Testing Send Money ===" -ForegroundColor Cyan
Write-Host "1. Go to your app (it should still be running)" -ForegroundColor White
Write-Host "2. Try Send Money again" -ForegroundColor White
Write-Host "3. Enter amount: 50" -ForegroundColor White
Write-Host "4. Click Continue → Send" -ForegroundColor White
Write-Host ""
Write-Host "Expected: ✅ Transfer successful without UNAUTHENTICATED error" -ForegroundColor Green
Write-Host ""

Write-Host "If it still fails, check Logcat for errors:" -ForegroundColor Yellow
Write-Host "adb logcat | grep -i 'processTransfer'" -ForegroundColor Cyan
Write-Host ""

Write-Host "Press any key to exit..." -ForegroundColor Cyan
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")