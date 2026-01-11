package com.example.mybank.data.local.dao

import androidx.room.*
import com.example.mybank.data.model.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE userId = :userId AND isActive = 1")
    fun getUserAccounts(userId: String): Flow<List<Account>>
    
    @Query("SELECT * FROM accounts WHERE id = :accountId")
    fun getAccount(accountId: String): Flow<Account?>
    
    @Query("SELECT * FROM accounts WHERE id = :accountId")
    suspend fun getAccountSync(accountId: String): Account?
    
    @Query("SELECT SUM(balance) FROM accounts WHERE userId = :userId AND isActive = 1")
    fun getTotalBalance(userId: String): Flow<Double?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<Account>)
    
    @Update
    suspend fun updateAccount(account: Account)
    
    @Query("DELETE FROM accounts WHERE id = :accountId")
    suspend fun deleteAccount(accountId: String)
    
    @Query("DELETE FROM accounts WHERE userId = :userId")
    suspend fun deleteUserAccounts(userId: String)
    
    @Query("DELETE FROM accounts")
    suspend fun deleteAllAccounts()
    
    @Query("UPDATE accounts SET balance = :newBalance WHERE id = :accountId")
    suspend fun updateAccountBalance(accountId: String, newBalance: Double)
}
