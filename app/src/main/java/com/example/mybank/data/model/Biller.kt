package com.example.mybank.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

enum class BillerCategory {
    UTILITIES,
    TELECOM,
    INSURANCE,
    SUBSCRIPTION,
    LOAN,
    CREDIT_CARD,
    EDUCATION,
    HEALTHCARE,
    GOVERNMENT,
    OTHER
}

@Entity(tableName = "billers")
data class Biller(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("billerName")
    val billerName: String,
    
    @SerializedName("category")
    val category: BillerCategory,
    
    @SerializedName("accountNumber")
    val accountNumber: String,
    
    @SerializedName("billerCode")
    val billerCode: String? = null,
    
    @SerializedName("logoUrl")
    val logoUrl: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("isActive")
    val isActive: Boolean = true,
    
    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis()
)
