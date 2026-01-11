package com.example.mybank.data.repository

import com.example.mybank.data.local.dao.BillPaymentDao
import com.example.mybank.data.local.dao.BillerDao
import com.example.mybank.data.model.BillPayment
import com.example.mybank.data.model.Biller
import com.example.mybank.data.model.BillerCategory
import com.example.mybank.data.model.PaymentStatus
import com.example.mybank.data.model.Transaction
import com.example.mybank.data.model.TransactionCategory
import com.example.mybank.data.model.TransactionType
import com.example.mybank.data.preferences.PreferencesManager
import com.example.mybank.data.remote.MyBankApiService
import com.example.mybank.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillPaymentRepository @Inject constructor(
    private val billerDao: BillerDao,
    private val billPaymentDao: BillPaymentDao,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val apiService: MyBankApiService,
    private val preferencesManager: PreferencesManager
) {
    
    fun getUserBillers(userId: String): Flow<List<Biller>> {
        return billerDao.getUserBillers(userId)
    }
    
    fun getBiller(billerId: String): Flow<Biller?> {
        return billerDao.getBiller(billerId)
    }
    
    fun getBillersByCategory(userId: String, category: BillerCategory): Flow<List<Biller>> {
        return billerDao.getBillersByCategory(userId, category)
    }
    
    fun getUserBillPayments(userId: String): Flow<List<BillPayment>> {
        return billPaymentDao.getUserBillPayments(userId)
    }
    
    fun getBillerPayments(billerId: String): Flow<List<BillPayment>> {
        return billPaymentDao.getBillerPayments(billerId)
    }
    
    fun getBillPayment(paymentId: String): Flow<BillPayment?> {
        return billPaymentDao.getBillPayment(paymentId)
    }
    
    fun getPaymentsByStatus(userId: String, status: PaymentStatus): Flow<List<BillPayment>> {
        return billPaymentDao.getPaymentsByStatus(userId, status)
    }
    
    fun getRecurringPayments(userId: String): Flow<List<BillPayment>> {
        return billPaymentDao.getRecurringPayments(userId)
    }
    
    fun getDuePayments(userId: String): Flow<List<BillPayment>> {
        return billPaymentDao.getDuePayments(userId, System.currentTimeMillis())
    }
    
    suspend fun addBiller(biller: Biller): Resource<Unit> {
        return try {
            billerDao.insertBiller(biller)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add biller")
        }
    }
    
    suspend fun updateBiller(biller: Biller): Resource<Unit> {
        return try {
            billerDao.updateBiller(biller)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update biller")
        }
    }
    
    suspend fun deleteBiller(billerId: String): Resource<Unit> {
        return try {
            billerDao.deleteBillerById(billerId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete biller")
        }
    }
    
    suspend fun payBill(payment: BillPayment): Resource<String> {
        return try {
            val account = accountRepository.getAccount(payment.accountId).first()
            if (account == null) {
                return Resource.Error("Account not found")
            }
            
            if (account.balance < payment.amount) {
                return Resource.Error("Insufficient funds. Available: ${String.format("$%.2f", account.balance)}")
            }
            
            val newBalance = account.balance - payment.amount
            accountRepository.updateAccountBalance(payment.accountId, newBalance)
            
            val completedPayment = payment.copy(
                status = PaymentStatus.COMPLETED,
                completedAt = System.currentTimeMillis()
            )
            billPaymentDao.insertBillPayment(completedPayment)
            
            val biller = billerDao.getBiller(payment.billerId).first()
            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                accountId = payment.accountId,
                type = TransactionType.DEBIT,
                category = TransactionCategory.BILL,
                amount = payment.amount,
                currency = payment.currency,
                description = "Bill Payment: ${biller?.billerName ?: "Unknown"}",
                recipientName = biller?.billerName,
                recipientAccount = biller?.accountNumber,
                timestamp = System.currentTimeMillis(),
                status = "COMPLETED",
                balanceAfter = newBalance
            )
            transactionRepository.insertTransaction(transaction)
            
            Resource.Success(payment.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to process payment")
        }
    }
    
    suspend fun scheduleBillPayment(payment: BillPayment): Resource<Unit> {
        return try {
            val scheduledPayment = payment.copy(status = PaymentStatus.SCHEDULED)
            billPaymentDao.insertBillPayment(scheduledPayment)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to schedule payment")
        }
    }
    
    suspend fun cancelBillPayment(paymentId: String): Resource<Unit> {
        return try {
            billPaymentDao.updatePaymentStatus(paymentId, PaymentStatus.CANCELLED)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to cancel payment")
        }
    }
}
