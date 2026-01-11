package com.example.mybank.data.remote.dto

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String
)
