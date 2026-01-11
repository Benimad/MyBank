package com.example.mybank.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

enum class PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    SCHEDULED
}

enum class RecurrenceFrequency {
    NONE,
    DAILY,
    WEEKLY,
    BI_WEEKLY,
    MONTHLY,
    QUARTERLY,
    YEARLY
}

@Entity(tableName = "bill_payments")
data class BillPayment(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("billerId")
    val billerId: String,
    
    @SerializedName("accountId")
    val accountId: String,
    
    @SerializedName("amount")
    val amount: Double,
    
    @SerializedName("currency")
    val currency: String = "USD",
    
    @SerializedName("status")
    val status: PaymentStatus = PaymentStatus.PENDING,
    
    @SerializedName("isRecurring")
    val isRecurring: Boolean = false,
    
    @SerializedName("recurrenceFrequency")
    val recurrenceFrequency: RecurrenceFrequency = RecurrenceFrequency.NONE,
    
    @SerializedName("scheduledDate")
    val scheduledDate: Long? = null,
    
    @SerializedName("nextPaymentDate")
    val nextPaymentDate: Long? = null,
    
    @SerializedName("lastPaymentDate")
    val lastPaymentDate: Long? = null,
    
    @SerializedName("reference")
    val reference: String? = null,
    
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    
    @SerializedName("completedAt")
    val completedAt: Long? = null
)
