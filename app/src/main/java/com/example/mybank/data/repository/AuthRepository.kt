package com.example.mybank.data.repository

import android.util.Log
import com.example.mybank.data.local.dao.UserDao
import com.example.mybank.data.model.User
import com.example.mybank.data.preferences.PreferencesManager
import com.example.mybank.util.Resource
import com.example.mybank.util.SecurityUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val preferencesManager: PreferencesManager,
    private val userDao: UserDao,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val notificationRepository: NotificationRepository,
    private val cardRepository: CardRepository
) {
    
    companion object {
        private const val TAG = "AuthRepository"
    }
    
    val currentUser: FirebaseUser? get() = firebaseAuth.currentUser
    
    /**
     * Login with Firebase Authentication + Room for offline capability
     * Strategy: Try Firebase first, fallback to Room if offline
     */
    suspend fun loginWithFirebase(email: String, password: String): Resource<FirebaseUser> {
        return try {
            // Try Firebase login first (online)
            try {
                val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = result.user
                
                if (firebaseUser != null) {
                    // Get Firebase token
                    val token = firebaseUser.getIdToken(false).await().token
                    if (token != null) {
                        // Save to Room for offline access
                        val localUser = User(
                            id = firebaseUser.uid,
                            email = firebaseUser.email ?: email,
                            name = firebaseUser.displayName ?: "",
                            passwordHash = SecurityUtils.hashPassword(password),
                            firebaseUid = firebaseUser.uid,
                            lastLoginTimestamp = System.currentTimeMillis()
                        )
                        userDao.insertUser(localUser)
                        
                        // Save session
                        preferencesManager.saveUserSession(
                            token = token,
                            userId = firebaseUser.uid,
                            email = firebaseUser.email ?: email,
                            name = firebaseUser.displayName ?: ""
                        )
                        
                        Log.d(TAG, "Firebase login successful")
                        return Resource.Success(firebaseUser)
                    } else {
                        return Resource.Error("Failed to get authentication token")
                    }
                } else {
                    return Resource.Error("User not found")
                }
            } catch (firebaseException: Exception) {
                Log.w(TAG, "Firebase login failed, trying offline login: ${firebaseException.message}")
                
                // Fallback to Room (offline login)
                return loginOffline(email, password)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login failed: ${e.message}")
            Resource.Error(e.message ?: "Login failed")
        }
    }
    
    /**
     * Offline login using Room database
     */
    private suspend fun loginOffline(email: String, password: String): Resource<FirebaseUser> {
        return try {
            val localUser = userDao.getUserByEmail(email)
            
            if (localUser != null && localUser.passwordHash != null) {
                // Verify password
                if (SecurityUtils.verifyPassword(password, localUser.passwordHash)) {
                    // Update last login
                    userDao.updateUser(localUser.copy(lastLoginTimestamp = System.currentTimeMillis()))
                    
                    // Save session (without Firebase token)
                    preferencesManager.saveUserSession(
                        token = "offline_${localUser.id}",
                        userId = localUser.id,
                        email = localUser.email,
                        name = localUser.name
                    )
                    
                    Log.d(TAG, "Offline login successful")
                    // Return success with FirebaseUser if available
                    val currentUser = firebaseAuth.currentUser
                    if (currentUser != null) {
                        Resource.Success(currentUser)
                    } else {
                        Resource.Error("Offline mode - cannot get Firebase user instance")
                    }
                } else {
                    Resource.Error("Invalid email or password (offline)")
                }
            } else {
                Resource.Error("No offline data available. Please connect to internet to login.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Offline login failed: ${e.message}")
            Resource.Error("Offline login failed: ${e.message}")
        }
    }
    
    /**
     * Register with Firebase Authentication + save to Room
     */
    suspend fun registerWithFirebase(
        email: String,
        password: String,
        name: String
    ): Resource<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            
            if (firebaseUser != null) {
                // Update profile with name
                val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                    displayName = name
                }
                firebaseUser.updateProfile(profileUpdates).await()
                
                // Get Firebase token
                val token = firebaseUser.getIdToken(false).await().token
                if (token != null) {
                    // Save to Room for offline access
                    val localUser = User(
                        id = firebaseUser.uid,
                        email = firebaseUser.email ?: email,
                        name = name,
                        passwordHash = SecurityUtils.hashPassword(password),
                        firebaseUid = firebaseUser.uid,
                        createdAt = System.currentTimeMillis(),
                        lastLoginTimestamp = System.currentTimeMillis()
                    )
                    userDao.insertUser(localUser)
                    
                    // Save session
                    preferencesManager.saveUserSession(
                        token = token,
                        userId = firebaseUser.uid,
                        email = firebaseUser.email ?: email,
                        name = name
                    )
                    
                    Log.d(TAG, "Registration successful: ${firebaseUser.email}")
                    Resource.Success(firebaseUser)
                } else {
                    Resource.Error("Failed to get authentication token")
                }
            } else {
                Resource.Error("Registration failed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed: ${e.message}")
            Resource.Error(e.message ?: "Registration failed")
        }
    }
    
    /**
 * Logout - clear Firebase session and preferences
 * Cleanup listeners to prevent memory leaks
 */
suspend fun logout() {
    try {
        firebaseAuth.signOut()
        preferencesManager.clearAll()

        // Cleanup all Firestore listeners to prevent memory leaks
        accountRepository.stopAllListeners()
        transactionRepository.stopAllListeners()
        notificationRepository.stopAllListeners()
        cardRepository.stopAllListeners()

        Log.d(TAG, "Logout successful")
    } catch (e: Exception) {
        Log.e(TAG, "Logout error: ${e.message}")
    }
}
    
    /**
     * Full logout - clear everything including Room data
     */
    suspend fun logoutAndClearLocalData() {
        try {
            val userId = preferencesManager.userId
            firebaseAuth.signOut()
            preferencesManager.clearAll()
            
            // Optional: Clear Room data
            // userDao.deleteAllUsers()
            
            Log.d(TAG, "Full logout successful")
        } catch (e: Exception) {
            Log.e(TAG, "Full logout error: ${e.message}")
        }
    }
    
    /**
     * Reset password via Firebase
     */
    suspend fun resetPassword(email: String): Resource<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Log.d(TAG, "Password reset email sent to: $email")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Password reset failed: ${e.message}")
            Resource.Error(e.message ?: "Failed to send reset email")
        }
    }
    
    /**
     * Sign in with Google (Firebase)
     */
    suspend fun signInWithGoogle(idToken: String): Resource<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user
            
            if (firebaseUser != null) {
                // Get Firebase token
                val token = firebaseUser.getIdToken(false).await().token
                if (token != null) {
                    // Save to Room (without password for Google users)
                    val localUser = User(
                        id = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        name = firebaseUser.displayName ?: "",
                        passwordHash = null, // No password for Google Sign-In
                        firebaseUid = firebaseUser.uid,
                        profileImageUrl = firebaseUser.photoUrl?.toString(),
                        lastLoginTimestamp = System.currentTimeMillis()
                    )
                    userDao.insertUser(localUser)
                    
                    // Save session
                    preferencesManager.saveUserSession(
                        token = token,
                        userId = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        name = firebaseUser.displayName ?: ""
                    )
                    
                    Log.d(TAG, "Google Sign-In successful")
                    Resource.Success(firebaseUser)
                } else {
                    Resource.Error("Failed to get authentication token")
                }
            } else {
                Resource.Error("Google Sign-In failed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed: ${e.message}")
            Resource.Error(e.message ?: "Google Sign-In failed")
        }
    }
    
    /**
     * Check if user is logged in (Firebase or offline)
     */
    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
    
    /**
     * Get cached user from Room
     */
    suspend fun getCachedUser(email: String): User? {
        return try {
            userDao.getUserByEmail(email)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get cached user: ${e.message}")
            null
        }
    }
}
