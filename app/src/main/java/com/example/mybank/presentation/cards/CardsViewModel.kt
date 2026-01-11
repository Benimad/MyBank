package com.example.mybank.presentation.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybank.data.model.Card
import com.example.mybank.data.model.CardStatus
import com.example.mybank.data.repository.CardRepository
import com.example.mybank.util.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CardsUiState(
    val cards: List<Card> = emptyList(),
    val selectedCard: Card? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class CardsViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardsUiState())
    val uiState: StateFlow<CardsUiState> = _uiState.asStateFlow()

    init {
        loadCards()
    }

    fun loadCards() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                cardRepository.getUserCards(userId)
                    .collect { cards ->
                        _uiState.value = _uiState.value.copy(
                            cards = cards,
                            selectedCard = cards.firstOrNull { it.isActive } ?: cards.firstOrNull(),
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load cards"
                )
            }
        }
    }

    fun selectCard(card: Card) {
        _uiState.value = _uiState.value.copy(selectedCard = card)
    }

    fun freezeCard(cardId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = cardRepository.freezeCard(cardId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Card frozen successfully"
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun unfreezeCard(cardId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = cardRepository.unfreezeCard(cardId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Card unfrozen successfully"
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun blockCard(cardId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = cardRepository.blockCard(cardId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Card blocked successfully"
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun updateCardLimits(cardId: String, dailyLimit: Double, monthlyLimit: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = cardRepository.updateCardLimits(cardId, dailyLimit, monthlyLimit)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Card limits updated successfully"
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun updateContactlessEnabled(cardId: String, enabled: Boolean) {
        viewModelScope.launch {
            when (cardRepository.updateContactlessEnabled(cardId, enabled)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Contactless ${if (enabled) "enabled" else "disabled"}"
                    )
                }
                is Resource.Error -> {}
                else -> {}
            }
        }
    }

    fun updateOnlinePaymentsEnabled(cardId: String, enabled: Boolean) {
        viewModelScope.launch {
            when (cardRepository.updateOnlinePaymentsEnabled(cardId, enabled)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Online payments ${if (enabled) "enabled" else "disabled"}"
                    )
                }
                is Resource.Error -> {}
                else -> {}
            }
        }
    }

    fun updateATMWithdrawalsEnabled(cardId: String, enabled: Boolean) {
        viewModelScope.launch {
            when (cardRepository.updateATMWithdrawalsEnabled(cardId, enabled)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "ATM withdrawals ${if (enabled) "enabled" else "disabled"}"
                    )
                }
                is Resource.Error -> {}
                else -> {}
            }
        }
    }

    fun updateInternationalEnabled(cardId: String, enabled: Boolean) {
        viewModelScope.launch {
            when (cardRepository.updateInternationalEnabled(cardId, enabled)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "International transactions ${if (enabled) "enabled" else "disabled"}"
                    )
                }
                is Resource.Error -> {}
                else -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}
