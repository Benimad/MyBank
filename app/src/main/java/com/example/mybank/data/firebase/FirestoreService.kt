package com.example.mybank.data.firebase

import com.example.mybank.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions,
    private val firebaseAuth: FirebaseAuth
) {

    private suspend fun <T> retryWithExponentialBackoff(
        maxRetries: Int = 3,
        initialDelayMillis: Long = 1000,
        maxDelayMillis: Long = 10000,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMillis
        repeat(maxRetries - 1) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                if (e is FirebaseFunctionsException && e.code == FirebaseFunctionsException.Code.UNAVAILABLE) {
                    android.util.Log.w("FirestoreService", "Retry attempt ${attempt + 1} after ${currentDelay}ms: ${e.message}")
                    kotlinx.coroutines.delay(currentDelay)
                    currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMillis)
                } else {
                    throw e
                }
            }
        }
        return block()
    }

    /**
     * Force refresh the Firebase Auth token to ensure it's valid
     * This fixes UNAUTHENTICATED errors caused by expired tokens
     */
    private suspend fun ensureValidAuthToken() {
        try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                android.util.Log.e("FirestoreService", "No authenticated user found")
                throw Exception("Not authenticated. Please sign in again.")
            }
            
            // Force token refresh
            val tokenResult = currentUser.getIdToken(true).await()
            android.util.Log.d("FirestoreService", "Auth token refreshed for user: ${currentUser.uid}")
            
            // Verify token is valid
            if (tokenResult.token == null) {
                throw Exception("Failed to get valid auth token")
            }
            
            // Small delay to ensure token propagates to Firebase SDK
            kotlinx.coroutines.delay(100)
            
        } catch (e: Exception) {
            android.util.Log.e("FirestoreService", "Failed to refresh auth token: ${e.message}")
            throw Exception("Authentication failed. Please sign out and sign in again.")
        }
    }

    // ==================== USERS ====================

    suspend fun createUser(user: User) {
        firestore.collection("users")
            .document(user.id)
            .set(user)
            .await()
    }

    suspend fun getUser(userId: String): User? {
        return firestore.collection("users")
            .document(userId)
            .get()
            .await()
            .toObject(User::class.java)
    }

    suspend fun updateUser(userId: String, updates: Map<String, Any>) {
        firestore.collection("users")
            .document(userId)
            .update(updates)
            .await()
    }

    suspend fun findUserByEmail(email: String): User? {
        val result = firestore.collection("users")
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .await()
        return result.documents.firstOrNull()?.toObject(User::class.java)
    }

    suspend fun findUserByPhone(phone: String): User? {
        val result = firestore.collection("users")
            .whereEqualTo("phone", phone)
            .limit(1)
            .get()
            .await()
        return result.documents.firstOrNull()?.toObject(User::class.java)
    }

    suspend fun findUserByUsername(username: String): Result<User?> {
        return try {
            val data = hashMapOf("username" to username.lowercase())
            val httpsCallable = functions.getHttpsCallable("findUserByUsername")
            val task = httpsCallable.call(data)
            val result = task.await()
            
            val map = result.getData() as? Map<*, *> ?: throw Exception("Invalid response format")
            val success = map["success"] as? Boolean ?: false
            
            if (success) {
                val userMap = map["user"] as? Map<*, *>
                if (userMap != null) {
                    val user = User(
                        id = userMap["id"] as? String ?: "",
                        name = userMap["name"] as? String ?: "",
                        username = userMap["username"] as? String,
                        profileImageUrl = userMap["profileImageUrl"] as? String
                    )
                    Result.success(user)
                } else {
                    Result.success(null)
                }
            } else {
                Result.failure(Exception(map["error"] as? String ?: "User not found"))
            }
        } catch (e: Exception) {
            android.util.Log.e("FirestoreService", "Find user by username error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun createNewAccount(
        accountName: String,
        accountType: String,
        currency: String = "USD"
    ): Result<Map<String, Any>> {
        return try {
            val data = hashMapOf(
                "accountName" to accountName,
                "accountType" to accountType,
                "currency" to currency
            )
            
            val httpsCallable = functions.getHttpsCallable("createAccount")
            val task = httpsCallable.call(data)
            val result = task.await()
            
            val map = result.getData() as? Map<*, *> ?: throw Exception("Invalid response format")
            val success = map["success"] as? Boolean ?: false
            
            if (success) {
                Result.success(map as Map<String, Any>)
            } else {
                Result.failure(Exception(map["error"] as? String ?: "Failed to create account"))
            }
        } catch (e: FirebaseFunctionsException) {
            android.util.Log.e("FirestoreService", "Create account Firebase error: ${e.message}")
            Result.failure(Exception(e.message ?: "Failed to create account"))
        } catch (e: Exception) {
            android.util.Log.e("FirestoreService", "Create account error: ${e.message}")
            Result.failure(e)
        }
    }

    // ==================== ACCOUNTS ====================

    suspend fun createAccount(account: Account) {
        firestore.collection("accounts")
            .document(account.id)
            .set(account)
            .await()
    }

    suspend fun getAccount(accountId: String): Account? {
        return firestore.collection("accounts")
            .document(accountId)
            .get()
            .await()
            .toObject(Account::class.java)
    }

    fun getUserAccounts(userId: String): Flow<List<Account>> = callbackFlow {
        android.util.Log.d("FirestoreService", "Setting up listener for userId: $userId")
        val listener = firestore.collection("accounts")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirestoreService", "Error getting accounts: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                android.util.Log.d("FirestoreService", "Received ${snapshot?.documents?.size ?: 0} documents from Firestore")
                val accounts = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Account(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            accountNumber = doc.getString("accountNumber") ?: "",
                            accountName = doc.getString("accountName") ?: "",
                            accountType = AccountType.valueOf(doc.getString("accountType") ?: "CHECKING"),
                            balance = doc.getDouble("balance") ?: 0.0,
                            currency = doc.getString("currency") ?: "USD",
                            iban = doc.getString("iban"),
                            createdAt = (doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()),
                            isActive = doc.getBoolean("isActive") ?: true
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("FirestoreService", "Error parsing account ${doc.id}: ${e.message}")
                        null
                    }
                }?.sortedByDescending { it.createdAt } ?: emptyList()
                android.util.Log.d("FirestoreService", "Sending ${accounts.size} accounts")
                trySend(accounts)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateAccount(accountId: String, updates: Map<String, Any>) {
        firestore.collection("accounts")
            .document(accountId)
            .update(updates)
            .await()
    }

    suspend fun deleteAccount(accountId: String) {
        firestore.collection("accounts")
            .document(accountId)
            .delete()
            .await()
    }

    // ==================== TRANSACTIONS ====================

    suspend fun createTransaction(transaction: Transaction) {
        firestore.collection("transactions")
            .document(transaction.id)
            .set(transaction)
            .await()
    }

    fun getAccountTransactions(accountId: String, limit: Int = 50): Flow<List<Transaction>> = callbackFlow {
        val listener = firestore.collection("transactions")
            .whereEqualTo("accountId", accountId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val transactions = snapshot?.documents?.mapNotNull {
                    it.toObject(Transaction::class.java)
                } ?: emptyList()
                trySend(transactions)
            }
        awaitClose { listener.remove() }
    }

    fun getUserTransactions(userId: String, limit: Int = 50): Flow<List<Transaction>> = callbackFlow {
        val listener = firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val transactions = snapshot?.documents?.mapNotNull {
                    it.toObject(Transaction::class.java)
                } ?: emptyList()
                trySend(transactions)
            }
        awaitClose { listener.remove() }
    }

    // ==================== NOTIFICATIONS ====================

    fun getUserNotifications(userId: String): Flow<List<BankNotification>> = callbackFlow {
        val listener = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val notifications = snapshot?.documents?.mapNotNull {
                    it.toObject(BankNotification::class.java)
                } ?: emptyList()
                trySend(notifications.sortedByDescending { it.timestamp })
            }
        awaitClose { listener.remove() }
    }

    suspend fun createNotification(notification: BankNotification) {
        firestore.collection("notifications")
            .document(notification.id)
            .set(notification)
            .await()
    }

    suspend fun markNotificationAsRead(notificationId: String) {
        firestore.collection("notifications")
            .document(notificationId)
            .update("isRead", true)
            .await()
    }

    suspend fun deleteNotification(notificationId: String) {
        firestore.collection("notifications")
            .document(notificationId)
            .delete()
            .await()
    }

    // ==================== CARDS ====================

    suspend fun createCard(card: Card) {
        firestore.collection("cards")
            .document(card.id)
            .set(card)
            .await()
    }

    fun getUserCards(userId: String): Flow<List<Card>> = callbackFlow {
        val listener = try {
            firestore.collection("cards")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("FirestoreService", "Error getting cards: ${error.message}")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val cards = snapshot?.documents?.mapNotNull {
                        it.toObject(Card::class.java)
                    }?.filter { it.isActive } ?: emptyList()
                    trySend(cards)
                }
        } catch (e: Exception) {
            android.util.Log.e("FirestoreService", "Failed to create cards listener: ${e.message}")
            trySend(emptyList())
            return@callbackFlow
        }
        awaitClose { listener.remove() }
    }

    suspend fun updateCard(cardId: String, updates: Map<String, Any>) {
        firestore.collection("cards")
            .document(cardId)
            .update(updates)
            .await()
    }

    // ==================== REAL-TIME MONEY FLOW: CLOUD FUNCTIONS ====================
    // These functions use Firestore transactions on the backend for atomic money flow

    /**
     * Process transfer between accounts atomically using Cloud Function
     * @param fromAccountId Source account ID
     * @param toAccountId Destination account ID
     * @param amount Amount to transfer
     * @param currency Currency code (default: USD)
     * @param description Transfer description
     * @return Result with success status and new balances
     */
    suspend fun processTransfer(
        fromAccountId: String,
        toAccountId: String,
        amount: Double,
        currency: String = "USD",
        description: String? = null,
        idempotencyKey: String? = null
    ): Result<TransferResult> {
        return try {
            val currentUser = firebaseAuth.currentUser
            val currentUserId = currentUser?.uid
            
            android.util.Log.d("FirestoreService", "processTransfer: Starting transfer from=$fromAccountId to=$toAccountId amount=$amount user=$currentUserId")
            
            // CRITICAL FIX: Refresh auth token before calling Cloud Function
            ensureValidAuthToken()
            
            val key = idempotencyKey ?: "${System.currentTimeMillis()}-${java.util.UUID.randomUUID()}"
            val data = hashMapOf(
                "fromAccountId" to fromAccountId,
                "toAccountId" to toAccountId,
                "amount" to amount,
                "currency" to currency,
                "description" to (description ?: "Transfer"),
                "idempotencyKey" to key
            )

            android.util.Log.d("FirestoreService", "processTransfer: Calling Cloud Function with data: $data")

            val result = retryWithExponentialBackoff {
                val httpsCallable = functions.getHttpsCallable("processTransfer")
                val task = httpsCallable.call(data)
                task.await()
            }

            android.util.Log.d("FirestoreService", "processTransfer: Received response from Cloud Function")

            val map = result.getData() as? Map<*, *> ?: throw Exception("Invalid response format")
            val success = map["success"] as? Boolean ?: false

            if (success) {
                android.util.Log.d("FirestoreService", "processTransfer: Transfer successful")
                Result.success(
                    TransferResult(
                        success = true,
                        fromBalance = (map["fromBalance"] as? Number)?.toDouble() ?: 0.0,
                        toBalance = (map["toBalance"] as? Number)?.toDouble() ?: 0.0,
                        transactionId = map["transactionId"] as? String
                    )
                )
            } else {
                val errorMsg = map["error"] as? String ?: "Transfer failed"
                android.util.Log.e("FirestoreService", "processTransfer: Transfer failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: FirebaseFunctionsException) {
            val currentUser = firebaseAuth.currentUser
            android.util.Log.e("FirestoreService", "Transfer Firebase error: code=${e.code}, message=${e.message}, currentUser=${currentUser?.uid}")
            
            val errorMessage = when (e.code) {
                FirebaseFunctionsException.Code.UNAUTHENTICATED -> 
                    "Authentication error. Current user: ${currentUser?.uid}. Please sign out completely and sign in again."
                FirebaseFunctionsException.Code.PERMISSION_DENIED ->
                    "Permission denied. ${e.message}"
                FirebaseFunctionsException.Code.FAILED_PRECONDITION ->
                    e.message ?: "Transaction requirements not met"
                else ->
                    e.message ?: "Transfer failed: ${e.code}"
            }
            
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            android.util.Log.e("FirestoreService", "Transfer error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Process deposit to account atomically using Cloud Function
     * @param accountId Target account ID
     * @param amount Deposit amount
     * @param currency Currency code (default: USD)
     * @param description Deposit description
     * @param paymentMethod Payment method used
     * @return Result with success status and new balance
     */
    suspend fun processDeposit(
        accountId: String,
        amount: Double,
        currency: String = "USD",
        description: String? = null,
        paymentMethod: String? = null,
        idempotencyKey: String? = null
    ): Result<BalanceResult> {
        return try {
            val key = idempotencyKey ?: "${System.currentTimeMillis()}-${java.util.UUID.randomUUID()}"
            val data = hashMapOf(
                "accountId" to accountId,
                "amount" to amount,
                "currency" to currency,
                "description" to (description ?: "Deposit"),
                "paymentMethod" to (paymentMethod ?: "CASH"),
                "idempotencyKey" to key
            )

            val result = retryWithExponentialBackoff {
                val httpsCallable = functions.getHttpsCallable("processDeposit")
                val task = httpsCallable.call(data)
                task.await()
            }

            val map = result.getData() as? Map<*, *> ?: throw Exception("Invalid response format")
            val success = map["success"] as? Boolean ?: false

            if (success) {
                Result.success(
                    BalanceResult(
                        success = true,
                        balance = (map["balance"] as? Number)?.toDouble() ?: 0.0,
                        transactionId = map["transactionId"] as? String
                    )
                )
            } else {
                Result.failure(Exception(map["error"] as? String ?: "Deposit failed"))
            }
        } catch (e: FirebaseFunctionsException) {
            android.util.Log.e("FirestoreService", "Deposit Firebase error: code=${e.code}, message=${e.message}")
            Result.failure(Exception(e.message ?: "Deposit failed"))
        } catch (e: Exception) {
            android.util.Log.e("FirestoreService", "Deposit error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Process withdrawal from account atomically using Cloud Function
     * @param accountId Source account ID
     * @param amount Withdrawal amount
     * @param currency Currency code (default: USD)
     * @param description Withdrawal description
     * @param paymentMethod Payment method used
     * @return Result with success status and new balance
     */
    suspend fun processWithdrawal(
        accountId: String,
        amount: Double,
        currency: String = "USD",
        description: String? = null,
        paymentMethod: String? = null,
        idempotencyKey: String? = null
    ): Result<BalanceResult> {
        return try {
            val key = idempotencyKey ?: "${System.currentTimeMillis()}-${java.util.UUID.randomUUID()}"
            val data = hashMapOf(
                "accountId" to accountId,
                "amount" to amount,
                "currency" to currency,
                "description" to (description ?: "Withdrawal"),
                "paymentMethod" to (paymentMethod ?: "CASH"),
                "idempotencyKey" to key
            )

            val result = retryWithExponentialBackoff {
                val httpsCallable = functions.getHttpsCallable("processWithdrawal")
                val task = httpsCallable.call(data)
                task.await()
            }

            val map = result.getData() as? Map<*, *> ?: throw Exception("Invalid response format")
            val success = map["success"] as? Boolean ?: false

            if (success) {
                Result.success(
                    BalanceResult(
                        success = true,
                        balance = (map["balance"] as? Number)?.toDouble() ?: 0.0,
                        transactionId = map["transactionId"] as? String
                    )
                )
            } else {
                Result.failure(Exception(map["error"] as? String ?: "Withdrawal failed"))
            }
        } catch (e: FirebaseFunctionsException) {
            android.util.Log.e("FirestoreService", "Withdrawal Firebase error: code=${e.code}, message=${e.message}")
            Result.failure(Exception(e.message ?: "Withdrawal failed"))
        } catch (e: Exception) {
            android.util.Log.e("FirestoreService", "Withdrawal error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Get current account balance atomically using Cloud Function
     * @param accountId Account ID
     * @return Result with balance information
     */
    suspend fun getAccountBalance(accountId: String): Result<AccountBalanceInfo> {
        return try {
            val data = hashMapOf(
                "accountId" to accountId
            )

            val httpsCallable = functions.getHttpsCallable("getAccountBalance")
            val task = httpsCallable.call(data)
            val result = task.await()

            val map = result.getData() as? Map<*, *> ?: throw Exception("Invalid response format")
            val success = map["success"] as? Boolean ?: false

            if (success) {
                Result.success(
                    AccountBalanceInfo(
                        balance = (map["balance"] as? Number)?.toDouble() ?: 0.0,
                        accountId = accountId,
                        currency = map["currency"] as? String ?: "USD",
                        updatedAt = map["updatedAt"] as? Long
                    )
                )
            } else {
                Result.failure(Exception(map["error"] as? String ?: "Failed to get balance"))
            }
        } catch (e: FirebaseFunctionsException) {
            android.util.Log.e("FirestoreService", "Get balance Firebase error: code=${e.code}, message=${e.message}")
            Result.failure(Exception(e.message ?: "Failed to get balance"))
        } catch (e: Exception) {
            android.util.Log.e("FirestoreService", "Get balance error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Result data class for transfer operations
     */
    data class TransferResult(
        val success: Boolean,
        val fromBalance: Double,
        val toBalance: Double,
        val transactionId: String?
    )

    /**
     * Result data class for balance operations (deposit/withdrawal)
     */
    data class BalanceResult(
        val success: Boolean,
        val balance: Double,
        val transactionId: String?
    )

    /**
     * Result data class for balance queries
     */
    data class AccountBalanceInfo(
        val balance: Double,
        val accountId: String,
        val currency: String,
        val updatedAt: Long?
    )
}