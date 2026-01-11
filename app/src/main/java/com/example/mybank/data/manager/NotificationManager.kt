package com.example.mybank.data.manager

import com.example.mybank.data.model.BankNotification
import com.example.mybank.data.model.NotificationType
import com.example.mybank.data.repository.NotificationRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManager @Inject constructor(
    private val repository: NotificationRepository
) {

    suspend fun createNotification(
        userId: String,
        type: NotificationType,
        title: String,
        message: String,
        amount: String? = null,
        relatedTransactionId: String? = null,
        actionUrl: String? = null,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val notification = BankNotification(
            id = UUID.randomUUID().toString(),
            userId = userId,
            type = type,
            title = title,
            message = message,
            timestamp = timestamp,
            isRead = false,
            amount = amount,
            relatedTransactionId = relatedTransactionId,
            actionUrl = actionUrl
        )
        repository.insertNotification(notification)
    }

    suspend fun createSalaryNotification(userId: String, amount: String, timestamp: Long = System.currentTimeMillis()) {
        createNotification(
            userId = userId,
            type = NotificationType.INCOME,
            title = "Salary Received",
            message = "Your salary of $amount has been successfully credited to your account.",
            amount = amount,
            timestamp = timestamp
        )
    }

    suspend fun createPaymentNotification(userId: String, merchant: String, amount: String, timestamp: Long = System.currentTimeMillis()) {
        createNotification(
            userId = userId,
            type = NotificationType.EXPENSE,
            title = "Payment Successful",
            message = "Payment to $merchant was successful.",
            amount = amount,
            timestamp = timestamp
        )
    }

    suspend fun createSubscriptionNotification(userId: String, service: String, amount: String, timestamp: Long = System.currentTimeMillis()) {
        createNotification(
            userId = userId,
            type = NotificationType.SUBSCRIPTION,
            title = "$service Subscription",
            message = "Automatic payment of $amount was successful.",
            amount = amount,
            timestamp = timestamp
        )
    }

    suspend fun createLowBalanceAlert(userId: String, threshold: String, timestamp: Long = System.currentTimeMillis()) {
        createNotification(
            userId = userId,
            type = NotificationType.LOW_BALANCE,
            title = "Low Balance Alert",
            message = "Your Checking Account balance has dropped below your set threshold of $threshold.",
            timestamp = timestamp
        )
    }

    suspend fun createSecurityAlert(userId: String, deviceName: String, location: String, timestamp: Long = System.currentTimeMillis()) {
        createNotification(
            userId = userId,
            type = NotificationType.SECURITY,
            title = "New Device Login",
            message = "A new login was detected from $deviceName in $location. Was this you?",
            timestamp = timestamp
        )
    }
    
    suspend fun createRateUpdateNotification(userId: String, newRate: String, timestamp: Long = System.currentTimeMillis()) {
        createNotification(
            userId = userId,
            type = NotificationType.RATE_UPDATE,
            title = "Interest Rate Update",
            message = "Good news! Your High Yield Savings APY has increased to $newRate.",
            timestamp = timestamp
        )
    }
    
    suspend fun createStatementNotification(userId: String, month: String, timestamp: Long = System.currentTimeMillis()) {
        createNotification(
            userId = userId,
            type = NotificationType.STATEMENT,
            title = "Monthly Statement Ready",
            message = "Your statement for $month is now available to view and download.",
            timestamp = timestamp
        )
    }
}
