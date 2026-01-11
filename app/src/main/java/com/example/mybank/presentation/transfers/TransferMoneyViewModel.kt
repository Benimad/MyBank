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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransferMoneyUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val recipientUser: User? = null,
    val recipientAccount: Account? = null,
    val senderAccounts: List<Account> = emptyList(),
    val selectedSenderAccount: Account? = null,
    val amount: String = "",
    val note: String = "",
    val canTransfer: Boolean = false,
    val lastTransaction: Transaction? = null,
    val transferSuccess: Boolean = false
)

@HiltViewModel
class TransferMoneyViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestoreService: FirestoreService,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransferMoneyUiState())
    val uiState: StateFlow<TransferMoneyUiState> = _uiState.asStateFlow()

    private val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    init {
        loadSenderAccounts()
    }

    private fun loadSenderAccounts() {
        viewModelScope.launch {
            try {
                val userId = currentUserId ?: return@launch
                
                accountRepository.getUserAccounts(userId).collect { accounts ->
                    val activeAccounts = accounts.filter { it.isActive }
                    _uiState.value = _uiState.value.copy(
                        senderAccounts = activeAccounts,
                        selectedSenderAccount = activeAccounts.firstOrNull()
                    )
                    validateTransfer()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load accounts: ${e.message}"
                )
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            recipientUser = null,
            recipientAccount = null
        )
        validateTransfer()
    }

    fun searchUserByUsername() {
        viewModelScope.launch {
            val query = _uiState.value.searchQuery.trim()
            
            if (query.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    error = "Please enter a username"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isSearching = true,
                error = null,
                recipientUser = null,
                recipientAccount = null
            )

            try {
                val result = firestoreService.findUserByUsername(query)

                result.fold(
                    onSuccess = { user ->
                        if (user == null) {
                            _uiState.value = _uiState.value.copy(
                                isSearching = false,
                                error = "User '$query' not found"
                            )
                            return@launch
                        }

                        // Check if trying to transfer to self
                        if (user.id == currentUserId) {
                            _uiState.value = _uiState.value.copy(
                                isSearching = false,
                                error = "Cannot transfer to yourself"
                            )
                            return@launch
                        }

                        // Get recipient's active account
                        val recipientAccounts = accountRepository.getUserAccounts(user.id).first()
                        val recipientAccount = recipientAccounts.firstOrNull { it.isActive }

                        if (recipientAccount == null) {
                            _uiState.value = _uiState.value.copy(
                                isSearching = false,
                                error = "${user.name} has no active account"
                            )
                            return@launch
                        }

                        _uiState.value = _uiState.value.copy(
                            isSearching = false,
                            recipientUser = user,
                            recipientAccount = recipientAccount
                        )
                        validateTransfer()
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isSearching = false,
                            error = exception.message ?: "Failed to find user"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    error = "Search failed: ${e.message}"
                )
            }
        }
    }

    fun selectSenderAccount(account: Account) {
        _uiState.value = _uiState.value.copy(selectedSenderAccount = account)
        validateTransfer()
    }

    fun setAmount(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
        validateTransfer()
    }

    fun setNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    private fun validateTransfer() {
        val state = _uiState.value
        val amountValue = state.amount.toDoubleOrNull() ?: 0.0
        
        val canTransfer = state.selectedSenderAccount != null &&
                state.recipientAccount != null &&
                amountValue > 0.0 &&
                amountValue <= (state.selectedSenderAccount?.balance ?: 0.0)
        
        _uiState.value = state.copy(canTransfer = canTransfer)
    }

    fun executeTransfer(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            
            if (!state.canTransfer) {
                _uiState.value = state.copy(error = "Cannot process transfer")
                return@launch
            }

            val senderAccount = state.selectedSenderAccount ?: return@launch
            val recipientAccount = state.recipientAccount ?: return@launch
            val recipientUser = state.recipientUser ?: return@launch
            val amount = state.amount.toDoubleOrNull() ?: return@launch

            _uiState.value = state.copy(isLoading = true, error = null)

            try {
                // Validate amount
                if (amount <= 0) {
                    throw IllegalArgumentException("Amount must be greater than zero")
                }

                if (senderAccount.balance < amount) {
                    throw IllegalArgumentException(
                        "Insufficient funds. Available: ${String.format("$%.2f", senderAccount.balance)}"
                    )
                }

                // Check daily limit
                val DAILY_TRANSFER_LIMIT = 10000.0
                if (amount > DAILY_TRANSFER_LIMIT) {
                    throw IllegalArgumentException(
                        "Transfer amount exceeds daily limit of ${String.format("$%.2f", DAILY_TRANSFER_LIMIT)}"
                    )
                }

                // Process transfer using Cloud Function
                val description = if (state.note.isNotBlank()) {
                    state.note
                } else {
                    "Transfer to ${recipientUser.name}"
                }

                val result = firestoreService.processTransfer(
                    fromAccountId = senderAccount.id,
                    toAccountId = recipientAccount.id,
                    amount = amount,
                    currency = senderAccount.currency,
                    description = description,
                    idempotencyKey = "TRF-${System.currentTimeMillis()}-${java.util.UUID.randomUUID()}"
                )

                result.fold(
                    onSuccess = { transferResult ->
                        val transactionId = transferResult.transactionId ?: ""
                        
                        // Create local transaction record
                        val transaction = Transaction(
                            id = transactionId,
                            accountId = senderAccount.id,
                            userId = currentUserId ?: "",
                            type = TransactionType.DEBIT,
                            category = TransactionCategory.TRANSFER,
                            amount = amount,
                            currency = senderAccount.currency,
                            description = description,
                            recipientName = recipientUser.name,
                            recipientAccount = recipientAccount.accountNumber,
                            timestamp = System.currentTimeMillis(),
                            status = "COMPLETED",
                            balanceAfter = transferResult.fromBalance
                        )

                        transactionRepository.insertTransaction(transaction)

                        _uiState.value = state.copy(
                            isLoading = false,
                            transferSuccess = true,
                            lastTransaction = transaction,
                            amount = "",
                            note = "",
                            searchQuery = "",
                            recipientUser = null,
                            recipientAccount = null
                        )

                        onSuccess(transactionId)
                    },
                    onFailure = { exception ->
                        _uiState.value = state.copy(
                            isLoading = false,
                            error = exception.message ?: "Transfer failed"
                        )
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

    fun resetTransferState() {
        _uiState.value = _uiState.value.copy(
            transferSuccess = false,
            lastTransaction = null,
            amount = "",
            note = "",
            searchQuery = "",
            recipientUser = null,
            recipientAccount = null
        )
    }
}
