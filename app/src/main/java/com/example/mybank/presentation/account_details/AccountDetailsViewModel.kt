package com.example.mybank.presentation.account_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybank.data.model.Account
import com.example.mybank.data.model.Transaction
import com.example.mybank.data.model.TransactionType
import com.example.mybank.data.repository.AccountRepository
import com.example.mybank.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountDetailsUiState(
    val account: Account? = null,
    val transactions: List<Transaction> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AccountDetailsViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountDetailsUiState())
    val uiState: StateFlow<AccountDetailsUiState> = _uiState.asStateFlow()

    fun loadAccount(accountId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                accountRepository.getAccount(accountId).collect { account ->
                    if (account != null) {
                        _uiState.value = _uiState.value.copy(
                            account = account,
                            isLoading = false
                        )
                        loadTransactions(accountId)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Account not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun loadTransactions(accountId: String) {
        viewModelScope.launch {
            try {
                transactionRepository.getAccountTransactions(accountId).collect { transactions ->
                    val income = transactions
                        .filter { it.type == TransactionType.CREDIT }
                        .sumOf { it.amount }
                    
                    val expenses = transactions
                        .filter { it.type == TransactionType.DEBIT }
                        .sumOf { it.amount }
                    
                    _uiState.value = _uiState.value.copy(
                        transactions = transactions,
                        totalIncome = income,
                        totalExpenses = expenses
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun refresh(accountId: String) {
        loadAccount(accountId)
    }
}
