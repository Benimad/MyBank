package com.example.mybank.presentation.transaction_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybank.data.model.Transaction
import com.example.mybank.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionDetailsUiState(
    val transaction: Transaction? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TransactionDetailsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionDetailsUiState())
    val uiState: StateFlow<TransactionDetailsUiState> = _uiState.asStateFlow()

    fun loadTransaction(transactionId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                transactionRepository.getTransaction(transactionId)
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = null
                    )
                    .collect { transaction ->
                        _uiState.value = _uiState.value.copy(
                            transaction = transaction,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun downloadReceipt() {
        viewModelScope.launch {
            val transaction = _uiState.value.transaction ?: return@launch
        }
    }

    fun shareTransaction() {
        viewModelScope.launch {
            val transaction = _uiState.value.transaction ?: return@launch
        }
    }
}
