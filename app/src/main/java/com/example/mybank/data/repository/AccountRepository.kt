package com.example.mybank.data.repository

import com.example.mybank.data.firebase.FirestoreService
import com.example.mybank.data.local.dao.AccountDao
import com.example.mybank.data.model.Account
import com.example.mybank.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val firestoreService: FirestoreService
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeListeners = mutableMapOf<String, kotlinx.coroutines.Job>()

    // Cleanup method to stop all listeners (call on logout)
    fun stopAllListeners() {
        activeListeners.values.forEach { it.cancel() }
        activeListeners.clear()
    }

    // Cleanup method to stop listener for specific user (call on logout)
    fun stopUserListener(userId: String) {
        activeListeners[userId]?.cancel()
        activeListeners.remove(userId)
    }
    
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
    
    // Sync accounts from Firestore - Accounts are synced in real-time via listeners
    // This method is deprecated as accounts sync automatically through real-time listeners
    @Deprecated("Accounts sync automatically via real-time Firestore listeners", ReplaceWith(""))
    suspend fun syncAccounts(): Resource<List<Account>> {
        return try {
            // Force local refresh - real-time listeners will update from Firestore
            Resource.Success(emptyList())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to refresh accounts")
        }
    }

    // Get account from Firestore (deprecated - use real-time listener instead)
    @Deprecated("Accounts sync automatically via real-time Firestore listeners", ReplaceWith(""))
    suspend fun fetchAccountFromApi(accountId: String): Resource<Account> {
        return try {
            val account = firestoreService.getAccount(accountId)
            if (account != null) {
                accountDao.insertAccount(account)
                Resource.Success(account)
            } else {
                Resource.Error("Account not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch account")
        }
    }
    
    suspend fun insertAccount(account: Account) {
        accountDao.insertAccount(account)
    }
}
