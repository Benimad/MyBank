package com.example.mybank.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

enum class TransactionType {
    DEBIT,   // Débit (sortie d'argent)
    CREDIT   // Crédit (entrée d'argent)
}

enum class TransactionCategory {
    TRANSFER,
    PAYMENT,
    DEPOSIT,
    WITHDRAWAL,
    SALARY,
    BILL,
    SHOPPING,
    FOOD,
    TRANSPORT,
    ENTERTAINMENT,
    OTHER
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey
    @SerializedName("id")
    val id: String = "",

    @SerializedName("accountId")
    val accountId: String = "",

    @SerializedName("userId")
    val userId: String = "",  // ✅ ADDED: userId field for querying and filtering

    @SerializedName("type")
    val type: TransactionType = TransactionType.DEBIT,

    @SerializedName("category")
    val category: TransactionCategory = TransactionCategory.OTHER,

    @SerializedName("amount")
    val amount: Double = 0.0,

    @SerializedName("currency")
    val currency: String = "EUR",

    @SerializedName("description")
    val description: String = "",

    @SerializedName("recipientName")
    val recipientName: String? = null,

    @SerializedName("recipientAccount")
    val recipientAccount: String? = null,

    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @SerializedName("status")
    val status: String = "COMPLETED",

    @SerializedName("balanceAfter")
    val balanceAfter: Double? = null
) {
    // Empty constructor for Firebase deserialization
    constructor() : this(
        id = "",
        accountId = "",
        userId = "",  // ✅ ADDED: userId in empty constructor
        type = TransactionType.DEBIT,
        category = TransactionCategory.OTHER,
        amount = 0.0,
        currency = "EUR",
        description = "",
        recipientName = null,
        recipientAccount = null,
        timestamp = System.currentTimeMillis(),
        status = "COMPLETED",
        balanceAfter = null
    )
}