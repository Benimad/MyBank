package com.example.mybank.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybank.data.preferences.PreferencesManager
import com.example.mybank.data.repository.AuthRepository
import com.example.mybank.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        checkAuthStatus()
    }
    
    private fun checkAuthStatus() {
        viewModelScope.launch {
            preferencesManager.isLoggedIn.collect { isLoggedIn ->
                _authState.value = _authState.value.copy(
                    isAuthenticated = isLoggedIn && authRepository.isUserLoggedIn()
                )
            }
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            when (val result = authRepository.loginWithFirebase(email, password)) {
                is Resource.Success -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        successMessage = "Login successful"
                    )
                }
                is Resource.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    _authState.value = _authState.value.copy(isLoading = true)
                }
            }
        }
    }
    
    fun register(email: String, password: String, name: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            when (val result = authRepository.registerWithFirebase(email, password, name)) {
                is Resource.Success -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        successMessage = "Registration successful"
                    )
                }
                is Resource.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    _authState.value = _authState.value.copy(isLoading = true)
                }
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = _authState.value.copy(
                isAuthenticated = false,
                successMessage = "Logged out successfully"
            )
        }
    }
    
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            when (val result = authRepository.resetPassword(email)) {
                is Resource.Success -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        successMessage = "Password reset email sent"
                    )
                }
                is Resource.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    _authState.value = _authState.value.copy(isLoading = true)
                }
            }
        }
    }
    
    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
    
    fun clearSuccessMessage() {
        _authState.value = _authState.value.copy(successMessage = null)
    }
    
    // Google Sign-In
    /**
     * Sign in with Google using ID Token
     * Called from MainActivity after successful Google Sign-In
     */
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is Resource.Success -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        successMessage = "Google Sign-In successful!"
                    )
                }
                is Resource.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.message ?: "Google Sign-In failed"
                    )
                }
                is Resource.Loading -> {
                    _authState.value = _authState.value.copy(isLoading = true)
                }
            }
        }
    }
}
