// Simple test to verify Cloud Function works without App Check
// Run with: node test-transfer.js (after installing firebase-admin and firebase-functions)

const admin = require('firebase-admin');

// Initialize Firebase
const serviceAccount = require('./functions/service-account-key.json'); // You need this file

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const functions = admin.functions();

async function testTransfer() {
  console.log('=== Testing Cloud Function without App Check ===\n');

  // Test data (replace with actual account IDs from your Firestore)
  const testData = {
    fromAccountId: 'YOUR_SENDER_ACCOUNT_ID',
    toAccountId: 'YOUR_RECIPIENT_ACCOUNT_ID',
    amount: 1.0,
    currency: 'USD',
    description: 'Test transfer',
    idempotencyKey: `test-${Date.now()}`
  };

  try {
    console.log('Calling Cloud Function directly (bypasses App Check)...');
    const regionFunctions = functions.httpsCallable('processTransfer', 'us-central1');
    const result = await regionFunctions(testData);

    console.log('\n✅ Success! Cloud Function responded:');
    console.log(JSON.stringify(result, null, 2));

    if (result.data.success) {
      console.log(`\n✅ Transfer successful!`);
      console.log(`From balance: $${result.data.fromBalance}`);
      console.log(`To balance: $${result.data.toBalance}`);
    } else {
      console.log(`\n❌ Transfer failed: ${result.data.error}`);
    }
  } catch (error) {
    console.log('\n❌ Error calling Cloud Function:');
    console.log(error.code, error.message);

    if (error.code === 'unauthenticated' || error.code === 401) {
      console.log('\n⚠️ UNAUTHENTICATED: Still getting auth errors');
      console.log('This means:');
      console.log('1. App Check is still blocking', OR);
      console.log('2. Auth token is not being passed correctly');
    }
  }
}

console.log('Note: This test requires Firebase Admin SDK\n');
console.log('Follow the instructions in FINAL_APP_CHECK_FIX.md instead\n');