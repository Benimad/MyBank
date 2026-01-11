package com.example.mybank.presentation.add_money

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val cardNumber: String = "",
    val cardHolderName: String = "",
    val expiryDate: String = "",
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
                val userId = currentUserId ?: return@launch

                // Get user name from users collection
                val userDoc = firestore
                    .collection("users")
                    .document(userId)
                    .get()
                    .await()

                val name = userDoc.getString("name") ?: ""

                // Get balance from accounts collection (not users collection)
                val accounts = accountRepository.getUserAccounts(userId).first()
                val mainAccount = accounts.firstOrNull { it.isActive }
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
                val userId = currentUserId ?: return@launch

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
                val userId = currentUserId ?: return@launch
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
                val userId = currentUserId ?: run {
                    android.util.Log.e("AddMoneyViewModel", "User not authenticated - currentUserId is null")
                    onError("User not authenticated")
                    return@launch
                }

                android.util.Log.d("AddMoneyViewModel", "Starting deposit for user: $userId, amount: $amount")
                android.util.Log.d("AddMoneyViewModel", "FirebaseAuth currentUser: ${firebaseAuth.currentUser?.uid}")
                android.util.Log.d("AddMoneyViewModel", "FirebaseAuth email: ${firebaseAuth.currentUser?.email}")

                _uiState.value = _uiState.value.copy(isLoading = true)

                // Query Firestore directly for user's active account
                android.util.Log.d("AddMoneyViewModel", "Querying Firestore for active accounts...")
                val accountsSnapshot = firestore.collection("accounts")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("isActive", true)
                    .get()
                    .await()
                
                android.util.Log.d("AddMoneyViewModel", "Found ${accountsSnapshot.documents.size} active accounts in Firestore")
                
                if (accountsSnapshot.documents.isEmpty()) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError("No active account found. Please create an account first.")
                    return@launch
                }
                
                // Get the first active account
                val accountDoc = accountsSnapshot.documents.first()
                val accountId = accountDoc.id
                val currentBalance = accountDoc.getDouble("balance") ?: 0.0
                val accountName = accountDoc.getString("accountName") ?: "Main Account"
                val currency = accountDoc.getString("currency") ?: "USD"
                
                android.util.Log.d("AddMoneyViewModel", "Using account: id=$accountId, balance=$currentBalance")

                firestore.runTransaction { transaction ->
                    // Read account within transaction for consistency
                    val accountRef = firestore.collection("accounts").document(accountId)
                    val accountSnapshot = transaction.get(accountRef)
                    
                    // Verify account still exists
                    if (!accountSnapshot.exists()) {
                        android.util.Log.e("AddMoneyViewModel", "Account $accountId was deleted during transaction")
                        throw Exception("Account was deleted. Please refresh and try again.")
                    }
                    
                    val accountData = accountSnapshot.data
                    if (accountData?.get("userId") != userId) {
                        throw Exception("Unauthorized access to account")
                    }
                    
                    val currentBalanceFromDb = accountData["balance"] as? Double ?: 0.0
                    
                    // Update account balance
                    transaction.update(
                        accountRef,
                        "balance",
                        currentBalanceFromDb + amount
                    )

                    val transactionId = "TXN-${System.currentTimeMillis()}-${UUID.randomUUID().toString().take(4)}"
                    val timestamp = System.currentTimeMillis()

                    // Create transaction with correct fields
                    val transactionData = hashMapOf(
                        "id" to transactionId,
                        "userId" to userId,  // Add userId for filtering
                        "accountId" to accountId,  // Use the Firestore account ID
                        "type" to TransactionType.CREDIT.name,
                        "category" to TransactionCategory.DEPOSIT.name,
                        "amount" to amount,
                        "currency" to currency,  // Use currency from Firestore
                        "description" to "Deposit from $paymentMethod",
                        "paymentMethod" to paymentMethod,
                        "timestamp" to timestamp,
                        "status" to "COMPLETED",
                        "balanceAfter" to (currentBalanceFromDb + amount)
                    )

                    // Store in transactions collection (not users/{userId}/transactions)
                    transaction.set(
                        firestore.collection("transactions").document(transactionId),
                        transactionData
                    )

                    // Create notification
                    val notification = hashMapOf(
                        "id" to UUID.randomUUID().toString(),
                        "userId" to userId,
                        "title" to "Money Added",
                        "message" to "You successfully added $${String.format("%.2f", amount)} to your $accountName account",
                        "type" to "DEPOSIT",
                        "timestamp" to timestamp,
                        "isRead" to false,
                        "relatedTransactionId" to transactionId,
                        "relatedAccountId" to accountId
                    )

                    transaction.set(
                        firestore.collection("notifications").document(UUID.randomUUID().toString()),
                        notification
                    )

                    transactionId
                }.await().let { transactionId ->
                    // Calculate new balance
                    val newBalance = currentBalance + amount

                    // Create transaction with all required fields
                    val transaction = Transaction(
                        id = transactionId,
                        accountId = accountId,  // Use Firestore account ID
                        userId = userId,
                        type = TransactionType.CREDIT,
                        category = TransactionCategory.DEPOSIT,
                        amount = amount,
                        currency = currency,  // Use currency from Firestore
                        description = "Deposit from $paymentMethod",
                        timestamp = System.currentTimeMillis(),
                        status = "COMPLETED",
                        balanceAfter = newBalance
                    )

                    transactionRepository.insertTransaction(transaction)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentBalance = newBalance,
                        lastTransaction = transaction
                    )

                    savePaymentMethod()

                    onSuccess(transactionId)
                }
            } catch (e: Exception) {
                // Enhanced error logging
                android.util.Log.e("AddMoneyViewModel", "Deposit failed", e)
                android.util.Log.e("AddMoneyViewModel", "Error type: ${e::class.simpleName}")
                android.util.Log.e("AddMoneyViewModel", "Error message: ${e.message}")
                android.util.Log.e("AddMoneyViewModel", "Stack trace: ${e.stackTraceToString()}")
                
                val errorMessage = when {
                    e.message?.contains("PERMISSION_DENIED") == true -> 
                        "Permission denied. Please ensure you're logged in and Firestore rules are deployed."
                    e.message?.contains("UNAUTHENTICATED") == true -> 
                        "Not authenticated. Please log in again."
                    e.message?.contains("NOT_FOUND") == true -> 
                        "Account not found. Please refresh and try again."
                    else -> "Transaction failed: ${e.message}"
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
                onError(errorMessage)
            }
        }
    }

    fun loadTransactionDetails(transactionId: String) {
        viewModelScope.launch {
            try {
                val userId = currentUserId ?: return@launch

                // Query from transactions collection (not users/{userId}/transactions)
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