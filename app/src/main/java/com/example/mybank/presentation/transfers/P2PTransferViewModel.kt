package com.example.mybank.presentation.transfers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybank.data.firebase.FirestoreService
import com.example.mybank.data.model.Account
import com.example.mybank.data.model.Transaction
import com.example.mybank.data.model.TransactionCategory
import com.example.mybank.data.model.TransactionType
import com.example.mybank.data.model.User
import com.example.mybank.data.repository.AccountRepository
import com.example.mybank.data.repository.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class P2PTransferUiState(
    val senderAccounts: List<Account> = emptyList(),
    val selectedAccount: Account? = null,
    val recipientIdentifier: String = "",
    val recipientUser: User? = null,
    val recipientAccount: Account? = null,
    val amount: Double = 0.0,
    val canTransfer: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchingRecipient: Boolean = false
)

@HiltViewModel
class P2PTransferViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val firestoreService: FirestoreService,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(P2PTransferUiState())
    val uiState: StateFlow<P2PTransferUiState> = _uiState.asStateFlow()

    init {
        loadSenderAccounts()
    }

    private fun loadSenderAccounts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                accountRepository.getUserAccounts(userId)
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = emptyList()
                    )
                    .collect { accounts ->
                        val activeAccounts = accounts.filter { it.isActive }
                        _uiState.value = _uiState.value.copy(
                            senderAccounts = activeAccounts,
                            selectedAccount = activeAccounts.firstOrNull(),
                            isLoading = false
                        )
                        validateTransfer()
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun selectAccount(account: Account) {
        _uiState.value = _uiState.value.copy(selectedAccount = account)
        validateTransfer()
    }

    fun updateRecipientIdentifier(identifier: String) {
        _uiState.value = _uiState.value.copy(
            recipientIdentifier = identifier,
            recipientUser = null,
            recipientAccount = null
        )
        validateTransfer()
    }

    fun searchRecipient() {
        viewModelScope.launch {
            val identifier = _uiState.value.recipientIdentifier.trim()
            if (identifier.isEmpty()) {
                _uiState.value = _uiState.value.copy(error = "Please enter recipient email or phone")
                return@launch
            }

            _uiState.value = _uiState.value.copy(searchingRecipient = true, error = null)

            try {
                val currentUserId = auth.currentUser?.uid
                
                val result = firestoreService.findUserByUsername(identifier)
                
                result.fold(
                    onSuccess = { recipientUser ->
                        if (recipientUser == null) {
                            _uiState.value = _uiState.value.copy(
                                searchingRecipient = false,
                                error = "User not found"
                            )
                            return@launch
                        }
                        
                        if (recipientUser.id == currentUserId) {
                            _uiState.value = _uiState.value.copy(
                                searchingRecipient = false,
                                error = "Cannot transfer to yourself"
                            )
                            return@launch
                        }
                        
                        val accounts = accountRepository.getUserAccounts(recipientUser.id).first()
                        val recipientAccount = accounts.firstOrNull { it.isActive }
                        
                        if (recipientAccount == null) {
                            _uiState.value = _uiState.value.copy(
                                searchingRecipient = false,
                                error = "Recipient has no active accounts"
                            )
                            return@launch
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            recipientUser = recipientUser,
                            recipientAccount = recipientAccount,
                            searchingRecipient = false
                        )
                        validateTransfer()
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            searchingRecipient = false,
                            error = exception.message ?: "Failed to find recipient"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    searchingRecipient = false,
                    error = e.message ?: "Failed to find recipient"
                )
            }
        }
    }

    fun updateAmount(amount: Double) {
        _uiState.value = _uiState.value.copy(amount = amount)
        validateTransfer()
    }

    private fun validateTransfer() {
        val state = _uiState.value
        val canTransfer = state.selectedAccount != null &&
                state.recipientAccount != null &&
                state.amount > 0.0 &&
                state.amount <= (state.selectedAccount?.balance ?: 0.0)
        
        _uiState.value = state.copy(canTransfer = canTransfer)
    }

    fun initiateP2PTransfer(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            if (!state.canTransfer) return@launch

            _uiState.value = state.copy(isLoading = true, error = null)

            try {
                val senderAccount = state.selectedAccount ?: throw IllegalStateException("Sender account is required")
                val recipientAccount = state.recipientAccount ?: throw IllegalStateException("Recipient account is required")
                val recipientUser = state.recipientUser ?: throw IllegalStateException("Recipient user is required")
                val amount = state.amount

                if (amount <= 0) {
                    throw IllegalArgumentException("Amount must be greater than zero")
                }

                if (senderAccount.balance < amount) {
                    throw IllegalArgumentException("Insufficient funds. Available: ${String.format("$%.2f", senderAccount.balance)}")
                }

                val DAILY_TRANSFER_LIMIT = 10000.0
                if (amount > DAILY_TRANSFER_LIMIT) {
                    throw IllegalArgumentException("Transfer amount exceeds daily limit of ${String.format("$%.2f", DAILY_TRANSFER_LIMIT)}")
                }

                val FRAUD_THRESHOLD = 5000.0
                if (amount > FRAUD_THRESHOLD) {
                    val transactions = transactionRepository.getRecentAccountTransactions(senderAccount.id, 10).first()
                    val last24h = transactions.filter {
                        it.timestamp > System.currentTimeMillis() - 24 * 60 * 60 * 1000
                    }
                    val totalTransferred = last24h.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount }
                    if (totalTransferred + amount > DAILY_TRANSFER_LIMIT) {
                        throw IllegalArgumentException("Daily transfer limit exceeded")
                    }
                }

                val transactionId = UUID.randomUUID().toString()

                val result = firestoreService.processTransfer(
                    fromAccountId = senderAccount.id,
                    toAccountId = recipientAccount.id,
                    amount = amount,
                    currency = senderAccount.currency,
                    description = "P2P Transfer to ${recipientUser.name}"
                )

                result.fold(
                    onSuccess = { transferResult ->
                        _uiState.value = state.copy(
                            isLoading = false,
                            amount = 0.0,
                            recipientIdentifier = "",
                            recipientUser = null,
                            recipientAccount = null
                        )
                        onSuccess(transferResult.transactionId ?: transactionId)
                    },
                    onFailure = { exception ->
                        throw exception
                    }
                )
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = e.message ?: "P2P transfer failed"
                )
            }
        }
    }

    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
