package com.example.mybank

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyBankApplication : Application() {

    companion object {
        private const val TAG = "MyBankApplication"
    }

    override fun onCreate() {
        try {
            super.onCreate()
            Log.d(TAG, "Application onCreate started")

            // Initialize Firebase (google-services plugin handles most of this)
            // This handles any Firebase initialization errors gracefully
            com.google.firebase.FirebaseApp.initializeApp(this)

            Log.d(TAG, "Application onCreate completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error during application initialization", e)
            // Don't crash the app, but log the error
            e.printStackTrace()
        }
    }
}