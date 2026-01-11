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
    private val accountRepository: AccountRepository
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

                val usersSnapshot = firestore
                    .collection("users")
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

                val usersSnapshot = firestore
                    .collection("users")
                    .get()
                    .await()

                val results = usersSnapshot.documents.mapNotNull { doc ->
                    if (doc.id == userId) return@mapNotNull null

                    val name = doc.getString("name") ?: ""
                    val username = doc.getString("username") ?: ""
                    val email = doc.getString("email") ?: ""
                    val phone = doc.getString("phone") ?: ""

                    if (name.lowercase().contains(lowercaseQuery) ||
                        username.lowercase().contains(lowercaseQuery) ||
                        email.lowercase().contains(lowercaseQuery) ||
                        phone.contains(query)
                    ) {
                        RecipientContact(
                            id = doc.id,
                            name = name,
                            username = username.ifEmpty { null },
                            email = email.ifEmpty { null },
                            phone = phone.ifEmpty { null },
                            photoUrl = doc.getString("profileImageUrl"),
                            accountNumber = doc.id.takeLast(4),
                            isRevolutUser = doc.getBoolean("isRevolutUser") ?: false
                        )
                    } else {
                        null
                    }
                }

                _uiState.value = _uiState.value.copy(searchResults = results)
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
            try {
                val userId = currentUserId ?: run {
                    onError("User not authenticated")
                    return@launch
                }

                _uiState.value = _uiState.value.copy(isLoading = true)

                // Get sender's accounts
                val senderAccounts = accountRepository.getUserAccounts(userId).first()
                val senderAccount = senderAccounts.firstOrNull { it.isActive }

                if (senderAccount == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError("No active account found")
                    return@launch
                }

                val senderBalance = senderAccount.balance

                if (senderBalance < amount) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError("Insufficient balance. Available: $${String.format("%.2f", senderBalance)}")
                    return@launch
                }

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

                // Get recipient's accounts
                val recipientAccounts = accountRepository.getUserAccounts(recipientId).first()
                val recipientAccount = recipientAccounts.firstOrNull { it.isActive }

                if (recipientAccount == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError("Recipient has no active account")
                    return@launch
                }

                val recipientBalance = recipientAccount.balance
                val recipientName = recipientDoc.getString("name") ?: "Unknown"
                val senderName = senderAccount.accountName

                firestore.runTransaction { transaction ->
                    // Update sender account balance
                    transaction.update(
                        firestore.collection("accounts").document(senderAccount.id),
                        "balance",
                        senderBalance - amount
                    )

                    // Update recipient account balance
                    transaction.update(
                        firestore.collection("accounts").document(recipientAccount.id),
                        "balance",
                        recipientBalance + amount
                    )

                    val transactionId = "TXN-${System.currentTimeMillis()}-${UUID.randomUUID().toString().take(4)}"
                    val timestamp = System.currentTimeMillis()

                    val senderTransaction = hashMapOf(
                        "id" to transactionId,
                        "userId" to userId,  // Add userId
                        "accountId" to senderAccount.id,  // Correct: account ID
                        "type" to TransactionType.DEBIT.name,
                        "category" to TransactionCategory.TRANSFER.name,
                        "amount" to amount,
                        "currency" to senderAccount.currency,
                        "description" to (note ?: "Transfer to $recipientName"),
                        "recipientName" to recipientName,
                        "recipientAccount" to recipientAccount.accountNumber,
                        "timestamp" to timestamp,
                        "status" to "COMPLETED",
                        "balanceAfter" to (senderBalance - amount)
                    )

                    val recipientTransaction = hashMapOf(
                        "id" to UUID.randomUUID().toString(),  // Different ID for recipient
                        "userId" to recipientId,  // Recipient's userId
                        "accountId" to recipientAccount.id,  // Recipient's account ID
                        "type" to TransactionType.CREDIT.name,
                        "category" to TransactionCategory.TRANSFER.name,
                        "amount" to amount,
                        "currency" to recipientAccount.currency,
                        "description" to (note ?: "Transfer from $senderName"),
                        "recipientName" to senderName,
                        "recipientAccount" to senderAccount.accountNumber,
                        "timestamp" to timestamp,
                        "status" to "COMPLETED",
                        "balanceAfter" to (recipientBalance + amount)
                    )

                    // Store in top-level transactions collection
                    transaction.set(
                        firestore.collection("transactions").document(transactionId),
                        senderTransaction
                    )

                    transaction.set(
                        firestore.collection("transactions").document(UUID.randomUUID().toString()),
                        recipientTransaction
                    )

                    // Create notification for recipient
                    val notification = hashMapOf(
                        "id" to UUID.randomUUID().toString(),
                        "userId" to recipientId,
                        "title" to "Money Received",
                        "message" to "You received $${String.format("%.2f", amount)} from $senderName",
                        "type" to "TRANSFER_RECEIVED",
                        "timestamp" to timestamp,
                        "isRead" to false,
                        "relatedTransactionId" to transactionId,
                        "relatedAccountId" to recipientAccount.id
                    )

                    transaction.set(
                        firestore.collection("notifications").document(UUID.randomUUID().toString()),
                        notification
                    )

                    transactionId
                }.await().let { transactionId ->
                    // Update local database
                    accountRepository.updateAccountBalance(senderAccount.id, senderBalance - amount)
                    accountRepository.updateAccountBalance(recipientAccount.id, recipientBalance + amount)

                    val transaction = Transaction(
                        id = transactionId,
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
                        balanceAfter = senderBalance - amount
                    )

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        lastTransaction = transaction
                    )

                    onSuccess(transactionId)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Transaction failed: ${e.message}"
                )
                onError(e.message ?: "Unknown error")
            }
        }
    }

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