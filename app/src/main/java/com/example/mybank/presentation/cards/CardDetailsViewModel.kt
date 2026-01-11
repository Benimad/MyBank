package com.example.mybank.presentation.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybank.data.model.Card
import com.example.mybank.data.repository.CardRepository
import com.example.mybank.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CardDetailsUiState(
    val card: Card? = null,
    val showCVV: Boolean = false,
    val showPIN: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class CardDetailsViewModel @Inject constructor(
    private val cardRepository: CardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardDetailsUiState())
    val uiState: StateFlow<CardDetailsUiState> = _uiState.asStateFlow()

    fun loadCard(cardId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                cardRepository.getCard(cardId)
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = null
                    )
                    .collect { card ->
                        _uiState.value = _uiState.value.copy(
                            card = card,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load card"
                )
            }
        }
    }

    fun toggleCVVVisibility() {
        _uiState.value = _uiState.value.copy(showCVV = !_uiState.value.showCVV)
    }

    fun togglePINVisibility() {
        _uiState.value = _uiState.value.copy(showPIN = !_uiState.value.showPIN)
    }

    fun activateCard(cardId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = cardRepository.activateCard(cardId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Card activated successfully"
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
                        successMessage = "International payments ${if (enabled) "enabled" else "disabled"}"
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
