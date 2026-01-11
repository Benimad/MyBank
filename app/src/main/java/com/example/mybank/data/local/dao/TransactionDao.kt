package com.example.mybank.data.local.dao

import androidx.room.*
import com.example.mybank.data.model.Transaction
import com.example.mybank.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY timestamp DESC")
    fun getAccountTransactions(accountId: String): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentAccountTransactions(accountId: String, limit: Int = 10): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    fun getTransaction(transactionId: String): Flow<Transaction?>
    
    @Query("""
        SELECT * FROM transactions 
        WHERE accountId IN (SELECT id FROM accounts WHERE userId = :userId) 
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    fun getRecentUserTransactions(userId: String, limit: Int = 20): Flow<List<Transaction>>
    
    @Query("""
        SELECT * FROM transactions 
        WHERE accountId IN (SELECT id FROM accounts WHERE userId = :userId) 
        AND timestamp BETWEEN :startTime AND :endTime
        ORDER BY timestamp DESC
    """)
    fun getUserTransactionsByDateRange(userId: String, startTime: Long, endTime: Long): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions 
        WHERE accountId = :accountId 
        AND timestamp BETWEEN :startTime AND :endTime 
        ORDER BY timestamp DESC
    """)
    fun getTransactionsByDateRange(
        accountId: String,
        startTime: Long,
        endTime: Long
    ): Flow<List<Transaction>>
    
    @Query("""
        SELECT * FROM transactions 
        WHERE accountId = :accountId 
        AND type = :type 
        ORDER BY timestamp DESC
    """)
    fun getTransactionsByType(accountId: String, type: TransactionType): Flow<List<Transaction>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<Transaction>)
    
    @Update
    suspend fun updateTransaction(transaction: Transaction)
    
    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransaction(transactionId: String)
    
    @Query("DELETE FROM transactions WHERE accountId = :accountId")
    suspend fun deleteAccountTransactions(accountId: String)
    
    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}
