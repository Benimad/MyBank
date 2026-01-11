package com.example.mybank.data.remote

import com.example.mybank.data.model.Account
import com.example.mybank.data.model.BankNotification
import com.example.mybank.data.model.Transaction
import com.example.mybank.data.model.User
import com.example.mybank.data.remote.dto.ApiResponse
import com.example.mybank.data.remote.dto.AuthResponse
import com.example.mybank.data.remote.dto.LoginRequest
import com.example.mybank.data.remote.dto.RegisterRequest
import retrofit2.Response
import retrofit2.http.*

interface MyBankApiService {
    
    // Authentication endpoints
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>
    
    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<ApiResponse<Unit>>
    
    // User endpoints
    @GET("users/{userId}")
    suspend fun getUser(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<ApiResponse<User>>
    
    @PUT("users/{userId}")
    suspend fun updateUser(
        @Path("userId") userId: String,
        @Header("Authorization") token: String,
        @Body user: User
    ): Response<ApiResponse<User>>
    
    // Account endpoints
    @GET("accounts")
    suspend fun getUserAccounts(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<Account>>>
    
    @GET("accounts/{accountId}")
    suspend fun getAccount(
        @Path("accountId") accountId: String,
        @Header("Authorization") token: String
    ): Response<ApiResponse<Account>>
    
    // Transaction endpoints
    @GET("accounts/{accountId}/transactions")
    suspend fun getAccountTransactions(
        @Path("accountId") accountId: String,
        @Header("Authorization") token: String,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): Response<ApiResponse<List<Transaction>>>
    
    @GET("transactions")
    suspend fun getAllTransactions(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int? = null
    ): Response<ApiResponse<List<Transaction>>>
    
    @GET("transactions/{transactionId}")
    suspend fun getTransaction(
        @Path("transactionId") transactionId: String,
        @Header("Authorization") token: String
    ): Response<ApiResponse<Transaction>>
    
    // Notification endpoints
    @GET("notifications")
    suspend fun getNotifications(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<BankNotification>>>
    
    @PUT("notifications/{notificationId}/read")
    suspend fun markNotificationAsRead(
        @Path("notificationId") notificationId: String,
        @Header("Authorization") token: String
    ): Response<ApiResponse<Unit>>
    
    // FCM Token registration
    @POST("fcm/register")
    suspend fun registerFcmToken(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<ApiResponse<Unit>>
    
    // Card endpoints
    @GET("cards")
    suspend fun getUserCards(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<com.example.mybank.data.model.Card>>>
}
