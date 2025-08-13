package com.domicoder.miunieventos.util

import android.util.Log
import com.domicoder.miunieventos.data.model.User
import com.domicoder.miunieventos.data.repository.AuthRepository
import com.domicoder.miunieventos.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginStateManager @Inject constructor(
    private val authRepository: AuthRepository,
    private val userStateManager: UserStateManager,
    private val authPersistenceManager: AuthPersistenceManager
) {
    
    companion object {
        private const val TAG = "LoginStateManager"
    }
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    suspend fun login(email: String, password: String, rememberMe: Boolean = true): Boolean {
        _isLoading.value = true
        _error.value = null
        
        Log.d(TAG, "Attempting login for email: $email, rememberMe: $rememberMe")
        
        return try {
            val result = authRepository.authenticateUser(email, password)
            when (result) {
                is AuthResult.Success -> {
                    Log.d(TAG, "Login successful for user: ${result.user.name}")
                    // Set the current user in UserStateManager (this will also persist the data)
                    userStateManager.setCurrentUserId(result.user.id, rememberMe)
                    true
                }
                is AuthResult.Error -> {
                    Log.w(TAG, "Login failed: ${result.message}")
                    _error.value = result.message
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during login", e)
            _error.value = "Error inesperado: ${e.message}"
            false
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun createAccount(email: String, password: String, name: String, department: String, rememberMe: Boolean = true): Boolean {
        _isLoading.value = true
        _error.value = null
        
        Log.d(TAG, "Attempting to create account for email: $email, name: $name, rememberMe: $rememberMe")
        
        return try {
            val result = authRepository.createUser(email, password, name, department)
            when (result) {
                is AuthResult.Success -> {
                    Log.d(TAG, "Account creation successful for user: ${result.user.name}")
                    // Set the current user in UserStateManager (this will also persist the data)
                    userStateManager.setCurrentUserId(result.user.id, rememberMe)
                    true
                }
                is AuthResult.Error -> {
                    Log.w(TAG, "Account creation failed: ${result.message}")
                    _error.value = result.message
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during account creation", e)
            _error.value = "Error inesperado: ${e.message}"
            false
        } finally {
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Attempts to restore authentication state from persistent storage
     * This is a convenience method that delegates to UserStateManager
     */
    suspend fun restoreAuthenticationState(): Boolean {
        Log.d(TAG, "Attempting to restore authentication state")
        return try {
            val restored = userStateManager.restoreAuthenticationState()
            if (restored) {
                Log.d(TAG, "Authentication state restored successfully")
            } else {
                Log.d(TAG, "No valid authentication state found, user needs to login")
            }
            restored
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring authentication state", e)
            false
        }
    }
    
    /**
     * Logs out the current user and clears all persistent data
     */
    suspend fun logout() {
        Log.d(TAG, "Logging out current user")
        try {
            userStateManager.logout()
            Log.d(TAG, "Logout completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout", e)
        }
    }
}
