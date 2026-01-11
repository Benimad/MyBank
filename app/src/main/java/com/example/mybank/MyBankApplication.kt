package com.example.mybank

import android.app.Application
import android.util.Log
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class MyBankApplication : Application() {

    companion object {
        private const val TAG = "MyBankApplication"
        private const val MIGRATION_PREF = "migration_prefs"
        private const val MIGRATION_ACCOUNTS_ISACTIVE = "accounts_isactive_done"
    }

    override fun onCreate() {
        try {
            super.onCreate()
            Log.d(TAG, "Application onCreate started")

            // Initialize Firebase (google-services plugin handles most of this)
            // This handles any Firebase initialization errors gracefully
            com.google.firebase.FirebaseApp.initializeApp(this)

            // Enable App Check with Debug provider for development
            val firebaseAppCheck = FirebaseAppCheck.getInstance()
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Log.d(TAG, "Firebase initialized successfully")

            // Run one-time migration for old accounts
            runMigrations()

            Log.d(TAG, "Application onCreate completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error during application initialization", e)
            // Don't crash the app, but log the error
            e.printStackTrace()
        }
    }

    private fun runMigrations() {
        val prefs = getSharedPreferences(MIGRATION_PREF, MODE_PRIVATE)
        val migrationDone = prefs.getBoolean(MIGRATION_ACCOUNTS_ISACTIVE, false)

        if (!migrationDone) {
            Log.d(TAG, "Running account migration...")
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                try {
                    com.example.mybank.util.FirestoreMigration.migrateAccountsAddIsActive(
                        FirebaseFirestore.getInstance()
                    )
                    prefs.edit().putBoolean(MIGRATION_ACCOUNTS_ISACTIVE, true).apply()
                    Log.d(TAG, "Account migration completed")
                } catch (e: Exception) {
                    Log.e(TAG, "Migration failed: ${e.message}", e)
                }
            }
        }
    }
}