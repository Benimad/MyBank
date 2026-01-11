package com.example.mybank.data.local.dao

import androidx.room.*
import com.example.mybank.data.model.BillPayment
import com.example.mybank.data.model.PaymentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BillPaymentDao {
    
    @Query("SELECT * FROM bill_payments WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserBillPayments(userId: String): Flow<List<BillPayment>>
    
    @Query("SELECT * FROM bill_payments WHERE billerId = :billerId ORDER BY createdAt DESC")
    fun getBillerPayments(billerId: String): Flow<List<BillPayment>>
    
    @Query("SELECT * FROM bill_payments WHERE id = :paymentId")
    fun getBillPayment(paymentId: String): Flow<BillPayment?>
    
    @Query("SELECT * FROM bill_payments WHERE userId = :userId AND status = :status ORDER BY createdAt DESC")
    fun getPaymentsByStatus(userId: String, status: PaymentStatus): Flow<List<BillPayment>>
    
    @Query("SELECT * FROM bill_payments WHERE userId = :userId AND isRecurring = 1 ORDER BY nextPaymentDate ASC")
    fun getRecurringPayments(userId: String): Flow<List<BillPayment>>
    
    @Query("SELECT * FROM bill_payments WHERE userId = :userId AND status = 'SCHEDULED' AND scheduledDate <= :currentTime")
    fun getDuePayments(userId: String, currentTime: Long): Flow<List<BillPayment>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillPayment(payment: BillPayment)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillPayments(payments: List<BillPayment>)
    
    @Update
    suspend fun updateBillPayment(payment: BillPayment)
    
    @Query("UPDATE bill_payments SET status = :status WHERE id = :paymentId")
    suspend fun updatePaymentStatus(paymentId: String, status: PaymentStatus)
    
    @Query("UPDATE bill_payments SET status = :status, completedAt = :completedAt WHERE id = :paymentId")
    suspend fun completePayment(paymentId: String, status: PaymentStatus, completedAt: Long)
    
    @Delete
    suspend fun deleteBillPayment(payment: BillPayment)
    
    @Query("DELETE FROM bill_payments WHERE id = :paymentId")
    suspend fun deletePaymentById(paymentId: String)
}
