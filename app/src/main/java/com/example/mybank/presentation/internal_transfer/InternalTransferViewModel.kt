package com.example.mybank.presentation.internal_transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybank.data.firebase.FirestoreService
import com.example.mybank.data.model.Account
import com.example.mybank.data.model.Transaction
import com.example.mybank.data.model.TransactionCategory
import com.example.mybank.data.model.TransactionType
import com.example.mybank.data.repository.AccountRepository
import com.example.mybank.data.repository.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class InternalTransferUiState(
    val accounts: List<Account> = emptyList(),
    val fromAccount: Account? = null,
    val toAccount: Account? = null,
    val amount: Double = 0.0,
    val canTransfer: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class InternalTransferViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val firestoreService: FirestoreService,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(InternalTransferUiState())
    val uiState: StateFlow<InternalTransferUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                accountRepository.getUserAccounts(userId).collect { accounts ->
                    val activeAccounts = accounts.filter { it.isActive }
                    _uiState.value = _uiState.value.copy(
                        accounts = activeAccounts,
                        fromAccount = activeAccounts.firstOrNull(),
                        toAccount = activeAccounts.getOrNull(1),
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

    fun selectFromAccount(account: Account) {
        _uiState.value = _uiState.value.copy(fromAccount = account)
        validateTransfer()
    }

    fun selectToAccount(account: Account) {
        _uiState.value = _uiState.value.copy(toAccount = account)
        validateTransfer()
    }

    fun updateAmount(amount: Double) {
        _uiState.value = _uiState.value.copy(amount = amount)
        validateTransfer()
    }

    fun swapAccounts() {
        val from = _uiState.value.fromAccount
        val to = _uiState.value.toAccount
        _uiState.value = _uiState.value.copy(
            fromAccount = to,
            toAccount = from
        )
        validateTransfer()
    }

    private fun validateTransfer() {
        val state = _uiState.value
        val canTransfer = state.fromAccount != null &&
                state.toAccount != null &&
                state.fromAccount?.id != state.toAccount?.id &&
                state.amount > 0.0 &&
                state.amount <= (state.fromAccount?.balance ?: 0.0)
        
        _uiState.value = state.copy(canTransfer = canTransfer)
    }

    fun initiateTransfer(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            if (!state.canTransfer) return@launch

            _uiState.value = state.copy(isLoading = true, error = null)

            try {
                val fromAccount = state.fromAccount ?: throw IllegalStateException("Source account is required")
                val toAccount = state.toAccount ?: throw IllegalStateException("Destination account is required")
                val amount = state.amount

                if (amount <= 0) {
                    throw IllegalArgumentException("Amount must be greater than zero")
                }

                if (fromAccount.id == toAccount.id) {
                    throw IllegalArgumentException("Cannot transfer to the same account")
                }

                if (fromAccount.balance < amount) {
                    throw IllegalArgumentException("Insufficient funds. Available: ${String.format("$%.2f", fromAccount.balance)}")
                }

                val DAILY_TRANSFER_LIMIT = 10000.0
                if (amount > DAILY_TRANSFER_LIMIT) {
                    throw IllegalArgumentException("Transfer amount exceeds daily limit of ${String.format("$%.2f", DAILY_TRANSFER_LIMIT)}")
                }

                val FRAUD_THRESHOLD = 5000.0
                if (amount > FRAUD_THRESHOLD) {
                    // FIXED: Use .first() instead of .collect() to get transactions
                    val recentTransfers = transactionRepository.getRecentAccountTransactions(fromAccount.id, 10).first()
                    val last24h = recentTransfers.filter {
                        it.timestamp > System.currentTimeMillis() - 24 * 60 * 60 * 1000
                    }
                    val totalTransferred = last24h.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount }
                    if (totalTransferred + amount > DAILY_TRANSFER_LIMIT) {
                        throw IllegalArgumentException("Daily transfer limit exceeded")
                    }
                }

                val transactionId = UUID.randomUUID().toString()
                val timestamp = System.currentTimeMillis()
                val userId = auth.currentUser?.uid ?: "unknown"

                val newFromBalance = fromAccount.balance - amount
                val newToBalance = toAccount.balance + amount

                val result = firestoreService.processTransfer(
                    fromAccountId = fromAccount.id,
                    toAccountId = toAccount.id,
                    amount = amount,
                    currency = fromAccount.currency,
                    description = "Transfer to ${toAccount.accountName}"
                )

                result.fold(
                    onSuccess = { transferResult ->
                        _uiState.value = state.copy(isLoading = false, amount = 0.0)
                        onSuccess(transferResult.transactionId ?: transactionId)
                    },
                    onFailure = { exception ->
                        throw exception
                    }
                )
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = e.message ?: "Transfer failed"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
