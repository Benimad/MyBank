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
import com.example.mybank.data.preferences.PreferencesManager
import com.example.mybank.data.remote.MyBankApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    // Base URL - IMPORTANT: Replace this with your actual API endpoint!
    // This is currently a placeholder URL and will cause all API calls to fail
    private const val BASE_URL = "https://api.mybank.example.com/api/v1/"
    
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }
    
    @Provides
    @Singleton
    fun provideAuthInterceptor(preferencesManager: PreferencesManager): Interceptor {
        return Interceptor { chain ->
            val token = runBlocking {
                preferencesManager.authToken.first()
            }
            val request = chain.request().newBuilder()
            if (token != null) {
                request.addHeader("Authorization", "Bearer $token")
            }
            chain.proceed(request.build())
        }
    }
    
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideMyBankApiService(retrofit: Retrofit): MyBankApiService {
        return retrofit.create(MyBankApiService::class.java)
    }
    
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
