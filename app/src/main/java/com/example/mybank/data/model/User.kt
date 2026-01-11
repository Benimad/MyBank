package com.example.mybank.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @SerializedName("id")
    val id: String = "",

    @SerializedName("email")
    val email: String = "",

    @SerializedName("name")
    val name: String = "",

    // Password hash for offline login (not synced to server)
    val passwordHash: String? = null,

    @SerializedName("username")
    val username: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("profileImageUrl")
    val profileImageUrl: String? = null,

    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),

    // For offline login tracking
    val lastLoginTimestamp: Long = System.currentTimeMillis(),

    // Firebase UID for syncing
    val firebaseUid: String? = null
) {
    // Empty constructor for Firebase deserialization
    constructor() : this(
        id = "",
        email = "",
        name = "",
        passwordHash = null,
        username = null,
        phone = null,
        address = null,
        profileImageUrl = null,
        createdAt = System.currentTimeMillis(),
        lastLoginTimestamp = System.currentTimeMillis(),
        firebaseUid = null
    )
}
