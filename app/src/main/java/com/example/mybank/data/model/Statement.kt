package com.example.mybank.data.model

data class Statement(
    val id: String,
    val period: String,
    val generatedDate: Long,
    val totalTransactions: Int,
    val totalSpent: Double,
    val totalIncome: Double,
    val fileUrl: String
)