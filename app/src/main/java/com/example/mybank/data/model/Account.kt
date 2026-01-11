package com.example.mybank.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

enum class AccountType {
    CHECKING,
    SAVINGS,
    CREDIT,
    GOAL,
    INVESTMENT,
    SPENDING
}

enum class PotColorTag {
    BLUE,
    EMERALD,
    PURPLE,
    TEAL,
    AMBER,
    ORANGE,
    PINK
}

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey
    @SerializedName("id")
    val id: String = "",

    @SerializedName("userId")
    val userId: String = "",

    @SerializedName("accountNumber")
    val accountNumber: String = "",

    @SerializedName("accountName")
    val accountName: String = "",

    @SerializedName("accountType")
    val accountType: AccountType = AccountType.CHECKING,

    @SerializedName("balance")
    val balance: Double = 0.0,

    @SerializedName("currency")
    val currency: String = "USD",

    @SerializedName("iban")
    val iban: String? = null,

    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),

    @SerializedName("isActive")
    val isActive: Boolean = true,

    @SerializedName("potColorTag")
    val potColorTag: PotColorTag? = null,

    @SerializedName("goalAmount")
    val goalAmount: Double? = null,

    @SerializedName("goalDeadline")
    val goalDeadline: Long? = null,

    @SerializedName("tagLabel")
    val tagLabel: String? = null
) {
    // Empty constructor for Firebase deserialization
    constructor() : this(
        id = "",
        userId = "",
        accountNumber = "",
        accountName = "",
        accountType = AccountType.CHECKING,
        balance = 0.0,
        currency = "USD",
        iban = null,
        createdAt = System.currentTimeMillis(),
        isActive = true,
        potColorTag = null,
        goalAmount = null,
        goalDeadline = null,
        tagLabel = null
    )

    val progress: Float
        get() = if (goalAmount != null && goalAmount > 0) {
            (balance / goalAmount).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        }
}
