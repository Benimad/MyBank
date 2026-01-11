package com.example.mybank.data.local.dao

import androidx.room.*
import com.example.mybank.data.model.BankNotification
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserNotifications(userId: String): Flow<List<BankNotification>>
    
    @Query("SELECT * FROM notifications WHERE userId = :userId AND isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadNotifications(userId: String): Flow<List<BankNotification>>
    
    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadNotificationCount(userId: String): Flow<Int>
    
    @Query("SELECT * FROM notifications WHERE id = :notificationId")
    fun getNotification(notificationId: String): Flow<BankNotification?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: BankNotification)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<BankNotification>)
    
    @Update
    suspend fun updateNotification(notification: BankNotification)
    
    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: String)
    
    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)
    
    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteNotification(notificationId: String)
    
    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteUserNotifications(userId: String)
    
    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()
}
