package com.example.mybank.data.repository

import com.example.mybank.data.firebase.FirestoreService
import com.example.mybank.data.local.dao.NotificationDao
import com.example.mybank.data.model.BankNotification
import com.example.mybank.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao,
    private val firestoreService: FirestoreService
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeListeners = mutableMapOf<String, kotlinx.coroutines.Job>()

    // Cleanup methods to prevent memory leaks
    fun stopAllListeners() {
        activeListeners.values.forEach { it.cancel() }
        activeListeners.clear()
    }

    fun stopUserListener(userId: String) {
        activeListeners[userId]?.cancel()
        activeListeners.remove(userId)
    }

    fun getUserNotifications(userId: String): Flow<List<BankNotification>> {
        startRealtimeSync(userId)
        return notificationDao.getUserNotifications(userId)
    }

    fun getUnreadNotifications(userId: String): Flow<List<BankNotification>> {
        return notificationDao.getUnreadNotifications(userId)
    }

    fun getUnreadNotificationCount(userId: String): Flow<Int> {
        return notificationDao.getUnreadNotificationCount(userId)
    }

    private fun startRealtimeSync(userId: String) {
        if (activeListeners.containsKey(userId)) return

        val job = repositoryScope.launch {
            firestoreService.getUserNotifications(userId).collect { notifications ->
                notificationDao.insertNotifications(notifications)
            }
        }
        activeListeners[userId] = job
    }

    suspend fun markAsRead(notificationId: String) {
        try {
            notificationDao.markAsRead(notificationId)
            firestoreService.markNotificationAsRead(notificationId)
        } catch (e: Exception) {
            android.util.Log.e("NotificationRepository", "Failed to mark notification as read", e)
        }
    }

    suspend fun markAllAsRead(userId: String) {
        notificationDao.markAllAsRead(userId)
    }

    suspend fun insertNotification(notification: BankNotification) {
        notificationDao.insertNotification(notification)
        try {
            firestoreService.createNotification(notification)
        } catch (e: Exception) {
            android.util.Log.e("NotificationRepository", "Failed to sync notification to Firestore", e)
        }
    }

    suspend fun deleteNotification(notificationId: String) {
        notificationDao.deleteNotification(notificationId)
        try {
            firestoreService.deleteNotification(notificationId)
        } catch (e: Exception) {
            android.util.Log.e("NotificationRepository", "Failed to delete notification from Firestore", e)
        }
    }

    // Sync notifications - DEPRECATED: Notifications sync automatically via real-time Firestore listeners
    @Deprecated("Notifications sync automatically via real-time Firestore listeners", ReplaceWith(""))
    suspend fun syncNotifications(): Resource<List<BankNotification>> {
        return try {
            // Force local refresh - real-time listeners will update from Firestore
            Resource.Success(emptyList())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to refresh notifications")
        }
    }
}
