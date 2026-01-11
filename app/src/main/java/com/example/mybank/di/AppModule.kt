package com.example.mybank.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.example.mybank.data.local.MyBankDatabase
import com.example.mybank.data.local.dao.AccountDao
import com.example.mybank.data.local.dao.BillPaymentDao
import com.example.mybank.data.local.dao.BillerDao
import com.example.mybank.data.local.dao.CardDao
import com.example.mybank.data.local.dao.NotificationDao
import com.example.mybank.data.local.dao.TransactionDao
import com.example.mybank.data.local.dao.UserDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMyBankDatabase(@ApplicationContext context: Context): MyBankDatabase {
        return Room.databaseBuilder(
            context,
            MyBankDatabase::class.java,
            MyBankDatabase.DATABASE_NAME
        )
            .addMigrations(*com.example.mybank.data.local.ALL_MIGRATIONS)
            // Fallback to destructive migration ONLY for development
            // Remove this in production!
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }
    
    @Provides
    @Singleton
    fun provideUserDao(database: MyBankDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    @Singleton
    fun provideAccountDao(database: MyBankDatabase): AccountDao {
        return database.accountDao()
    }
    
    @Provides
    @Singleton
    fun provideTransactionDao(database: MyBankDatabase): TransactionDao {
        return database.transactionDao()
    }
    
    @Provides
    @Singleton
    fun provideNotificationDao(database: MyBankDatabase): NotificationDao {
        return database.notificationDao()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseFunctions(firebaseAuth: FirebaseAuth): FirebaseFunctions {
        // CRITICAL FIX: Use us-central1 region to match deployed functions
        val functions = FirebaseFunctions.getInstance("us-central1")
        android.util.Log.d("AppModule", "FirebaseFunctions initialized for us-central1. Current user: ${firebaseAuth.currentUser?.uid}")
        return functions
    }
    
    @Provides
    @Singleton
    fun provideCardDao(database: MyBankDatabase): CardDao {
        return database.cardDao()
    }
    
    @Provides
    @Singleton
    fun provideBillerDao(database: MyBankDatabase): BillerDao {
        return database.billerDao()
    }
    
    @Provides
    @Singleton
    fun provideBillPaymentDao(database: MyBankDatabase): BillPaymentDao {
        return database.billPaymentDao()
    }
    
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}
