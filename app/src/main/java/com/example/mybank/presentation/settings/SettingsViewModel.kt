package com.example.mybank.presentation.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybank.data.model.Statement
import com.example.mybank.data.preferences.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class SettingsUiState(
    val isDarkTheme: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val biometricEnabled: Boolean = false,
    val userName: String = "",
    val userEmail: String = "",
    val is2FAEnabled: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _statements = MutableStateFlow<List<Statement>>(emptyList())
    val statements = _statements.asStateFlow()

    private val _limits = MutableStateFlow<Map<String, String>>(
        mapOf(
            "Daily Transfer Limit" to "$50,000",
            "Monthly Transfer Limit" to "$500,000",
            "Daily Withdrawal Limit" to "$20,000",
            "Daily POS Transaction Limit" to "$100,000"
        )
    )
    val limits = _limits.asStateFlow()

    private val _fees = MutableStateFlow<Map<String, String>>(
        mapOf(
            "Card Replacement" to "$10.00",
            "Statement Copy" to "$5.00",
            "Stop Payment" to "$15.00",
            "Wire Transfer (Domestic)" to "$25.00",
            "Wire Transfer (International)" to "$45.00"
        )
    )
    val fees = _fees.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    companion object {
        private const val TAG = "SettingsViewModel"
    }

    init {
        loadSettings()
        loadStatements()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            preferencesManager.isDarkTheme.collect { isDark ->
                _uiState.value = _uiState.value.copy(isDarkTheme = isDark)
            }
        }

        viewModelScope.launch {
            preferencesManager.notificationsEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
            }
        }

        viewModelScope.launch {
            preferencesManager.biometricEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(biometricEnabled = enabled)
            }
        }

        viewModelScope.launch {
            preferencesManager.userName.collect { name ->
                _uiState.value = _uiState.value.copy(userName = name ?: "")
            }
        }

        viewModelScope.launch {
            preferencesManager.userEmail.collect { email ->
                _uiState.value = _uiState.value.copy(userEmail = email ?: "")
            }
        }
    }

    private fun loadStatements() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Generate sample statements
                val sampleStatements = listOf(
                    Statement(
                        id = "stmt1",
                        period = "January 2026",
                        generatedDate = System.currentTimeMillis(),
                        totalTransactions = 45,
                        totalSpent = 3542.50,
                        totalIncome = 8500.00,
                        fileUrl = ""
                    ),
                    Statement(
                        id = "stmt2",
                        period = "December 2025",
                        generatedDate = System.currentTimeMillis() - 2629800000L, // ~30 days ago
                        totalTransactions = 52,
                        totalSpent = 4235.00,
                        totalIncome = 7500.00,
                        fileUrl = ""
                    ),
                    Statement(
                        id = "stmt3",
                        period = "November 2025",
                        generatedDate = System.currentTimeMillis() - 5259600000L, // ~60 days ago
                        totalTransactions = 38,
                        totalSpent = 3120.00,
                        totalIncome = 7200.00,
                        fileUrl = ""
                    )
                )
                _statements.value = sampleStatements
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load statements", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun enable2FA() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Simulate 2FA enable
                _uiState.value = _uiState.value.copy(is2FAEnabled = true)
                _successMessage.value = "2FA enabled successfully"
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to enable 2FA"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun disable2FA() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Simulate 2FA disable
                _uiState.value = _uiState.value.copy(is2FAEnabled = false)
                _successMessage.value = "2FA disabled successfully"
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to disable 2FA"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val user = auth.currentUser
                if (user != null) {
                    // In a real app, reauthenticate with current password first
                    // user.reauthenticate(credential)...

                    user.updatePassword(newPassword).await()
                    _successMessage.value = "Password changed successfully"
                } else {
                    _errorMessage.value = "User not authenticated"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to change password", e)
                _errorMessage.value = e.message ?: "Failed to change password"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setError(message: String) {
        _errorMessage.value = message
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    fun toggleDarkTheme() {
        viewModelScope.launch {
            preferencesManager.setDarkTheme(!_uiState.value.isDarkTheme)
        }
    }

    fun toggleNotifications() {
        viewModelScope.launch {
            preferencesManager.setNotificationsEnabled(!_uiState.value.notificationsEnabled)
        }
    }

    fun toggleBiometric() {
        viewModelScope.launch {
            preferencesManager.setBiometricEnabled(!_uiState.value.biometricEnabled)
        }
    }
}
