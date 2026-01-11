package com.example.mybank.presentation.send_money

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybank.data.model.RecipientContact
import com.example.mybank.data.model.Transaction
import com.example.mybank.data.model.TransactionCategory
import com.example.mybank.data.model.TransactionType
import com.example.mybank.data.repository.AccountRepository
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

data class SendMoneyUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val recentRecipients: List<RecipientContact> = emptyList(),
    val allContacts: List<RecipientContact> = emptyList(),
    val searchResults: List<RecipientContact> = emptyList(),
    val selectedRecipient: RecipientContact? = null,
    val lastTransaction: Transaction? = null
)

@HiltViewModel
class SendMoneyViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val accountRepository: AccountRepository,
    private val firestoreService: com.example.mybank.data.firebase.FirestoreService,
    private val firebaseFunctions: com.google.firebase.functions.FirebaseFunctions
) : ViewModel() {

    private val _uiState = MutableStateFlow(SendMoneyUiState())
    val uiState: StateFlow<SendMoneyUiState> = _uiState.asStateFlow()

    private val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    fun loadRecentRecipients() {
        viewModelScope.launch {
            try {
                val userId = currentUserId ?: return@launch

                // Query from top-level transactions collection
                val recentTransactions = firestore
                    .collection("transactions")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("type", TransactionType.DEBIT.name)
                    .whereEqualTo("category", TransactionCategory.TRANSFER.name)
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(5)
                    .get()
                    .await()

                val recipients = recentTransactions.documents.mapNotNull { doc ->
                    val recipientName = doc.getString("recipientName") ?: return@mapNotNull null
                    val recipientId = doc.getString("recipientAccount") ?: return@mapNotNull null

                    RecipientContact(
                        id = recipientId,
                        name = recipientName,
                        accountNumber = doc.getString("relatedAccountId")?.takeLast(4) ?: recipientId.takeLast(4),
                        lastTransferredAt = doc.getLong("timestamp")
                    )
                }.distinctBy { it.id }

                _uiState.value = _uiState.value.copy(
                    recentRecipients = recipients
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load recent recipients: ${e.message}"
                )
            }
        }
    }

    fun loadAllContacts() {
        viewModelScope.launch {
            try {
                val userId = currentUserId ?: return@launch

                // FIXED: Use limit to avoid fetching all users
                // In production, add pagination cursor
                val usersSnapshot = firestore
                    .collection("users")
                    .limit(100)  // Limit to 100 recent users for performance
                    .get()
                    .await()

                val contacts = usersSnapshot.documents.mapNotNull { doc ->
                    if (doc.id == userId) return@mapNotNull null

                    RecipientContact(
                        id = doc.id,
                        name = doc.getString("name") ?: "Unknown",
                        username = doc.getString("username"),
                        email = doc.getString("email"),
                        phone = doc.getString("phone"),
                        photoUrl = doc.getString("profileImageUrl"),
                        accountNumber = doc.id.takeLast(4),
                        isRevolutUser = doc.getBoolean("isRevolutUser") ?: false
                    )
                }

                _uiState.value = _uiState.value.copy(
                    allContacts = contacts
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load contacts: ${e.message}"
                )
            }
        }
    }

    fun searchContacts(query: String) {
        viewModelScope.launch {
            if (query.isEmpty()) {
                _uiState.value = _uiState.value.copy(searchResults = emptyList())
                return@launch
            }

            try {
                val userId = currentUserId ?: return@launch
                val lowercaseQuery = query.lowercase()

                // FIXED: Search by individual indexed fields for better performance
                // Try email first (most common)
                var results = mutableListOf<RecipientContact>()

                // Search by email
                try {
                    val emailSnapshot = firestore
                        .collection("users")
                        .whereGreaterThanOrEqualTo("email", query.lowercase())
                        .whereLessThanOrEqualTo("email", query.lowercase() + "\uf8ff")
                        .limit(10)
                        .get()
                        .await()

                    emailSnapshot.documents.forEach { doc ->
                        if (doc.id != userId) {
                            results.add(RecipientContact(
                                id = doc.id,
                                name = doc.getString("name") ?: "Unknown",
                                username = doc.getString("username"),
                                email = doc.getString("email"),
                                phone = doc.getString("phone"),
                                photoUrl = doc.getString("profileImageUrl"),
                                accountNumber = doc.id.takeLast(4),
                                isRevolutUser = doc.getBoolean("isRevolutUser") ?: false
                            ))
                        }
                    }
                } catch (e: Exception) {
                    // Email search failed, continue to other searches
                }

                // If not enough results, search by phone
                if (results.size < 5 && query.contains("@").not()) {
                    try {
                        val phoneSnapshot = firestore
                            .collection("users")
                            .whereGreaterThanOrEqualTo("phone", query)
                            .whereLessThanOrEqualTo("phone", query + "\uf8ff")
                            .limit((10 - results.size).toLong())
                            .get()
                            .await()

                        phoneSnapshot.documents.forEach { doc ->
                            if (doc.id != userId) {
                                results.add(RecipientContact(
                                    id = doc.id,
                                    name = doc.getString("name") ?: "Unknown",
                                    username = doc.getString("username"),
                                    email = doc.getString("email"),
                                    phone = doc.getString("phone"),
                                    photoUrl = doc.getString("profileImageUrl"),
                                    accountNumber = doc.id.takeLast(4),
                                    isRevolutUser = doc.getBoolean("isRevolutUser") ?: false
                                ))
                            }
                        }
                    } catch (e: Exception) {
                        // Phone search failed, continue to name search
                    }
                }

                // If still not enough, search by name (requires client-side filter)
                if (results.size < 10) {
                    try {
                        val nameSnapshot = firestore
                            .collection("users")
                            .limit(50)  // Limit for name search
                            .get()
                            .await()

                        nameSnapshot.documents.forEach { doc ->
                            if (doc.id == userId) return@forEach

                            val name = doc.getString("name") ?: ""
                            if (name.lowercase().contains(lowercaseQuery)) {
                                // Check for duplicates
                                if (!results.any { it.id == doc.id }) {
                                    results.add(RecipientContact(
                                        id = doc.id,
                                        name = name,
                                        username = doc.getString("username"),
                                        email = doc.getString("email"),
                                        phone = doc.getString("phone"),
                                        photoUrl = doc.getString("profileImageUrl"),
                                        accountNumber = doc.id.takeLast(4),
                                        isRevolutUser = doc.getBoolean("isRevolutUser") ?: false
                                    ))
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Name search failed
                    }
                }

                _uiState.value = _uiState.value.copy(searchResults = results.take(10))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Search failed: ${e.message}"
                )
            }
        }
    }

    fun loadRecipientDetails(recipientId: String) {
        viewModelScope.launch {
            try {
                val recipientDoc = firestore
                    .collection("users")
                    .document(recipientId)
                    .get()
                    .await()

                if (recipientDoc.exists()) {
                    val recipient = RecipientContact(
                        id = recipientDoc.id,
                        name = recipientDoc.getString("name") ?: "Unknown",
                        username = recipientDoc.getString("username"),
                        email = recipientDoc.getString("email"),
                        phone = recipientDoc.getString("phone"),
                        photoUrl = recipientDoc.getString("profileImageUrl"),
                        accountNumber = recipientDoc.id.takeLast(4),
                        isRevolutUser = recipientDoc.getBoolean("isRevolutUser") ?: false
                    )

                    _uiState.value = _uiState.value.copy(
                        selectedRecipient = recipient
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load recipient: ${e.message}"
                )
            }
        }
    }

    fun sendMoney(
        recipientId: String,
        amount: Double,
        note: String?,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val currentUser = firebaseAuth.currentUser
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError("User not authenticated")
                    return@launch
                }

                val userId = currentUser.uid

                // FIX: Get sender's account directly from Firestore to avoid stale cache
                android.util.Log.d("SendMoneyViewModel", "Getting sender accounts for user: $userId")
                
                val senderAccountsSnapshot = firestore
                    .collection("accounts")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("isActive", true)
                    .limit(1)
                    .get()
                    .await()

                if (senderAccountsSnapshot.isEmpty) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError("No active account found for your user")
                    return@launch
                }

                val senderAccountDoc = senderAccountsSnapshot.documents.first()
                val senderAccount = com.example.mybank.data.model.Account(
                    id = senderAccountDoc.id,
                    userId = senderAccountDoc.getString("userId") ?: userId,
                    accountNumber = senderAccountDoc.getString("accountNumber") ?: "",
                    accountName = senderAccountDoc.getString("accountName") ?: "Main Account",
                    accountType = com.example.mybank.data.model.AccountType.valueOf(
                        senderAccountDoc.getString("accountType") ?: "CHECKING"
                    ),
                    balance = senderAccountDoc.getDouble("balance") ?: 0.0,
                    currency = senderAccountDoc.getString("currency") ?: "USD",
                    iban = senderAccountDoc.getString("iban"),
                    createdAt = senderAccountDoc.getTimestamp("createdAt")?.toDate()?.time
                        ?: System.currentTimeMillis(),
                    isActive = senderAccountDoc.getBoolean("isActive") ?: true
                )

                android.util.Log.d("SendMoneyViewModel", "Sender account: ${senderAccount.id}, userId: ${senderAccount.userId}, balance: ${senderAccount.balance}")

                if (senderAccount.balance < amount) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError("Insufficient funds. Available: $${String.format("%.2f", senderAccount.balance)}")
                    return@launch
                }

                // Verify recipient exists
                val recipientDoc = firestore
                    .collection("users")
                    .document(recipientId)
                    .get()
                    .await()

                if (!recipientDoc.exists()) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError("Recipient not found")
                    return@launch
                }

                val recipientName = recipientDoc.getString("name") ?: "Unknown"

                // FIX: Get recipient's account with fallback to Firestore
                android.util.Log.d("SendMoneyViewModel", "Getting recipient accounts for user: $recipientId")
                
                val recipientAccountsSnapshot = firestore
                    .collection("accounts")
                    .whereEqualTo("userId", recipientId)
                    .whereEqualTo("isActive", true)
                    .limit(1)
                    .get()
                    .await()

                if (recipientAccountsSnapshot.isEmpty) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError("Recipient has no active account")
                    return@launch
                }

                val recipientAccountDoc = recipientAccountsSnapshot.documents.first()
                val recipientAccount = com.example.mybank.data.model.Account(
                    id = recipientAccountDoc.id,
                    userId = recipientAccountDoc.getString("userId") ?: recipientId,
                    accountNumber = recipientAccountDoc.getString("accountNumber") ?: "",
                    accountName = recipientAccountDoc.getString("accountName") ?: "Main Account",
                    accountType = com.example.mybank.data.model.AccountType.valueOf(
                        recipientAccountDoc.getString("accountType") ?: "CHECKING"
                    ),
                    balance = recipientAccountDoc.getDouble("balance") ?: 0.0,
                    currency = recipientAccountDoc.getString("currency") ?: "USD",
                    iban = recipientAccountDoc.getString("iban"),
                    createdAt = recipientAccountDoc.getTimestamp("createdAt")?.toDate()?.time
                        ?: System.currentTimeMillis(),
                    isActive = recipientAccountDoc.getBoolean("isActive") ?: true
                )

                android.util.Log.d("SendMoneyViewModel", "Recipient account: ${recipientAccount.id}, userId: ${recipientAccount.userId}, balance: ${recipientAccount.balance}")

                // Prevent transfers to self
                if (senderAccount.id == recipientAccount.id) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError("Cannot transfer to the same account")
                    return@launch
                }

                android.util.Log.d("SendMoneyViewModel", "Initiating transfer: ${senderAccount.id} -> ${recipientAccount.id}, amount: $amount")

                // FIX: Use FirestoreService.processTransfer for consistency with other ViewModels
                val idempotencyKey = "transfer-${userId}-${System.currentTimeMillis()}-${UUID.randomUUID()}"

                val result = firestoreService.processTransfer(
                    fromAccountId = senderAccount.id,
                    toAccountId = recipientAccount.id,
                    amount = amount,
                    currency = senderAccount.currency,
                    description = note ?: "Transfer to $recipientName",
                    idempotencyKey = idempotencyKey
                )

                result.fold(
                    onSuccess = { transferResult ->
                        android.util.Log.d("SendMoneyViewModel", "Transfer successful: ${transferResult.transactionId}")

                        val transaction = Transaction(
                            id = transferResult.transactionId ?: "",
                            accountId = senderAccount.id,
                            userId = userId,
                            type = TransactionType.DEBIT,
                            category = TransactionCategory.TRANSFER,
                            amount = amount,
                            currency = senderAccount.currency,
                            description = note ?: "Transfer to $recipientName",
                            recipientName = recipientName,
                            recipientAccount = recipientAccount.accountNumber,
                            timestamp = System.currentTimeMillis(),
                            status = "COMPLETED",
                            balanceAfter = transferResult.fromBalance
                        )

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            lastTransaction = transaction
                        )

                        val finalTransactionId = transferResult.transactionId
                        if (finalTransactionId != null) {
                            onSuccess(finalTransactionId)
                        } else {
                            onError("Transfer completed but no transaction ID returned")
                        }
                    },
                    onFailure = { exception ->
                        android.util.Log.e("SendMoneyViewModel", "Transfer failed: ${exception.message}", exception)

                        val errorMsg = when (exception) {
                            is com.google.firebase.functions.FirebaseFunctionsException -> {
                                when (exception.code) {
                                    com.google.firebase.functions.FirebaseFunctionsException.Code.UNAUTHENTICATED ->
                                        "Please sign out and sign in again."
                                    com.google.firebase.functions.FirebaseFunctionsException.Code.PERMISSION_DENIED ->
                                        "Permission denied."
                                    com.google.firebase.functions.FirebaseFunctionsException.Code.FAILED_PRECONDITION ->
                                        exception.message ?: "Transaction requirements not met."
                                    com.google.firebase.functions.FirebaseFunctionsException.Code.NOT_FOUND ->
                                        "Account not found."
                                    else ->
                                        exception.message ?: "Transaction failed"
                                }
                            }
                            else -> exception.message ?: "Transaction failed"
                        }

                        _uiState.value = _uiState.value.copy(error = errorMsg)
                        onError(errorMsg)
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("SendMoneyViewModel", "Unexpected error: ${e.message}", e)

                val errorMsg = e.message ?: "Transaction failed"
                _uiState.value = _uiState.value.copy(isLoading = false, error = errorMsg)
                onError(errorMsg)
            }
        }
    }

    // TODO: Generate and send receipt
    // Consider:
    // 1. Generate PDF receipt with transfer details (sender, recipient, amount, date/time)
    // 2. Upload PDF to Cloud Storage
    // 3. Send email notification with receipt download link to both parties
    // 4. Save receipt metadata to Firestore
    // See: com.example.mybank.util.BankStatementGenerator.kt

    fun loadTransactionDetails(transactionId: String) {
        viewModelScope.launch {
            try {
                val userId = currentUserId ?: return@launch

                // Query from top-level transactions collection
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
                        type = TransactionType.valueOf(transactionDoc.getString("type") ?: "DEBIT"),
                        category = TransactionCategory.valueOf(transactionDoc.getString("category") ?: "TRANSFER"),
                        amount = transactionDoc.getDouble("amount") ?: 0.0,
                        currency = transactionDoc.getString("currency") ?: "USD",
                        description = transactionDoc.getString("description") ?: "",
                        recipientName = transactionDoc.getString("recipientName"),
                        recipientAccount = transactionDoc.getString("recipientAccount"),
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
}