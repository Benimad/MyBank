# Check App Check Status

## Quick Test: Disable App Check Enforcement

### Step 1: Open Firebase Console
I've already opened this for you!

### Step 2: Follow these exact steps:

1. **Look at the page that opened** - You should see "App Check" in the left sidebar
2. **Click on the "APIs" tab** (there are two tabs: "Apps" and "APIs")
3. **Find "Cloud Functions for Firebase"** in the list
4. **Click the three-dot menu (⋮)** next to "Cloud Functions for Firebase"
5. **Select "Unenforced"** from the dropdown
6. **Click "Confirm"**

### Step 3: Verify
After you do this, the status next to "Cloud Functions for Firebase" should change to **"Unenforced"**

### Step 4: Test Send Money
1. Go back to your app (it should still be running)
2. Try to send money again
3. It should work!

## Visual Guide

**What you'll see:**
```
Firebase Console → App Check → APIs tab

[Cloud Functions for Firebase] [Enforced ⬇]  ⋮
                                            ↓
                                  Click this menu
                                  Select "Unenforced"
```

**After changing:**
```
[Cloud Functions for Firebase] [Unenforced]  ✅
```

## Why This Works

Your app does NOT send App Check tokens (because you commented it out in `MyBankApplication.kt`).
But Firebase Console is set to ENFORCE App Check.

So:
- App calls Cloud Function → No App Check token
- Firebase sees no token → REJECTS with UNAUTHENTICATED
- App Check takes precedence over Auth tokens

By setting to "Unenforced":
- App calls Cloud Function → No App Check token needed
- Firebase checks Auth token (which is now working!) ✅
- Transfer succeeds! ✅

## After Testing

Once you confirm Send Money works, you have two options:

### Option A: Keep it Unenforced (Easiest for development)
- No changes needed
- Send Money will work
- OK for testing/presentation

### Option B: Properly enable App Check (For production)
1. Uncomment App Check code in `MyBankApplication.kt`
2. Use debug provider (development) or Play Integrity (production)
3. Re-enable enforcement after configuring properly

---

**NEXT STEP:**
Follow the steps above in Firebase Console, then test Send Money!