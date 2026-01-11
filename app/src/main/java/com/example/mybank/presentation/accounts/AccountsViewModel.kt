package com.example.mybank.presentation.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybank.data.firebase.FirestoreService
import com.example.mybank.data.model.Account
import com.example.mybank.data.preferences.PreferencesManager
import com.example.mybank.data.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountsUiState(
    val accounts: List<Account> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isCreatingAccount: Boolean = false
)

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val preferencesManager: PreferencesManager,
    private val firestoreService: FirestoreService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AccountsUiState())
    val uiState: StateFlow<AccountsUiState> = _uiState.asStateFlow()
    
    init {
        loadAccounts()
    }
    
    private fun loadAccounts() {
        viewModelScope.launch {
            preferencesManager.userId.collect { userId ->
                if (userId != null) {
                    accountRepository.getUserAccounts(userId)
                        .stateIn(
                            scope = viewModelScope,
                            started = SharingStarted.WhileSubscribed(5000),
                            initialValue = emptyList()
                        )
                        .collect { accounts ->
                            _uiState.value = _uiState.value.copy(accounts = accounts)
                        }
                }
            }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            accountRepository.syncAccounts()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    
    fun createAccount(
        accountName: String,
        accountType: String,
        currency: String = "USD",
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingAccount = true, error = null)
            
            val result = firestoreService.createNewAccount(accountName, accountType, currency)
            
            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isCreatingAccount = false,
                        successMessage = "Account created successfully"
                    )
                    onSuccess()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isCreatingAccount = false,
                        error = exception.message ?: "Failed to create account"
                    )
                }
            )
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
