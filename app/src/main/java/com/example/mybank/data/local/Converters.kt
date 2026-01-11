package com.example.mybank.data.local

import androidx.room.TypeConverter
import com.example.mybank.data.model.AccountType
import com.example.mybank.data.model.BillerCategory
import com.example.mybank.data.model.CardNetwork
import com.example.mybank.data.model.CardStatus
import com.example.mybank.data.model.CardType
import com.example.mybank.data.model.NotificationType
import com.example.mybank.data.model.PaymentStatus
import com.example.mybank.data.model.PotColorTag
import com.example.mybank.data.model.RecurrenceFrequency
import com.example.mybank.data.model.TransactionCategory
import com.example.mybank.data.model.TransactionType

class Converters {
    
    @TypeConverter
    fun fromAccountType(value: AccountType): String {
        return value.name
    }
    
    @TypeConverter
    fun toAccountType(value: String): AccountType {
        return AccountType.valueOf(value)
    }
    
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }
    
    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }
    
    @TypeConverter
    fun fromTransactionCategory(value: TransactionCategory): String {
        return value.name
    }
    
    @TypeConverter
    fun toTransactionCategory(value: String): TransactionCategory {
        return TransactionCategory.valueOf(value)
    }
    
    @TypeConverter
    fun fromNotificationType(value: NotificationType): String {
        return value.name
    }
    
    @TypeConverter
    fun toNotificationType(value: String): NotificationType {
        return NotificationType.valueOf(value)
    }
    
    @TypeConverter
    fun fromCardType(value: CardType): String {
        return value.name
    }
    
    @TypeConverter
    fun toCardType(value: String): CardType {
        return CardType.valueOf(value)
    }
    
    @TypeConverter
    fun fromCardStatus(value: CardStatus): String {
        return value.name
    }
    
    @TypeConverter
    fun toCardStatus(value: String): CardStatus {
        return CardStatus.valueOf(value)
    }
    
    @TypeConverter
    fun fromCardNetwork(value: CardNetwork): String {
        return value.name
    }
    
    @TypeConverter
    fun toCardNetwork(value: String): CardNetwork {
        return CardNetwork.valueOf(value)
    }
    
    @TypeConverter
    fun fromBillerCategory(value: BillerCategory): String {
        return value.name
    }
    
    @TypeConverter
    fun toBillerCategory(value: String): BillerCategory {
        return BillerCategory.valueOf(value)
    }
    
    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus): String {
        return value.name
    }
    
    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus {
        return PaymentStatus.valueOf(value)
    }
    
    @TypeConverter
    fun fromRecurrenceFrequency(value: RecurrenceFrequency): String {
        return value.name
    }
    
    @TypeConverter
    fun toRecurrenceFrequency(value: String): RecurrenceFrequency {
        return RecurrenceFrequency.valueOf(value)
    }
    
    @TypeConverter
    fun fromPotColorTag(value: PotColorTag?): String? {
        return value?.name
    }
    
    @TypeConverter
    fun toPotColorTag(value: String?): PotColorTag? {
        return value?.let { PotColorTag.valueOf(it) }
    }
}
