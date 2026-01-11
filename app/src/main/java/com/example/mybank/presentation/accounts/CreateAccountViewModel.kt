package com.example.mybank.presentation.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybank.data.firebase.FirestoreService
import com.example.mybank.data.model.AccountType
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateAccountUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val accountName: String = "",
    val selectedAccountType: AccountType = AccountType.SAVINGS,
    val currency: String = "USD",
    val success: Boolean = false,
    val createdAccountId: String? = null
)

@HiltViewModel
class CreateAccountViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestoreService: FirestoreService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateAccountUiState())
    val uiState: StateFlow<CreateAccountUiState> = _uiState.asStateFlow()

    fun setAccountName(name: String) {
        _uiState.value = _uiState.value.copy(accountName = name)
    }

    fun setAccountType(type: AccountType) {
        _uiState.value = _uiState.value.copy(selectedAccountType = type)
    }

    fun setCurrency(currency: String) {
        _uiState.value = _uiState.value.copy(currency = currency)
    }

    fun createAccount(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            
            if (state.accountName.isBlank()) {
                _uiState.value = state.copy(error = "Account name is required")
                return@launch
            }

            _uiState.value = state.copy(isLoading = true, error = null)

            try {
                val result = firestoreService.createNewAccount(
                    accountName = state.accountName,
                    accountType = state.selectedAccountType.name,
                    currency = state.currency
                )

                result.fold(
                    onSuccess = { response ->
                        val accountId = response["accountId"] as? String
                        _uiState.value = state.copy(
                            isLoading = false,
                            success = true,
                            createdAccountId = accountId
                        )
                        accountId?.let { onSuccess(it) }
                    },
                    onFailure = { exception ->
                        _uiState.value = state.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to create account"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to create account"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
