package com.example.mybank.util

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirestoreMigration {
    private const val TAG = "FirestoreMigration"

    /**
     * Migrate old accounts to add isActive field
     * Run this once to update existing accounts in Firestore
     */
    suspend fun migrateAccountsAddIsActive(firestore: FirebaseFirestore) {
        try {
            Log.d(TAG, "Starting account migration...")
            
            val accountsSnapshot = firestore.collection("accounts")
                .get()
                .await()
            
            var updatedCount = 0
            var skippedCount = 0
            
            for (doc in accountsSnapshot.documents) {
                // Check if isActive field exists
                if (!doc.contains("isActive")) {
                    // Add isActive = true to old accounts
                    doc.reference.update("isActive", true).await()
                    updatedCount++
                    Log.d(TAG, "Updated account ${doc.id} with isActive=true")
                } else {
                    skippedCount++
                }
            }
            
            Log.d(TAG, "Migration complete: $updatedCount accounts updated, $skippedCount already had isActive field")
        } catch (e: Exception) {
            Log.e(TAG, "Migration failed: ${e.message}", e)
        }
    }
}
