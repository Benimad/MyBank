package com.example.mybank.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mybank_preferences")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore
    
    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
        private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
        private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        private val BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("biometric_enabled")
    }
    
    // Auth Token
    val authToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN_KEY]
    }
    
    suspend fun setAuthToken(token: String?) {
        dataStore.edit { preferences ->
            if (token != null) {
                preferences[AUTH_TOKEN_KEY] = token
            } else {
                preferences.remove(AUTH_TOKEN_KEY)
            }
        }
    }
    
    // User ID
    val userId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }
    
    suspend fun setUserId(userId: String?) {
        dataStore.edit { preferences ->
            if (userId != null) {
                preferences[USER_ID_KEY] = userId
            } else {
                preferences.remove(USER_ID_KEY)
            }
        }
    }
    
    // User Email
    val userEmail: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }
    
    suspend fun setUserEmail(email: String?) {
        dataStore.edit { preferences ->
            if (email != null) {
                preferences[USER_EMAIL_KEY] = email
            } else {
                preferences.remove(USER_EMAIL_KEY)
            }
        }
    }
    
    // User Name
    val userName: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }
    
    suspend fun setUserName(name: String?) {
        dataStore.edit { preferences ->
            if (name != null) {
                preferences[USER_NAME_KEY] = name
            } else {
                preferences.remove(USER_NAME_KEY)
            }
        }
    }
    
    // Is Logged In
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN_KEY] ?: false
    }
    
    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN_KEY] = isLoggedIn
        }
    }
    
    // Dark Theme
    val isDarkTheme: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DARK_THEME_KEY] ?: false
    }
    
    suspend fun setDarkTheme(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_THEME_KEY] = isDark
        }
    }
    
    // Notifications Enabled
    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED_KEY] ?: true
    }
    
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }
    
    // Biometric Enabled
    val biometricEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[BIOMETRIC_ENABLED_KEY] ?: false
    }
    
    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[BIOMETRIC_ENABLED_KEY] = enabled
        }
    }
    
    // Clear all preferences (logout)
    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    // Save user session
    suspend fun saveUserSession(token: String, userId: String, email: String, name: String) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
            preferences[USER_EMAIL_KEY] = email
            preferences[USER_NAME_KEY] = name
            preferences[IS_LOGGED_IN_KEY] = true
        }
    }
}
