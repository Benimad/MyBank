# Deploy Firebase Functions

## Prerequisites
1. Install Firebase CLI: `npm install -g firebase-tools`
2. Login to Firebase: `firebase login`

## Deploy Functions
```bash
cd functions
npm install
cd ..
firebase deploy --only functions
```

## Verify Deployment
After deployment, check the Firebase Console:
https://console.firebase.google.com/project/mybank-8deeb/functions

## Test the processTransfer Function
The function should appear in the Functions list with status "Healthy"
