package com.example.mybank.data.model

import com.google.firebase.firestore.DocumentId

data class UserProfile(
    @DocumentId
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val isPremium: Boolean = false,
    val isFaceIdEnabled: Boolean = false,
    val is2FAEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
)
