package com.example.mybank.presentation.bill_payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybank.data.model.BillPayment
import com.example.mybank.data.model.Biller
import com.example.mybank.data.model.BillerCategory
import com.example.mybank.data.model.PaymentStatus
import com.example.mybank.data.model.RecurrenceFrequency
import com.example.mybank.data.repository.BillPaymentRepository
import com.example.mybank.util.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class BillPaymentUiState(
    val billers: List<Biller> = emptyList(),
    val payments: List<BillPayment> = emptyList(),
    val recurringPayments: List<BillPayment> = emptyList(),
    val selectedBiller: Biller? = null,
    val amount: Double = 0.0,
    val accountId: String = "",
    val isRecurring: Boolean = false,
    val recurrenceFrequency: RecurrenceFrequency = RecurrenceFrequency.MONTHLY,
    val scheduledDate: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class BillPaymentViewModel @Inject constructor(
    private val billPaymentRepository: BillPaymentRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(BillPaymentUiState())
    val uiState: StateFlow<BillPaymentUiState> = _uiState.asStateFlow()

    init {
        loadBillers()
        loadPayments()
        loadRecurringPayments()
    }

    private fun loadBillers() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                billPaymentRepository.getUserBillers(userId)
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = emptyList()
                    )
                    .collect { billers ->
                        _uiState.value = _uiState.value.copy(
                            billers = billers,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load billers"
                )
            }
        }
    }

    private fun loadPayments() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            
            try {
                billPaymentRepository.getUserBillPayments(userId)
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = emptyList()
                    )
                    .collect { payments ->
                        _uiState.value = _uiState.value.copy(payments = payments)
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load payments"
                )
            }
        }
    }

    private fun loadRecurringPayments() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            
            try {
                billPaymentRepository.getRecurringPayments(userId)
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = emptyList()
                    )
                    .collect { payments ->
                        _uiState.value = _uiState.value.copy(recurringPayments = payments)
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load recurring payments"
                )
            }
        }
    }

    fun selectBiller(biller: Biller) {
        _uiState.value = _uiState.value.copy(selectedBiller = biller)
    }

    fun setAmount(amount: Double) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    fun setAccountId(accountId: String) {
        _uiState.value = _uiState.value.copy(accountId = accountId)
    }

    fun setIsRecurring(isRecurring: Boolean) {
        _uiState.value = _uiState.value.copy(isRecurring = isRecurring)
    }

    fun setRecurrenceFrequency(frequency: RecurrenceFrequency) {
        _uiState.value = _uiState.value.copy(recurrenceFrequency = frequency)
    }

    fun setScheduledDate(date: Long) {
        _uiState.value = _uiState.value.copy(scheduledDate = date)
    }

    fun addBiller(
        billerName: String,
        category: BillerCategory,
        accountNumber: String,
        billerCode: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val biller = Biller(
                id = UUID.randomUUID().toString(),
                userId = userId,
                billerName = billerName,
                category = category,
                accountNumber = accountNumber,
                billerCode = billerCode
            )

            when (val result = billPaymentRepository.addBiller(biller)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Biller added successfully"
                    )
                    onSuccess()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun payBill(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            val userId = auth.currentUser?.uid ?: return@launch

            if (state.selectedBiller == null) {
                _uiState.value = state.copy(error = "Please select a biller")
                return@launch
            }

            if (state.amount <= 0) {
                _uiState.value = state.copy(error = "Please enter a valid amount")
                return@launch
            }

            if (state.accountId.isEmpty()) {
                _uiState.value = state.copy(error = "Please select an account")
                return@launch
            }

            _uiState.value = state.copy(isLoading = true, error = null)

            val payment = BillPayment(
                id = UUID.randomUUID().toString(),
                userId = userId,
                billerId = state.selectedBiller.id,
                accountId = state.accountId,
                amount = state.amount,
                isRecurring = state.isRecurring,
                recurrenceFrequency = if (state.isRecurring) state.recurrenceFrequency else RecurrenceFrequency.NONE,
                scheduledDate = state.scheduledDate,
                nextPaymentDate = if (state.isRecurring) calculateNextPaymentDate(state.recurrenceFrequency) else null
            )

            when (val result = if (state.scheduledDate != null) {
                billPaymentRepository.scheduleBillPayment(payment)
            } else {
                billPaymentRepository.payBill(payment)
            }) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        amount = 0.0,
                        selectedBiller = null,
                        successMessage = if (state.scheduledDate != null) "Payment scheduled" else "Payment successful"
                    )
                    if (result.data is String) {
                        onSuccess(result.data)
                    }
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun cancelPayment(paymentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = billPaymentRepository.cancelBillPayment(paymentId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Payment cancelled successfully"
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun deleteBiller(billerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = billPaymentRepository.deleteBiller(billerId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Biller deleted successfully"
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    private fun calculateNextPaymentDate(frequency: RecurrenceFrequency): Long {
        val calendar = Calendar.getInstance()
        when (frequency) {
            RecurrenceFrequency.DAILY -> calendar.add(Calendar.DAY_OF_MONTH, 1)
            RecurrenceFrequency.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            RecurrenceFrequency.BI_WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 2)
            RecurrenceFrequency.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            RecurrenceFrequency.QUARTERLY -> calendar.add(Calendar.MONTH, 3)
            RecurrenceFrequency.YEARLY -> calendar.add(Calendar.YEAR, 1)
            else -> {}
        }
        return calendar.timeInMillis
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}
