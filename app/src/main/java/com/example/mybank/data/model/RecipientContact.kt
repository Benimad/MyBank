package com.example.mybank.data.model

data class RecipientContact(
    val id: String,
    val name: String,
    val username: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val photoUrl: String? = null,
    val accountNumber: String? = null,
    val isRevolutUser: Boolean = false,
    val lastTransferredAt: Long? = null
)
