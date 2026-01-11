package com.example.mybank.data.repository

import com.example.mybank.data.firebase.FirestoreService
import com.example.mybank.data.local.dao.AccountDao
import com.example.mybank.data.model.Account
import com.example.mybank.data.preferences.PreferencesManager
import com.example.mybank.data.remote.MyBankApiService
import com.example.mybank.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val apiService: MyBankApiService,
    private val preferencesManager: PreferencesManager,
    private val firestoreService: FirestoreService
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeListeners = mutableMapOf<String, kotlinx.coroutines.Job>()
    
    // Get accounts from local database (offline-first)
    fun getUserAccounts(userId: String): Flow<List<Account>> {
        startRealtimeSync(userId)
        return accountDao.getUserAccounts(userId)
    }
    
    fun getAccount(accountId: String): Flow<Account?> {
        return accountDao.getAccount(accountId)
    }
    
    fun getTotalBalance(userId: String): Flow<Double?> {
        return accountDao.getTotalBalance(userId)
    }
    
    private fun startRealtimeSync(userId: String) {
        if (activeListeners.containsKey(userId)) return
        
        val job = repositoryScope.launch {
            firestoreService.getUserAccounts(userId).collect { accounts ->
                accountDao.insertAccounts(accounts)
            }
        }
        activeListeners[userId] = job
    }
    
    // Sync accounts from API and update local database
    suspend fun syncAccounts(): Resource<List<Account>> {
        return try {
            val token = preferencesManager.authToken.first()
            if (token == null) {
                return Resource.Error("Authentication token not found")
            }
            
            val response = apiService.getUserAccounts("Bearer $token")
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    // Save to local database
                    accountDao.insertAccounts(apiResponse.data)
                    Resource.Success(apiResponse.data)
                } else {
                    Resource.Error(apiResponse?.message ?: "Failed to fetch accounts")
                }
            } else {
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to sync accounts")
        }
    }
    
    // Get account from API
    suspend fun fetchAccountFromApi(accountId: String): Resource<Account> {
        return try {
            val token = preferencesManager.authToken.first()
            if (token == null) {
                return Resource.Error("Authentication token not found")
            }
            
            val response = apiService.getAccount(accountId, "Bearer $token")
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    // Update local database
                    accountDao.insertAccount(apiResponse.data)
                    Resource.Success(apiResponse.data)
                } else {
                    Resource.Error(apiResponse?.message ?: "Failed to fetch account")
                }
            } else {
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch account")
        }
    }
    
    suspend fun updateAccountBalance(accountId: String, newBalance: Double) {
        accountDao.updateAccountBalance(accountId, newBalance)
        try {
            firestoreService.updateAccountBalance(accountId, newBalance)
        } catch (e: Exception) {
        }
    }
    
    suspend fun insertAccount(account: Account) {
        accountDao.insertAccount(account)
    }
}
