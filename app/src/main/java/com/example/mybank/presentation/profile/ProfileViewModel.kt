package com.example.mybank.presentation.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybank.data.model.UserProfile
import com.example.mybank.data.preferences.PreferencesManager
import com.example.mybank.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUploadingPhoto: Boolean = false,
    val successMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var profileListener: ListenerRegistration? = null

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val userId = firebaseAuth.currentUser?.uid ?: run {
                _uiState.update { 
                    it.copy(isLoading = false, error = "User not authenticated") 
                }
                return@launch
            }

            // Real-time listener for profile changes
            profileListener = firestore.collection("users")
                .document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _uiState.update { 
                            it.copy(isLoading = false, error = error.message) 
                        }
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val profile = snapshot.toObject(UserProfile::class.java)
                        _uiState.update { 
                            it.copy(
                                userProfile = profile,
                                isLoading = false,
                                error = null
                            ) 
                        }
                    } else {
                        // Create profile if doesn't exist
                        createDefaultProfile(userId)
                    }
                }
        }
    }

    private fun createDefaultProfile(userId: String) {
        viewModelScope.launch {
            try {
                val currentUser = firebaseAuth.currentUser
                val defaultProfile = UserProfile(
                    id = userId,
                    displayName = currentUser?.displayName ?: "",
                    email = currentUser?.email ?: "",
                    photoUrl = currentUser?.photoUrl?.toString(),
                    isPremium = false,
                    isFaceIdEnabled = false,
                    is2FAEnabled = false
                )

                firestore.collection("users")
                    .document(userId)
                    .set(defaultProfile)
                    .await()

                _uiState.update { 
                    it.copy(
                        userProfile = defaultProfile,
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to create profile: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun updateProfile(
        displayName: String? = null,
        phone: String? = null,
        address: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val userId = firebaseAuth.currentUser?.uid ?: return@launch
            val currentProfile = _uiState.value.userProfile ?: return@launch

            try {
                val updates = mutableMapOf<String, Any>(
                    "lastUpdated" to System.currentTimeMillis()
                )

                displayName?.let { updates["displayName"] = it }
                phone?.let { updates["phone"] = it }
                address?.let { updates["address"] = it }

                firestore.collection("users")
                    .document(userId)
                    .update(updates)
                    .await()

                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        successMessage = "Profile updated successfully"
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to update profile: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun uploadProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true) }
            
            val userId = firebaseAuth.currentUser?.uid ?: return@launch

            try {
                val photoRef = storage.reference
                    .child("profile_photos/$userId/${System.currentTimeMillis()}.jpg")

                photoRef.putFile(uri).await()
                val downloadUrl = photoRef.downloadUrl.await().toString()

                firestore.collection("users")
                    .document(userId)
                    .update(
                        mapOf(
                            "photoUrl" to downloadUrl,
                            "lastUpdated" to System.currentTimeMillis()
                        )
                    )
                    .await()

                // Update Firebase Auth profile
                val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                    photoUri = Uri.parse(downloadUrl)
                }
                firebaseAuth.currentUser?.updateProfile(profileUpdates)?.await()

                _uiState.update { 
                    it.copy(
                        isUploadingPhoto = false,
                        successMessage = "Photo uploaded successfully"
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isUploadingPhoto = false,
                        error = "Failed to upload photo: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun toggleFaceId(enabled: Boolean) {
        viewModelScope.launch {
            val userId = firebaseAuth.currentUser?.uid ?: return@launch

            try {
                firestore.collection("users")
                    .document(userId)
                    .update(
                        mapOf(
                            "isFaceIdEnabled" to enabled,
                            "lastUpdated" to System.currentTimeMillis()
                        )
                    )
                    .await()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to update setting: ${e.message}") 
                }
            }
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                firebaseAuth.signOut()
                preferencesManager.clearAll()
                onLogoutComplete()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Logout failed: ${e.message}") 
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        profileListener?.remove()
    }
}
