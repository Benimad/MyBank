package com.example.mybank.presentation.add_money

// ⚠️ CRITICAL SECURITY WARNING ⚠️
// This class stores card details (including CVV) in memory which violates PCI-DSS compliance.
// DO NOT USE THIS CODE IN PRODUCTION.
//
// REQUIRED INTEGRATION:
// 1. Add Stripe Android SDK to app/build.gradle.kts dependencies
// 2. Use Stripe Elements for secure card collection
// 3. Follow Stripe Payment Intents flow:
//    - Create PaymentIntent on backend Cloud Function
//    - Collect card via Stripe Elements securely
//    - Confirm payment on client using tokenized card
//    - Server webhook validates and updates balance on payment success
//
// See: https://stripe.com/docs/mobile/android
// See: https://stripe.com/docs/payments/accept-a-payment
// See: https://stripe.com/docs/api/payment_intents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybank.data.model.Account
import com.example.mybank.data.model.Transaction
import com.example.mybank.data.model.TransactionCategory
import com.example.mybank.data.model.TransactionType
import com.example.mybank.data.repository.AccountRepository
import com.example.mybank.data.repository.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

data class PaymentMethod(
    val id: String,
    val type: PaymentMethodType,
    val name: String,
    val lastFour: String,
    val isDefault: Boolean = false,
    val fee: Double = 0.0,
    val arrivalTime: String = "Instant"
)

enum class PaymentMethodType {
    DEBIT_CARD,
    CREDIT_CARD,
    BANK_ACCOUNT,
    APPLE_PAY,
    GOOGLE_PAY
}

data class AddMoneyUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentBalance: Double = 0.0,
    val amount: String = "",
    val selectedPaymentMethod: PaymentMethod? = null,
    val savedPaymentMethods: List<PaymentMethod> = emptyList(),
    @Deprecated("Card details should never be stored, use Stripe tokenization instead")
    val cardNumber: String = "",
    @Deprecated("Card name should be retrieved tokenized from Stripe, not entered directly")
    val cardHolderName: String = "",
    @Deprecated("Expiry date should be tokenized with Stripe, not stored directly")
    val expiryDate: String = "",
    @Deprecated("CVV should NEVER be stored, even in memory. Use Stripe Elements SDK")
    val cvv: String = "",
    val saveCard: Boolean = true,
    val lastTransaction: Transaction? = null
)

@HiltViewModel
class AddMoneyViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMoneyUiState())
    val uiState: StateFlow<AddMoneyUiState> = _uiState.asStateFlow()

    private val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    init {
        loadUserData()
        loadSavedPaymentMethods()
    }

    fun setAmount(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _uiState.value = _uiState.value.copy(selectedPaymentMethod = method)
    }

    fun setCardNumber(cardNumber: String) {
        _uiState.value = _uiState.value.copy(cardNumber = cardNumber)
    }

    fun setCardHolderName(name: String) {
        _uiState.value = _uiState.value.copy(cardHolderName = name)
    }

    fun setExpiryDate(expiryDate: String) {
        _uiState.value = _uiState.value.copy(expiryDate = expiryDate)
    }

    fun setCvv(cvv: String) {
        _uiState.value = _uiState.value.copy(cvv = cvv)
    }

    fun setSaveCard(save: Boolean) {
        _uiState.value = _uiState.value.copy(saveCard = save)
    }

    fun loadUserData() {
        viewModelScope.launch {
            try {
                val userId = firebaseAuth.currentUser?.uid ?: return@launch

                // Get user name from users collection
                val userDoc = firestore
                    .collection("users")
                    .document(userId)
                    .get()
                    .await()

                val name = userDoc.getString("name") ?: ""

                // Get balance from accounts collection
                val accounts = accountRepository.getUserAccounts(userId).first()
                val mainAccount = accounts.firstOrNull()
                val balance = mainAccount?.balance ?: 0.0

                _uiState.value = _uiState.value.copy(
                    currentBalance = balance,
                    cardHolderName = name
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load user data: ${e.message}"
                )
            }
        }
    }

    private fun loadSavedPaymentMethods() {
        viewModelScope.launch {
            try {
                val userId = firebaseAuth.currentUser?.uid ?: return@launch

                val methodsSnapshot = firestore
                    .collection("users")
                    .document(userId)
                    .collection("payment_methods")
                    .get()
                    .await()

                val methods = methodsSnapshot.documents.map { doc ->
                    PaymentMethod(
                        id = doc.id,
                        type = PaymentMethodType.valueOf(doc.getString("type") ?: "DEBIT_CARD"),
                        name = doc.getString("name") ?: "",
                        lastFour = doc.getString("lastFour") ?: "",
                        isDefault = doc.getBoolean("isDefault") ?: false,
                        fee = doc.getDouble("fee") ?: 0.0,
                        arrivalTime = doc.getString("arrivalTime") ?: "Instant"
                    )
                }

                _uiState.value = _uiState.value.copy(
                    savedPaymentMethods = methods
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load payment methods: ${e.message}"
                )
            }
        }
    }

    fun savePaymentMethod() {
        viewModelScope.launch {
            try {
                val userId = firebaseAuth.currentUser?.uid ?: return@launch
                val state = _uiState.value

                if (!state.saveCard) return@launch

                val methodData = hashMapOf(
                    "type" to "DEBIT_CARD",
                    "name" to "Visa ending in ${state.cardNumber.takeLast(4)}",
                    "lastFour" to state.cardNumber.takeLast(4),
                    "isDefault" to false,
                    "fee" to 0.0,
                    "arrivalTime" to "Instant",
                    "addedAt" to System.currentTimeMillis()
                )

                firestore
                    .collection("users")
                    .document(userId)
                    .collection("payment_methods")
                    .add(methodData)
                    .await()

            } catch (e: Exception) {
            }
        }
    }

    fun addMoney(
        amount: Double,
        paymentMethod: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val userId = firebaseAuth.currentUser?.uid ?: run {
                    android.util.Log.e("AddMoneyViewModel", "User not authenticated")
                    onError("User not authenticated")
                    return@launch
                }

                android.util.Log.d("AddMoneyViewModel", "Starting deposit for userId: $userId")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                // Direct Firestore query with manual parsing
                val accountsSnapshot = firestore.collection("accounts")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                
                val accounts = accountsSnapshot.documents.mapNotNull { doc ->
                    try {
                        Account(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            accountNumber = doc.getString("accountNumber") ?: "",
                            accountName = doc.getString("accountName") ?: "",
                            accountType = com.example.mybank.data.model.AccountType.valueOf(doc.getString("accountType") ?: "CHECKING"),
                            balance = doc.getDouble("balance") ?: 0.0,
                            currency = doc.getString("currency") ?: "USD",
                            iban = doc.getString("iban"),
                            createdAt = (doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()),
                            isActive = doc.getBoolean("isActive") ?: true
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("AddMoneyViewModel", "Error parsing account: ${e.message}")
                        null
                    }
                }
                android.util.Log.d("AddMoneyViewModel", "Found ${accounts.size} accounts for user $userId")
                
                if (accounts.isEmpty()) {
                    val userEmail = firebaseAuth.currentUser?.email
                    android.util.Log.d("AddMoneyViewModel", "No accounts found, trying with email: $userEmail")
                    
                    val userDoc = firestore.collection("users")
                        .whereEqualTo("email", userEmail)
                        .limit(1)
                        .get()
                        .await()
                    
                    if (userDoc.documents.isNotEmpty()) {
                        val correctUserId = userDoc.documents[0].id
                        android.util.Log.d("AddMoneyViewModel", "Found correct userId: $correctUserId")
                        
                        val correctAccountsSnapshot = firestore.collection("accounts")
                            .whereEqualTo("userId", correctUserId)
                            .get()
                            .await()
                        
                        val correctAccounts = correctAccountsSnapshot.documents.mapNotNull { doc ->
                            try {
                                Account(
                                    id = doc.id,
                                    userId = doc.getString("userId") ?: "",
                                    accountNumber = doc.getString("accountNumber") ?: "",
                                    accountName = doc.getString("accountName") ?: "",
                                    accountType = com.example.mybank.data.model.AccountType.valueOf(doc.getString("accountType") ?: "CHECKING"),
                                    balance = doc.getDouble("balance") ?: 0.0,
                                    currency = doc.getString("currency") ?: "USD",
                                    iban = doc.getString("iban"),
                                    createdAt = (doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()),
                                    isActive = doc.getBoolean("isActive") ?: true
                                )
                            } catch (e: Exception) {
                                android.util.Log.e("AddMoneyViewModel", "Error parsing account: ${e.message}")
                                null
                            }
                        }
                        android.util.Log.d("AddMoneyViewModel", "Found ${correctAccounts.size} accounts with correct userId")
                        
                        if (correctAccounts.isEmpty()) {
                            _uiState.value = _uiState.value.copy(isLoading = false)
                            onError("No account found. Please create an account first.")
                            return@launch
                        }
                        
                        processDeposit(correctAccounts.first(), correctUserId, amount, paymentMethod, onSuccess, onError)
                        return@launch
                    }
                }
                
                if (accounts.isEmpty()) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError("No account found. Please create an account first.")
                    return@launch
                }
                
                processDeposit(accounts.first(), userId, amount, paymentMethod, onSuccess, onError)
            } catch (e: Exception) {
                android.util.Log.e("AddMoneyViewModel", "Deposit error", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Transaction failed"
                )
                onError(e.message ?: "Transaction failed")
            }
        }
    }
    
    private suspend fun processDeposit(
        account: Account,
        userId: String,
        amount: Double,
        paymentMethod: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val newBalance = account.balance + amount
            val transactionId = "TXN-${System.currentTimeMillis()}-${java.util.UUID.randomUUID()}"
            
            firestore.collection("accounts")
                .document(account.id)
                .update("balance", newBalance)
                .await()
            
            val transaction = Transaction(
                id = transactionId,
                accountId = account.id,
                userId = userId,
                type = TransactionType.CREDIT,
                category = TransactionCategory.DEPOSIT,
                amount = amount,
                currency = account.currency,
                description = "Deposit from $paymentMethod",
                timestamp = System.currentTimeMillis(),
                status = "COMPLETED",
                balanceAfter = newBalance
            )
            
            firestore.collection("transactions")
                .document(transactionId)
                .set(transaction)
                .await()
            
            transactionRepository.insertTransaction(transaction)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                currentBalance = newBalance,
                lastTransaction = transaction
            )
            
            savePaymentMethod()
            android.util.Log.d("AddMoneyViewModel", "Deposit successful: newBalance=$newBalance")
            onSuccess(transactionId)
        } catch (e: Exception) {
            android.util.Log.e("AddMoneyViewModel", "Process deposit error", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: "Deposit failed"
            )
            onError(e.message ?: "Deposit failed")
        }
    }

    fun loadTransactionDetails(transactionId: String) {
        viewModelScope.launch {
            try {
                val userId = firebaseAuth.currentUser?.uid ?: return@launch

                // Query from transactions collection
                val transactionDoc = firestore
                    .collection("transactions")
                    .document(transactionId)
                    .get()
                    .await()

                if (transactionDoc.exists()) {
                    val transaction = Transaction(
                        id = transactionDoc.id,
                        accountId = transactionDoc.getString("accountId") ?: "",
                        userId = transactionDoc.getString("userId") ?: userId,
                        type = TransactionType.valueOf(transactionDoc.getString("type") ?: "CREDIT"),
                        category = TransactionCategory.valueOf(transactionDoc.getString("category") ?: "DEPOSIT"),
                        amount = transactionDoc.getDouble("amount") ?: 0.0,
                        currency = transactionDoc.getString("currency") ?: "USD",
                        description = transactionDoc.getString("description") ?: "",
                        timestamp = transactionDoc.getLong("timestamp") ?: System.currentTimeMillis(),
                        status = transactionDoc.getString("status") ?: "COMPLETED",
                        balanceAfter = transactionDoc.getDouble("balanceAfter")
                    )

                    _uiState.value = _uiState.value.copy(
                        lastTransaction = transaction
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load transaction: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}