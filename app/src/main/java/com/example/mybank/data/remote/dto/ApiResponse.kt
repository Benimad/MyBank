package com.example.mybank.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: T? = null,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("error")
    val error: String? = null
)

data class AuthResponse(
    @SerializedName("token")
    val token: String,
    
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("name")
    val name: String
)
