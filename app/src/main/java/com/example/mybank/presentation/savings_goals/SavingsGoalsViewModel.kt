package com.example.mybank.presentation.savings_goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.mybank.data.model.Account
import com.example.mybank.data.model.AccountType
import com.example.mybank.data.repository.AccountRepository
import com.example.mybank.workers.SavingsGoalsAutomationWorker
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class SavingsGoalsUiState(
    val goalAccounts: List<Account> = emptyList(),
    val sourceAccounts: List<Account> = emptyList(),
    val selectedGoalAccount: Account? = null,
    val selectedSourceAccount: Account? = null,
    val automationAmount: Double = 0.0,
    val automationFrequency: AutomationFrequency = AutomationFrequency.WEEKLY,
    val isAutomationEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

enum class AutomationFrequency(val days: Long) {
    DAILY(1),
    WEEKLY(7),
    BI_WEEKLY(14),
    MONTHLY(30)
}

@HiltViewModel
class SavingsGoalsViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val auth: FirebaseAuth,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavingsGoalsUiState())
    val uiState: StateFlow<SavingsGoalsUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                accountRepository.getUserAccounts(userId)
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = emptyList()
                    )
                    .collect { accounts ->
                        val goalAccounts = accounts.filter {
                            it.accountType == AccountType.GOAL || it.accountType == AccountType.SAVINGS
                        }
                        val sourceAccounts = accounts.filter {
                            it.accountType == AccountType.CHECKING || it.accountType == AccountType.SPENDING
                        }

                        _uiState.value = _uiState.value.copy(
                            goalAccounts = goalAccounts,
                            sourceAccounts = sourceAccounts,
                            isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load accounts"
                )
            }
        }
    }

    fun selectGoalAccount(account: Account) {
        _uiState.value = _uiState.value.copy(selectedGoalAccount = account)
    }

    fun selectSourceAccount(account: Account) {
        _uiState.value = _uiState.value.copy(selectedSourceAccount = account)
    }

    fun setAutomationAmount(amount: Double) {
        _uiState.value = _uiState.value.copy(automationAmount = amount)
    }

    fun setAutomationFrequency(frequency: AutomationFrequency) {
        _uiState.value = _uiState.value.copy(automationFrequency = frequency)
    }

    fun toggleAutomation(enabled: Boolean) {
        viewModelScope.launch {
            val state = _uiState.value
            val userId = auth.currentUser?.uid ?: return@launch

            if (enabled) {
                if (state.selectedSourceAccount == null) {
                    _uiState.value = state.copy(error = "Please select a source account")
                    return@launch
                }

                if (state.selectedGoalAccount == null) {
                    _uiState.value = state.copy(error = "Please select a goal account")
                    return@launch
                }

                if (state.automationAmount <= 0) {
                    _uiState.value = state.copy(error = "Please enter a valid amount")
                    return@launch
                }

                scheduleAutomation(
                    userId,
                    state.selectedSourceAccount.id,
                    state.selectedGoalAccount.id,
                    state.automationAmount,
                    state.automationFrequency
                )

                _uiState.value = state.copy(
                    isAutomationEnabled = true,
                    successMessage = "Automation enabled successfully"
                )
            } else {
                cancelAutomation()
                _uiState.value = state.copy(
                    isAutomationEnabled = false,
                    successMessage = "Automation disabled"
                )
            }
        }
    }

    private fun scheduleAutomation(
        userId: String,
        sourceAccountId: String,
        goalAccountId: String,
        amount: Double,
        frequency: AutomationFrequency
    ) {
        val inputData = Data.Builder()
            .putString(SavingsGoalsAutomationWorker.KEY_USER_ID, userId)
            .putString(SavingsGoalsAutomationWorker.KEY_SOURCE_ACCOUNT_ID, sourceAccountId)
            .putString(SavingsGoalsAutomationWorker.KEY_GOAL_ACCOUNT_ID, goalAccountId)
            .putDouble(SavingsGoalsAutomationWorker.KEY_AUTOMATION_AMOUNT, amount)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<SavingsGoalsAutomationWorker>(
            frequency.days,
            TimeUnit.DAYS
        )
            .setInputData(inputData)
            .setConstraints(constraints)
            .addTag(SavingsGoalsAutomationWorker.WORK_NAME)
            .build()

        workManager.enqueueUniquePeriodicWork(
            SavingsGoalsAutomationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun cancelAutomation() {
        workManager.cancelUniqueWork(SavingsGoalsAutomationWorker.WORK_NAME)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}
