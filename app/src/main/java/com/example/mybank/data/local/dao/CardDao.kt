package com.example.mybank.data.local.dao

import androidx.room.*
import com.example.mybank.data.model.Card
import com.example.mybank.data.model.CardStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    
    @Query("SELECT * FROM cards WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserCards(userId: String): Flow<List<Card>>
    
    @Query("SELECT * FROM cards WHERE accountId = :accountId ORDER BY createdAt DESC")
    fun getAccountCards(accountId: String): Flow<List<Card>>
    
    @Query("SELECT * FROM cards WHERE id = :cardId")
    fun getCard(cardId: String): Flow<Card?>
    
    @Query("SELECT * FROM cards WHERE userId = :userId AND status = :status ORDER BY createdAt DESC")
    fun getCardsByStatus(userId: String, status: CardStatus): Flow<List<Card>>
    
    @Query("SELECT * FROM cards WHERE userId = :userId AND status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getActiveCards(userId: String): Flow<List<Card>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: Card)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<Card>)
    
    @Update
    suspend fun updateCard(card: Card)
    
    @Query("UPDATE cards SET status = :status WHERE id = :cardId")
    suspend fun updateCardStatus(cardId: String, status: CardStatus)
    
    @Query("UPDATE cards SET dailyLimit = :limit WHERE id = :cardId")
    suspend fun updateDailyLimit(cardId: String, limit: Double)
    
    @Query("UPDATE cards SET monthlyLimit = :limit WHERE id = :cardId")
    suspend fun updateMonthlyLimit(cardId: String, limit: Double)
    
    @Query("UPDATE cards SET contactlessEnabled = :enabled WHERE id = :cardId")
    suspend fun updateContactlessEnabled(cardId: String, enabled: Boolean)
    
    @Query("UPDATE cards SET onlinePaymentsEnabled = :enabled WHERE id = :cardId")
    suspend fun updateOnlinePaymentsEnabled(cardId: String, enabled: Boolean)
    
    @Query("UPDATE cards SET atmWithdrawalsEnabled = :enabled WHERE id = :cardId")
    suspend fun updateATMWithdrawalsEnabled(cardId: String, enabled: Boolean)
    
    @Query("UPDATE cards SET internationalEnabled = :enabled WHERE id = :cardId")
    suspend fun updateInternationalEnabled(cardId: String, enabled: Boolean)
    
    @Query("UPDATE cards SET lastUsedAt = :timestamp WHERE id = :cardId")
    suspend fun updateLastUsed(cardId: String, timestamp: Long)
    
    @Delete
    suspend fun deleteCard(card: Card)
    
    @Query("DELETE FROM cards WHERE id = :cardId")
    suspend fun deleteCardById(cardId: String)
    
    @Query("DELETE FROM cards WHERE userId = :userId")
    suspend fun deleteUserCards(userId: String)
}
