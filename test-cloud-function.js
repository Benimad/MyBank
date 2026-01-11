// Test Cloud Function Direct Call
// This will help diagnose if App Check is the issue

const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json'); // You'll need to download this

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const functions = admin.functions();

async function testProcessTransfer() {
  try {
    console.log('Testing processTransfer Cloud Function...');
    
    // This test will fail if App Check is enforced
    const result = await functions
      .httpsCallable('processTransfer')
      .call({
        fromAccountId: 'm2TjxinEoajqC8yZDPBj',
        toAccountId: 'LmJVLtUwNhNeRCThB4Ap',
        amount: 1,
        currency: 'USD',
        description: 'Test transfer',
        idempotencyKey: 'test-' + Date.now()
      });
    
    console.log('Success:', result);
  } catch (error) {
    console.error('Error:', error.code, error.message);
    
    if (error.code === 'unauthenticated') {
      console.log('\n❌ CONFIRMED: App Check enforcement is blocking requests');
      console.log('Solution: Disable App Check enforcement in Firebase Console');
      console.log('URL: https://console.firebase.google.com/project/mybank-8deeb/appcheck');
    }
  }
}

testProcessTransfer();
