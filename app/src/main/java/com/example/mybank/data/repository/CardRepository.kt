package com.example.mybank.data.repository

import com.example.mybank.data.firebase.FirestoreService
import com.example.mybank.data.local.dao.CardDao
import com.example.mybank.data.model.Card
import com.example.mybank.data.model.CardStatus
import com.example.mybank.data.preferences.PreferencesManager
import com.example.mybank.data.remote.MyBankApiService
import com.example.mybank.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepository @Inject constructor(
    private val cardDao: CardDao,
    private val apiService: MyBankApiService,
    private val preferencesManager: PreferencesManager,
    private val firestoreService: FirestoreService
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeListeners = mutableMapOf<String, kotlinx.coroutines.Job>()
    
    fun getUserCards(userId: String): Flow<List<Card>> {
        startRealtimeSync(userId)
        return cardDao.getUserCards(userId)
    }
    
    fun getAccountCards(accountId: String): Flow<List<Card>> {
        return cardDao.getAccountCards(accountId)
    }
    
    fun getCard(cardId: String): Flow<Card?> {
        return cardDao.getCard(cardId)
    }
    
    fun getCardsByStatus(userId: String, status: CardStatus): Flow<List<Card>> {
        return cardDao.getCardsByStatus(userId, status)
    }
    
    fun getActiveCards(userId: String): Flow<List<Card>> {
        return cardDao.getActiveCards(userId)
    }
    
    private fun startRealtimeSync(userId: String) {
        if (activeListeners.containsKey(userId)) return
        
        val job = repositoryScope.launch {
            firestoreService.getUserCards(userId).collect { cards ->
                cardDao.insertCards(cards)
            }
        }
        activeListeners[userId] = job
    }
    
    suspend fun syncCards(): Resource<List<Card>> {
        return try {
            val token = preferencesManager.authToken.first()
            if (token == null) {
                return Resource.Error("Authentication token not found")
            }
            
            val response = apiService.getUserCards("Bearer $token")
            if (response.isSuccessful) {
                val body = response.body()?.data
                if (body != null) {
                    cardDao.insertCards(body)
                    Resource.Success(body)
                } else {
                    Resource.Error("Failed to fetch cards")
                }
            } else {
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to sync cards")
        }
    }
    
    suspend fun insertCard(card: Card) {
        cardDao.insertCard(card)
        try {
            firestoreService.createCard(card)
        } catch (e: Exception) {
        }
    }
    
    suspend fun updateCard(card: Card) {
        cardDao.updateCard(card)
        try {
            val updates = mapOf(
                "status" to card.status.name,
                "dailyLimit" to card.dailyLimit,
                "monthlyLimit" to card.monthlyLimit,
                "contactlessEnabled" to card.contactlessEnabled,
                "onlinePaymentsEnabled" to card.onlinePaymentsEnabled,
                "atmWithdrawalsEnabled" to card.atmWithdrawalsEnabled,
                "internationalEnabled" to card.internationalEnabled
            )
            firestoreService.updateCard(card.id, updates)
        } catch (e: Exception) {
        }
    }
    
    suspend fun activateCard(cardId: String): Resource<Unit> {
        return try {
            cardDao.updateCardStatus(cardId, CardStatus.ACTIVE)
            val activatedAt = System.currentTimeMillis()
            val card = cardDao.getCard(cardId).first()
            if (card != null) {
                cardDao.updateCard(card.copy(status = CardStatus.ACTIVE, activatedAt = activatedAt))
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to activate card")
        }
    }
    
    suspend fun freezeCard(cardId: String): Resource<Unit> {
        return try {
            cardDao.updateCardStatus(cardId, CardStatus.FROZEN)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to freeze card")
        }
    }
    
    suspend fun unfreezeCard(cardId: String): Resource<Unit> {
        return try {
            cardDao.updateCardStatus(cardId, CardStatus.ACTIVE)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to unfreeze card")
        }
    }
    
    suspend fun blockCard(cardId: String): Resource<Unit> {
        return try {
            cardDao.updateCardStatus(cardId, CardStatus.BLOCKED)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to block card")
        }
    }
    
    suspend fun updateCardLimits(cardId: String, dailyLimit: Double, monthlyLimit: Double): Resource<Unit> {
        return try {
            cardDao.updateDailyLimit(cardId, dailyLimit)
            cardDao.updateMonthlyLimit(cardId, monthlyLimit)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update card limits")
        }
    }
    
    suspend fun updateContactlessEnabled(cardId: String, enabled: Boolean): Resource<Unit> {
        return try {
            cardDao.updateContactlessEnabled(cardId, enabled)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update contactless setting")
        }
    }
    
    suspend fun updateOnlinePaymentsEnabled(cardId: String, enabled: Boolean): Resource<Unit> {
        return try {
            cardDao.updateOnlinePaymentsEnabled(cardId, enabled)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update online payments setting")
        }
    }
    
    suspend fun updateATMWithdrawalsEnabled(cardId: String, enabled: Boolean): Resource<Unit> {
        return try {
            cardDao.updateATMWithdrawalsEnabled(cardId, enabled)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update ATM withdrawals setting")
        }
    }
    
    suspend fun updateInternationalEnabled(cardId: String, enabled: Boolean): Resource<Unit> {
        return try {
            cardDao.updateInternationalEnabled(cardId, enabled)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update international setting")
        }
    }
    
    suspend fun deleteCard(cardId: String): Resource<Unit> {
        return try {
            cardDao.deleteCardById(cardId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete card")
        }
    }
}
