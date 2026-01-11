package com.example.mybank.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybank.data.model.Account
import com.example.mybank.data.model.Transaction
import com.example.mybank.data.preferences.PreferencesManager
import com.example.mybank.data.repository.AccountRepository
import com.example.mybank.data.repository.TransactionRepository
import com.example.mybank.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "",
    val userEmail: String = "",
    val profileImageUrl: String? = null,
    val accounts: List<Account> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val totalBalance: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            combine(
                preferencesManager.userName,
                preferencesManager.userEmail
            ) { name, email ->
                _uiState.value = _uiState.value.copy(
                    userName = name ?: "",
                    userEmail = email ?: ""
                )
            }.collect { }
        }
        
        viewModelScope.launch {
            preferencesManager.userId.collect { userId ->
                if (userId != null) {
                    combine(
                        accountRepository.getUserAccounts(userId),
                        accountRepository.getTotalBalance(userId),
                        transactionRepository.getRecentUserTransactions(userId, 10)
                    ) { accounts, balance, transactions ->
                        _uiState.value = _uiState.value.copy(
                            accounts = accounts,
                            totalBalance = balance ?: 0.0,
                            recentTransactions = transactions
                        )
                    }.collect { }
                }
            }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            
            // Sync accounts from API
            when (accountRepository.syncAccounts()) {
                is Resource.Success -> {
                    // Sync transactions from API
                    transactionRepository.syncAllTransactions(20)
                }
                is Resource.Error -> {
                    // Handle error silently for refresh
                }
                is Resource.Loading -> {}
            }
            
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }
}
