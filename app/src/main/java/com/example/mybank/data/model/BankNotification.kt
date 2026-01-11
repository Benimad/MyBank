package com.example.mybank.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

enum class NotificationType {
    INCOME,
    EXPENSE,
    LOW_BALANCE,
    SECURITY,
    SUBSCRIPTION,
    RATE_UPDATE,
    STATEMENT,
    INFO
}

@Entity(tableName = "notifications")
data class BankNotification(
    @PrimaryKey
    @SerializedName("id")
    val id: String = "",

    @SerializedName("userId")
    val userId: String = "",

    @SerializedName("type")
    val type: NotificationType = NotificationType.INFO,

    @SerializedName("title")
    val title: String = "",

    @SerializedName("message")
    val message: String = "",

    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @SerializedName("isRead")
    val isRead: Boolean = false,

    @SerializedName("amount")
    val amount: String? = null,

    @SerializedName("relatedTransactionId")
    val relatedTransactionId: String? = null,

    @SerializedName("relatedAccountId")
    val relatedAccountId: String? = null,

    @SerializedName("actionUrl")
    val actionUrl: String? = null
) {
    // Empty constructor for Firebase deserialization
    constructor() : this(
        id = "",
        userId = "",
        type = NotificationType.INFO,
        title = "",
        message = "",
        timestamp = System.currentTimeMillis(),
        isRead = false,
        amount = null,
        relatedTransactionId = null,
        relatedAccountId = null,
        actionUrl = null
    )
}
