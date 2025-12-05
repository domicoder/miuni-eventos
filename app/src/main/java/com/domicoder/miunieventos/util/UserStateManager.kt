package com.domicoder.miunieventos.util

import android.util.Log
import com.domicoder.miunieventos.data.mapper.UserMapper
import com.domicoder.miunieventos.data.model.User
import com.domicoder.miunieventos.data.repository.UserRepository
import com.domicoder.miunieventos.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserStateManager @Inject constructor(
    private val userRepository: UserRepository,
    private val authPersistenceManager: AuthPersistenceManager,
    private val authRepository: AuthRepository
) {
    
    companion object {
        private const val TAG = "UserStateManager"
    }
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    private val _isOrganizer = MutableStateFlow(false)
    val isOrganizer: StateFlow<Boolean> = _isOrganizer.asStateFlow()
    
    /**
     * Sets the current user and persists authentication state
     */
    suspend fun setCurrentUserId(userId: String, rememberMe: Boolean = true) {
        try {
            if (userId.isNotEmpty()) {
                var user = userRepository.getUserById(userId)
                
                if (user == null) {
                    Log.d(TAG, "User not found in Firestore for ID: $userId, attempting to sync from Firebase Auth")
                    val domainUser = authRepository.getCurrentUser()
                    if (domainUser != null && domainUser.id == userId) {
                        Log.d(TAG, "Found user in Firebase Auth, syncing to Firestore")
                        user = UserMapper.domainToData(domainUser)
                        val result = userRepository.insertUser(user)
                        if (result.isSuccess) {
                            Log.d(TAG, "User synced to Firestore: ${user.name} (${user.id})")
                        } else {
                            Log.e(TAG, "Failed to sync user to Firestore: ${result.exceptionOrNull()?.message}")
                        }
                    } else {
                        Log.w(TAG, "User not found in Firebase Auth either for ID: $userId")
                    }
                }
                
                if (user != null) {
                    Log.d(TAG, "Setting user state - id: ${user.id}, name: ${user.name}, isOrganizer: ${user.isOrganizer}")
                    _currentUser.value = user
                    _isAuthenticated.value = true
                    _isOrganizer.value = user.isOrganizer
                    Log.d(TAG, "isOrganizer state set to: ${_isOrganizer.value}")
                    
                    authPersistenceManager.saveAuthData(
                        userId = user.id,
                        email = user.email,
                        name = user.name,
                        department = user.department ?: "",
                        isOrganizer = user.isOrganizer,
                        rememberMe = rememberMe
                    )
                    
                    Log.d(TAG, "User set successfully: ${user.name} (${user.id}), isOrganizer: ${user.isOrganizer}, rememberMe: $rememberMe")
                } else {
                    Log.w(TAG, "User not found for ID: $userId after sync attempt")
                    _currentUser.value = null
                    _isAuthenticated.value = false
                    _isOrganizer.value = false
                }
            } else {
                Log.d(TAG, "Empty user ID provided, clearing state")
                _currentUser.value = null
                _isAuthenticated.value = false
                _isOrganizer.value = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting current user ID", e)
            _currentUser.value = null
            _isAuthenticated.value = false
            _isOrganizer.value = false
        }
    }
    
    /**
     * Logs out the current user and clears persistent data
     */
    suspend fun logout() {
        try {
            val currentUserId = _currentUser.value?.id
            Log.d(TAG, "Logging out user: $currentUserId")
            
            _currentUser.value = null
            _isAuthenticated.value = false
            _isOrganizer.value = false
            
            // Clear persistent authentication data
            authPersistenceManager.clearAuthData()
            
            Log.d(TAG, "Logout completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout", e)
            // Even if clearing persistent data fails, clear the in-memory state
            _currentUser.value = null
            _isAuthenticated.value = false
            _isOrganizer.value = false
        }
    }
    
    /**
     * Refreshes user data from the repository
     */
    suspend fun refreshUserData() {
        try {
            _currentUser.value?.let { user ->
                val refreshedUser = userRepository.getUserById(user.id)
                if (refreshedUser != null) {
                    _currentUser.value = refreshedUser
                    _isOrganizer.value = refreshedUser.isOrganizer
                    
                    // Update persistent data with refreshed information
                    authPersistenceManager.updateUserInfo(
                        name = refreshedUser.name,
                        department = refreshedUser.department ?: "",
                        isOrganizer = refreshedUser.isOrganizer
                    )
                    
                    Log.d(TAG, "User data refreshed successfully: ${refreshedUser.name}")
                } else {
                    Log.w(TAG, "User no longer exists, logging out")
                    logout()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing user data", e)
        }
    }
    
    /**
     * Gets the current user ID
     */
    fun getCurrentUserId(): String? {
        return _currentUser.value?.id
    }
    
    /**
     * Attempts to restore authentication state from persistent storage
     * This should be called on app startup to check if user was previously logged in
     */
    suspend fun restoreAuthenticationState(): Boolean {
        return try {
            Log.d(TAG, "Attempting to restore authentication state")
            
            // Check if we have valid stored authentication data
            if (authPersistenceManager.isStoredAuthValid()) {
                val storedUserId = authPersistenceManager.getStoredUserId()
                if (!storedUserId.isNullOrEmpty()) {
                    // Verify the user still exists in Firestore
                    val user = userRepository.getUserById(storedUserId)
                    if (user != null) {
                        // Restore the authentication state
                        Log.d(TAG, "Restoring auth - user found: ${user.name}, isOrganizer: ${user.isOrganizer}")
                        _currentUser.value = user
                        _isAuthenticated.value = true
                        _isOrganizer.value = user.isOrganizer
                        Log.d(TAG, "isOrganizer state restored to: ${_isOrganizer.value}")
                        
                        Log.d(TAG, "Authentication state restored successfully for user: ${user.name}, isOrganizer: ${user.isOrganizer}")
                        return true
                    } else {
                        // User no longer exists, clear stored data
                        Log.w(TAG, "Stored user no longer exists, clearing data")
                        authPersistenceManager.clearAuthData()
                        return false
                    }
                } else {
                    Log.d(TAG, "No stored user ID found")
                    return false
                }
            } else {
                Log.d(TAG, "No valid stored authentication data found")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring authentication state", e)
            // If there's an error reading from DataStore, clear the state
            _currentUser.value = null
            _isAuthenticated.value = false
            _isOrganizer.value = false
            false
        }
    }
    
    /**
     * Updates user profile information and persists the changes
     */
    suspend fun updateUserProfile(
        name: String? = null,
        department: String? = null,
        isOrganizer: Boolean? = null
    ) {
        try {
            // Update the current user object
            _currentUser.value?.let { currentUser ->
                val updatedUser = currentUser.copy(
                    name = name ?: currentUser.name,
                    department = department ?: currentUser.department,
                    organizer = isOrganizer ?: currentUser.isOrganizer
                )
                _currentUser.value = updatedUser
                _isOrganizer.value = updatedUser.isOrganizer
                
                // Update the repository (Firestore)
                val result = userRepository.updateUser(updatedUser)
                if (result.isSuccess) {
                    Log.d(TAG, "User profile updated in Firestore successfully")
                } else {
                    Log.e(TAG, "Failed to update user in Firestore: ${result.exceptionOrNull()?.message}")
                }
                
                // Update persistent data
                authPersistenceManager.updateUserInfo(
                    name = updatedUser.name,
                    department = updatedUser.department ?: "",
                    isOrganizer = updatedUser.isOrganizer
                )
                
                Log.d(TAG, "User profile updated successfully: ${updatedUser.name}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile", e)
        }
    }
    
    suspend fun isCurrentUserStored(): Boolean {
        return try {
            val storedUserId = authPersistenceManager.getStoredUserId()
            val currentUserId = getCurrentUserId()
            val isStored = storedUserId == currentUserId
            
            Log.d(TAG, "Checking if current user is stored - stored: $storedUserId, current: $currentUserId, isStored: $isStored")
            isStored
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if current user is stored", e)
            false
        }
    }
}
