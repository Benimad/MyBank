package com.example.mybank.data.repository

import com.example.mybank.data.firebase.FirestoreService
import com.example.mybank.data.local.dao.TransactionDao
import com.example.mybank.data.model.Transaction
import com.example.mybank.data.model.TransactionType
import com.example.mybank.util.Resource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val firestoreService: FirestoreService,
    private val auth: FirebaseAuth
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeAccountListeners = mutableMapOf<String, kotlinx.coroutines.Job>()
    private val activeUserListeners = mutableMapOf<String, kotlinx.coroutines.Job>()

    // Cleanup methods to prevent memory leaks
    fun stopAllListeners() {
        activeAccountListeners.values.forEach { it.cancel() }
        activeUserListeners.values.forEach { it.cancel() }
        activeAccountListeners.clear()
        activeUserListeners.clear()
    }

    fun stopAccountListener(accountId: String) {
        activeAccountListeners[accountId]?.cancel()
        activeAccountListeners.remove(accountId)
    }

    fun stopUserListener(userId: String) {
        activeUserListeners[userId]?.cancel()
        activeUserListeners.remove(userId)
    }
    
    // Get transactions from local database (offline-first)
    fun getAccountTransactions(accountId: String): Flow<List<Transaction>> {
        startRealtimeSyncForAccount(accountId)
        return transactionDao.getAccountTransactions(accountId)
    }
    
    fun getRecentAccountTransactions(accountId: String, limit: Int = 10): Flow<List<Transaction>> {
        return transactionDao.getRecentAccountTransactions(accountId, limit)
    }
    
    fun getRecentUserTransactions(userId: String, limit: Int = 20): Flow<List<Transaction>> {
        startRealtimeSyncForUser(userId)
        return transactionDao.getRecentUserTransactions(userId, limit)
    }
    
    private fun startRealtimeSyncForAccount(accountId: String) {
        if (activeAccountListeners.containsKey(accountId)) return
        
        val job = repositoryScope.launch {
            firestoreService.getAccountTransactions(accountId, 50).collect { transactions ->
                transactionDao.insertTransactions(transactions)
            }
        }
        activeAccountListeners[accountId] = job
    }
    
    private fun startRealtimeSyncForUser(userId: String) {
        if (activeUserListeners.containsKey(userId)) return
        
        val job = repositoryScope.launch {
            firestoreService.getUserTransactions(userId, 50).collect { transactions ->
                transactionDao.insertTransactions(transactions)
            }
        }
        activeUserListeners[userId] = job
    }
    
    fun getTransaction(transactionId: String): Flow<Transaction?> {
        return transactionDao.getTransaction(transactionId)
    }
    
    fun getTransactionsByDateRange(
        accountId: String,
        startTime: Long,
        endTime: Long
    ): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByDateRange(accountId, startTime, endTime)
    }
    
    fun getTransactionsByType(accountId: String, type: TransactionType): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByType(accountId, type)
    }
    
    // Sync transactions from API - DEPRECATED: Transactions sync automatically via real-time Firestore listeners
    @Deprecated("Transactions sync automatically via real-time Firestore listeners", ReplaceWith(""))
    suspend fun syncAccountTransactions(accountId: String, limit: Int? = null): Resource<List<Transaction>> {
        return try {
            // Force local refresh - real-time listeners will update from Firestore
            Resource.Success(emptyList())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to refresh transactions")
        }
    }

    // Sync all transactions from API - DEPRECATED: Transactions sync automatically via real-time Firestore listeners
    @Deprecated("Transactions sync automatically via real-time Firestore listeners", ReplaceWith(""))
    suspend fun syncAllTransactions(limit: Int? = null): Resource<List<Transaction>> {
        return try {
            // Force local refresh - real-time listeners will update from Firestore
            Resource.Success(emptyList())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to refresh transactions")
        }
    }
    
    suspend fun insertTransaction(transaction: Transaction): Resource<Unit> {
    return try {
        transactionDao.insertTransaction(transaction)
        firestoreService.createTransaction(transaction)
        Resource.Success(Unit)
    } catch (e: Exception) {
        android.util.Log.e("TransactionRepository", "Failed to sync transaction to Firestore", e)
        Resource.Error(e.message ?: "Failed to sync transaction to Firestore")
    }
}
}
