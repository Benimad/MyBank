package com.example.mybank.data.firebase

import com.example.mybank.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
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
    
    // ==================== ACCOUNTS ====================
    
    suspend fun createAccount(account: Account) {
        firestore.collection("accounts")
            .document(account.id)
            .set(account)
            .await()
    }
    
    fun getUserAccounts(userId: String): Flow<List<Account>> = callbackFlow {
        // Note: This query requires a Firestore composite index
        // Fields: userId (Ascending), isActive (Ascending), createdAt (Descending)

        val listener = try {
            firestore.collection("accounts")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("FirestoreService", "Error getting accounts: ${error.message}")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val accounts = snapshot?.documents?.mapNotNull {
                        it.toObject(Account::class.java)
                    } ?: emptyList()
                    trySend(accounts)
                }
        } catch (e: Exception) {
            android.util.Log.e("FirestoreService", "Failed to create accounts listener: ${e.message}")
            // Fallback query
            firestore.collection("accounts")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val accounts = snapshot?.documents?.mapNotNull {
                        it.toObject(Account::class.java)
                    }?.filter { it.isActive } ?: emptyList()
                    trySend(accounts.sortedByDescending { it.createdAt })
                }
        }
        awaitClose { listener.remove() }
    }
    
    suspend fun getAccount(accountId: String): Account? {
        return firestore.collection("accounts")
            .document(accountId)
            .get()
            .await()
            .toObject(Account::class.java)
    }
    
    suspend fun updateAccountBalance(accountId: String, newBalance: Double) {
        firestore.collection("accounts")
            .document(accountId)
            .update("balance", newBalance)
            .await()
    }
    
    suspend fun executeAtomicInternalTransfer(
        fromAccountId: String,
        toAccountId: String,
        fromNewBalance: Double,
        toNewBalance: Double,
        outgoingTransaction: Transaction,
        incomingTransaction: Transaction
    ) {
        val batch = firestore.batch()
        
        val fromAccountRef = firestore.collection("accounts").document(fromAccountId)
        batch.update(fromAccountRef, "balance", fromNewBalance)
        
        val toAccountRef = firestore.collection("accounts").document(toAccountId)
        batch.update(toAccountRef, "balance", toNewBalance)
        
        val outgoingTransactionRef = firestore.collection("transactions").document(outgoingTransaction.id)
        batch.set(outgoingTransactionRef, outgoingTransaction)
        
        val incomingTransactionRef = firestore.collection("transactions").document(incomingTransaction.id)
        batch.set(incomingTransactionRef, incomingTransaction)
        
        batch.commit().await()
    }
    
    suspend fun executeAtomicP2PTransfer(
        senderAccountId: String,
        recipientAccountId: String,
        senderNewBalance: Double,
        recipientNewBalance: Double,
        senderTransaction: Transaction,
        recipientTransaction: Transaction
    ) {
        val batch = firestore.batch()
        
        val senderAccountRef = firestore.collection("accounts").document(senderAccountId)
        batch.update(senderAccountRef, "balance", senderNewBalance)
        
        val recipientAccountRef = firestore.collection("accounts").document(recipientAccountId)
        batch.update(recipientAccountRef, "balance", recipientNewBalance)
        
        val senderTransactionRef = firestore.collection("transactions").document(senderTransaction.id)
        batch.set(senderTransactionRef, senderTransaction)
        
        val recipientTransactionRef = firestore.collection("transactions").document(recipientTransaction.id)
        batch.set(recipientTransactionRef, recipientTransaction)
        
        batch.commit().await()
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
                    close(error)
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
        val listener = try {
            firestore.collection("transactions")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("FirestoreService", "Error getting transactions: ${error.message}")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val transactions = snapshot?.documents?.mapNotNull {
                        it.toObject(Transaction::class.java)
                    } ?: emptyList()
                    trySend(transactions)
                }
        } catch (e: Exception) {
            android.util.Log.e("FirestoreService", "Failed to create transactions listener: ${e.message}")
            // Fallback query
            firestore.collection("transactions")
                .whereEqualTo("userId", userId)
                .limit(limit.toLong())
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val transactions = snapshot?.documents?.mapNotNull {
                        it.toObject(Transaction::class.java)
                    } ?: emptyList()
                    trySend(transactions.sortedByDescending { it.timestamp })
                }
        }
        awaitClose { listener.remove() }
    }
    
    // ==================== NOTIFICATIONS ====================
    
    suspend fun createNotification(notification: BankNotification) {
        firestore.collection("notifications")
            .document(notification.id)
            .set(notification)
            .await()
    }
    
    fun getUserNotifications(userId: String): Flow<List<BankNotification>> = callbackFlow {
        val listener = try {
            firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("FirestoreService", "Error getting notifications: ${error.message}")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val notifications = snapshot?.documents?.mapNotNull {
                        it.toObject(BankNotification::class.java)
                    } ?: emptyList()
                    trySend(notifications)
                }
        } catch (e: Exception) {
            android.util.Log.e("FirestoreService", "Failed to create notifications listener: ${e.message}")
            // Fallback query
            firestore.collection("notifications")
                .whereEqualTo("userId", userId)
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
        }
        awaitClose { listener.remove() }
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
        // Note: This query requires a Firestore composite index
        // Create it at: https://console.firebase.google.com/v1/r/project/mybank-8deeb/firestore/indexes?create_composite=...
        // Fields: userId (Ascending), createdAt (Descending)

        val listener = try {
            firestore.collection("cards")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        // Log the error but don't close the flow - send empty list instead
                        android.util.Log.e("FirestoreService", "Error getting cards: ${error.message}")
                        android.util.Log.w("FirestoreService", "This might require a Firestore index. Check console.")
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
            // Fallback: try without ordering
            try {
                firestore.collection("cards")
                    .whereEqualTo("userId", userId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        val cards = snapshot?.documents?.mapNotNull {
                            it.toObject(Card::class.java)
                        }?.filter { it.isActive }?: emptyList()
                        // Sort locally as fallback
                        trySend(cards.sortedByDescending { it.createdAt })
                    }
            } catch (fallbackError: Exception) {
                android.util.Log.e("FirestoreService", "Fallback query also failed: ${fallbackError.message}")
                trySend(emptyList())
                return@callbackFlow
            }
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
}
