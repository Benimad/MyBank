package com.example.mybank.presentation.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybank.data.model.Card
import com.example.mybank.data.model.CardNetwork
import com.example.mybank.data.model.CardStatus
import com.example.mybank.data.model.CardType
import com.example.mybank.data.repository.AccountRepository
import com.example.mybank.data.repository.CardRepository
import com.example.mybank.util.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class CreateCardUiState(
    val accountId: String = "",
    val cardType: CardType = CardType.DEBIT,
    val cardNetwork: CardNetwork = CardNetwork.VISA,
    val isVirtual: Boolean = false,
    val dailyLimit: Double = 5000.0,
    val monthlyLimit: Double = 50000.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val createdCardId: String? = null
)

@HiltViewModel
class CreateCardViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val accountRepository: AccountRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateCardUiState())
    val uiState: StateFlow<CreateCardUiState> = _uiState.asStateFlow()

    fun setAccountId(accountId: String) {
        _uiState.value = _uiState.value.copy(accountId = accountId)
    }

    fun setCardType(cardType: CardType) {
        _uiState.value = _uiState.value.copy(cardType = cardType)
    }

    fun setCardNetwork(cardNetwork: CardNetwork) {
        _uiState.value = _uiState.value.copy(cardNetwork = cardNetwork)
    }

    fun setIsVirtual(isVirtual: Boolean) {
        _uiState.value = _uiState.value.copy(isVirtual = isVirtual)
    }

    fun setDailyLimit(limit: Double) {
        _uiState.value = _uiState.value.copy(dailyLimit = limit)
    }

    fun setMonthlyLimit(limit: Double) {
        _uiState.value = _uiState.value.copy(monthlyLimit = limit)
    }

    fun createCard(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            val userId = auth.currentUser?.uid ?: run {
                _uiState.value = state.copy(error = "User not authenticated")
                return@launch
            }

            if (state.accountId.isEmpty()) {
                _uiState.value = state.copy(error = "Please select an account")
                return@launch
            }

            _uiState.value = state.copy(isLoading = true, error = null)

            try {
                val account = accountRepository.getAccount(state.accountId).first()
                if (account == null) {
                    _uiState.value = state.copy(
                        isLoading = false,
                        error = "Account not found"
                    )
                    return@launch
                }

                val cardId = UUID.randomUUID().toString()
                val cardNumber = generateCardNumber(state.cardNetwork)
                val cvv = generateCVV()
                val expiryDate = calculateExpiryDate()

                val card = Card(
                    id = cardId,
                    userId = userId,
                    accountId = state.accountId,
                    cardNumber = cardNumber,
                    cardHolderName = account.accountName.uppercase(),
                    expiryMonth = expiryDate.first,
                    expiryYear = expiryDate.second,
                    cvv = cvv,
                    cardType = state.cardType,
                    cardNetwork = state.cardNetwork,
                    status = if (state.isVirtual) CardStatus.ACTIVE else CardStatus.PENDING_ACTIVATION,
                    dailyLimit = state.dailyLimit,
                    monthlyLimit = state.monthlyLimit,
                    isVirtual = state.isVirtual,
                    createdAt = System.currentTimeMillis(),
                    activatedAt = if (state.isVirtual) System.currentTimeMillis() else null
                )

                cardRepository.insertCard(card)

                _uiState.value = state.copy(
                    isLoading = false,
                    createdCardId = cardId
                )
                onSuccess(cardId)
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to create card"
                )
            }
        }
    }

    private fun generateCardNumber(network: CardNetwork): String {
        val prefix = when (network) {
            CardNetwork.VISA -> "4"
            CardNetwork.MASTERCARD -> "5"
            CardNetwork.AMEX -> "37"
            CardNetwork.DISCOVER -> "6011"
        }
        
        val random = Random()
        val length = if (network == CardNetwork.AMEX) 15 else 16
        val remainingDigits = length - prefix.length
        
        val cardNumber = StringBuilder(prefix)
        repeat(remainingDigits) {
            cardNumber.append(random.nextInt(10))
        }
        
        return cardNumber.toString()
    }

    private fun generateCVV(): String {
        return (100..999).random().toString()
    }

    private fun calculateExpiryDate(): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, 5)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        return Pair(month, year)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
