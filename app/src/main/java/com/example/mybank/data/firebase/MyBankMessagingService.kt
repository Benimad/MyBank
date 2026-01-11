package com.example.mybank.data.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mybank.MainActivity
import com.example.mybank.R
import com.example.mybank.data.model.BankNotification
import com.example.mybank.data.model.NotificationType
import com.example.mybank.data.preferences.PreferencesManager
import com.example.mybank.data.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class MyBankMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyBankMessagingService"
        private const val CHANNEL_ID = "mybank_notifications"
        private const val CHANNEL_NAME = "MyBank Notifications"
    }

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Save FCM token to Firestore instead of sending to API
        serviceScope.launch {
            try {
                val userId = firebaseAuth.currentUser?.uid
                if (userId != null) {
                    firestore.collection("users")
                        .document(userId)
                        .update("fcmToken", token)
                        .addOnSuccessListener {
                            Log.d(TAG, "FCM token saved to Firestore successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Failed to save FCM token to Firestore", e)
                        }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving FCM token", e)
            }
        }
    }
    
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        val title = message.notification?.title ?: "MyBank"
        val body = message.notification?.body ?: ""
        val notificationType = message.data["type"] ?: "INFO"
        val transactionId = message.data["transactionId"]
        val accountId = message.data["accountId"]
        
        // Save notification to local database
        serviceScope.launch {
            try {
                val userId = preferencesManager.userId.first() ?: return@launch
                
                val notification = BankNotification(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = try {
                        NotificationType.valueOf(notificationType)
                    } catch (e: Exception) {
                        NotificationType.INFO
                    },
                    title = title,
                    message = body,
                    timestamp = System.currentTimeMillis(),
                    isRead = false,
                    relatedTransactionId = transactionId,
                    relatedAccountId = accountId
                )
                
                notificationRepository.insertNotification(notification)
            } catch (e: Exception) {
                // Handle error
            }
        }
        
        // Show notification to user
        showNotification(title, body)
    }
    
    private fun showNotification(title: String, message: String) {
        createNotificationChannel()

        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                // Add extras safely to prevent Binder Parcel errors
                putExtra("notification_title", title)
                putExtra("notification_message", message)
            }

            val pendingIntent = PendingIntent.getActivity(
                this,
                System.currentTimeMillis().toInt(), // Use unique request ID
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title.ifEmpty { "MyBank Notification" })
                .setContentText(message.ifEmpty { "You have a new notification" })
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification", e)
            e.printStackTrace()
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "MyBank notifications for transactions and alerts"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
