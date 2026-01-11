package com.example.mybank.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class UserProfile(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val isPremium: Boolean = false,
    val isFaceIdEnabled: Boolean = false,
    val is2FAEnabled: Boolean = true,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val lastUpdated: Timestamp? = null
)
