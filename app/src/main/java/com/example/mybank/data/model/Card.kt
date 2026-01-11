package com.example.mybank.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

enum class CardType {
    DEBIT,
    CREDIT,
    VIRTUAL
}

enum class CardStatus {
    ACTIVE,
    FROZEN,
    BLOCKED,
    PENDING_ACTIVATION,
    EXPIRED,
    CANCELLED
}

enum class CardNetwork {
    VISA,
    MASTERCARD,
    AMEX,
    DISCOVER
}

@Entity(tableName = "cards")
data class Card(
    @PrimaryKey
    @SerializedName("id")
    val id: String = "",

    @SerializedName("userId")
    val userId: String = "",

    @SerializedName("accountId")
    val accountId: String = "",

    @SerializedName("cardNumber")
    val cardNumber: String = "",

    @SerializedName("cardHolderName")
    val cardHolderName: String = "",

    @SerializedName("expiryMonth")
    val expiryMonth: Int = 1,

    @SerializedName("expiryYear")
    val expiryYear: Int = 2025,

    @SerializedName("cvv")
    val cvv: String = "",

    @SerializedName("pin")
    val pin: String? = null,

    @SerializedName("cardType")
    val cardType: CardType = CardType.DEBIT,

    @SerializedName("cardNetwork")
    val cardNetwork: CardNetwork = CardNetwork.VISA,

    @SerializedName("status")
    val status: CardStatus = CardStatus.PENDING_ACTIVATION,

    @SerializedName("dailyLimit")
    val dailyLimit: Double = 5000.0,

    @SerializedName("monthlyLimit")
    val monthlyLimit: Double = 50000.0,

    @SerializedName("contactlessEnabled")
    val contactlessEnabled: Boolean = true,

    @SerializedName("onlinePaymentsEnabled")
    val onlinePaymentsEnabled: Boolean = true,

    @SerializedName("atmWithdrawalsEnabled")
    val atmWithdrawalsEnabled: Boolean = true,

    @SerializedName("internationalEnabled")
    val internationalEnabled: Boolean = false,

    @SerializedName("isVirtual")
    val isVirtual: Boolean = false,

    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),

    @SerializedName("activatedAt")
    val activatedAt: Long? = null,

    @SerializedName("lastUsedAt")
    val lastUsedAt: Long? = null
) {
    // Empty constructor for Firebase deserialization
    constructor() : this(
        id = "",
        userId = "",
        accountId = "",
        cardNumber = "",
        cardHolderName = "",
        expiryMonth = 1,
        expiryYear = 2025,
        cvv = "",
        pin = null,
        cardType = CardType.DEBIT,
        cardNetwork = CardNetwork.VISA,
        status = CardStatus.PENDING_ACTIVATION,
        dailyLimit = 5000.0,
        monthlyLimit = 50000.0,
        contactlessEnabled = true,
        onlinePaymentsEnabled = true,
        atmWithdrawalsEnabled = true,
        internationalEnabled = false,
        isVirtual = false,
        createdAt = System.currentTimeMillis(),
        activatedAt = null,
        lastUsedAt = null
    )

    val lastFourDigits: String
        get() = cardNumber.takeLast(4)

    val expiryFormatted: String
        get() = "%02d/%02d".format(expiryMonth, expiryYear % 100)

    val maskedCardNumber: String
        get() = "**** **** **** ${lastFourDigits}"

    val isExpired: Boolean
        get() {
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
            return expiryYear < currentYear || (expiryYear == currentYear && expiryMonth < currentMonth)
        }

    val isActive: Boolean
        get() = status == CardStatus.ACTIVE && !isExpired
}
