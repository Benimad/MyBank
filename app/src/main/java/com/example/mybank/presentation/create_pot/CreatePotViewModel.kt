package com.example.mybank.presentation.create_pot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybank.data.model.Account
import com.example.mybank.data.model.AccountType
import com.example.mybank.data.model.PotColorTag
import com.example.mybank.data.repository.AccountRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CreatePotUiState(
    val selectedType: AccountType = AccountType.SAVINGS,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreatePotViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePotUiState())
    val uiState: StateFlow<CreatePotUiState> = _uiState.asStateFlow()

    fun selectType(type: AccountType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
    }

    fun createPot(
        name: String,
        colorTag: PotColorTag,
        targetAmount: Double?,
        deadline: Long?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val accountNumber = generateAccountNumber()
                
                val newAccount = Account(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    accountNumber = accountNumber,
                    accountName = name,
                    accountType = _uiState.value.selectedType,
                    balance = 0.0,
                    potColorTag = colorTag,
                    goalAmount = targetAmount,
                    goalDeadline = deadline,
                    tagLabel = _uiState.value.selectedType.name
                )

                accountRepository.insertAccount(newAccount)
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun generateAccountNumber(): String {
        return "POT${(1000..9999).random()}"
    }
}
