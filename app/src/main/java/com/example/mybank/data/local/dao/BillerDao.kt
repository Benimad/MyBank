package com.example.mybank.data.local.dao

import androidx.room.*
import com.example.mybank.data.model.Biller
import com.example.mybank.data.model.BillerCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface BillerDao {
    
    @Query("SELECT * FROM billers WHERE userId = :userId AND isActive = 1 ORDER BY billerName ASC")
    fun getUserBillers(userId: String): Flow<List<Biller>>
    
    @Query("SELECT * FROM billers WHERE id = :billerId")
    fun getBiller(billerId: String): Flow<Biller?>
    
    @Query("SELECT * FROM billers WHERE userId = :userId AND category = :category AND isActive = 1")
    fun getBillersByCategory(userId: String, category: BillerCategory): Flow<List<Biller>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBiller(biller: Biller)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillers(billers: List<Biller>)
    
    @Update
    suspend fun updateBiller(biller: Biller)
    
    @Query("UPDATE billers SET isActive = :isActive WHERE id = :billerId")
    suspend fun updateBillerStatus(billerId: String, isActive: Boolean)
    
    @Delete
    suspend fun deleteBiller(biller: Biller)
    
    @Query("DELETE FROM billers WHERE id = :billerId")
    suspend fun deleteBillerById(billerId: String)
    
    @Query("DELETE FROM billers WHERE userId = :userId")
    suspend fun deleteUserBillers(userId: String)
}
