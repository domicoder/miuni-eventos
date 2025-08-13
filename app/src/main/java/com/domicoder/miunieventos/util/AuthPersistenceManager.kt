package com.domicoder.miunieventos.util

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages persistent storage of authentication state using SharedPreferences.
 * This is a temporary implementation to get the project building.
 * In production, this should be replaced with DataStore for better performance and type safety.
 */
@Singleton
class AuthPersistenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "AuthPersistenceManager"
        private const val DATASTORE_NAME = "auth_preferences"
    }
    
    /**
     * Flow that emits the current authentication state
     */
    val isAuthenticated: Flow<Boolean> = flowOf(
        context.getSharedPreferences(DATASTORE_NAME, Context.MODE_PRIVATE)
            .getBoolean("is_authenticated", false)
    )
    
    /**
     * Flow that emits the current user ID
     */
    val userId: Flow<String?> = flowOf(
        context.getSharedPreferences(DATASTORE_NAME, Context.MODE_PRIVATE)
            .getString("user_id", null)
    )
    
    /**
     * Flow that emits the current user email
     */
    val userEmail: Flow<String?> = flowOf(
        context.getSharedPreferences(DATASTORE_NAME, Context.MODE_PRIVATE)
            .getString("user_email", null)
    )
    
    /**
     * Flow that emits the current user name
     */
    val userName: Flow<String?> = flowOf(
        context.getSharedPreferences(DATASTORE_NAME, Context.MODE_PRIVATE)
            .getString("user_name", null)
    )
    
    /**
     * Flow that emits the current user department
     */
    val userDepartment: Flow<String?> = flowOf(
        context.getSharedPreferences(DATASTORE_NAME, Context.MODE_PRIVATE)
            .getString("user_department", null)
    )
    
    /**
     * Flow that emits whether the current user is an organizer
     */
    val isOrganizer: Flow<Boolean> = flowOf(
        context.getSharedPreferences(DATASTORE_NAME, Context.MODE_PRIVATE)
            .getBoolean("is_organizer", false)
    )
    
    /**
     * Flow that emits the last login timestamp
     */
    val lastLoginTimestamp: Flow<Long> = flowOf(
        context.getSharedPreferences(DATASTORE_NAME, Context.MODE_PRIVATE)
            .getLong("last_login_timestamp", 0L)
    )
    
    /**
     * Flow that emits the remember me preference
     */
    val rememberMe: Flow<Boolean> = flowOf(
        context.getSharedPreferences(DATASTORE_NAME, Context.MODE_PRIVATE)
            .getBoolean("remember_me", false)
    )
    
    /**
     * Saves authentication data to persistent storage
     */
    suspend fun saveAuthData(
        userId: String,
        email: String,
        name: String,
        department: String,
        isOrganizer: Boolean,
        rememberMe: Boolean = true
    ) {
        try {
            val sharedPrefs = context.getSharedPreferences(DATASTORE_NAME, Context.MODE_PRIVATE)
            sharedPrefs.edit().apply {
                putBoolean("is_authenticated", true)
                putString("user_id", userId)
                putString("user_email", email)
                putString("user_name", name)
                putString("user_department", department)
                putBoolean("is_organizer", isOrganizer)
                putLong("last_login_timestamp", System.currentTimeMillis())
                putBoolean("remember_me", rememberMe)
            }.apply()
            
            Log.d(TAG, "Authentication data saved successfully for user: $userId, rememberMe: $rememberMe")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving authentication data", e)
            throw e
        }
    }
    
    /**
     * Clears all authentication data from persistent storage
     */
    suspend fun clearAuthData() {
        try {
            val sharedPrefs = context.getSharedPreferences(DATASTORE_NAME, Context.MODE_PRIVATE)
            sharedPrefs.edit().clear().apply()
            Log.d(TAG, "Authentication data cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing authentication data", e)
            throw e
        }
    }
    
    /**
     * Updates the remember me preference
     */
    suspend fun setRememberMe(enabled: Boolean) {
        try {
            val sharedPrefs = context.getSharedPreferences(DATASTORE_NAME, Context.MODE_PRIVATE)
            sharedPrefs.edit().putBoolean("remember_me", enabled).apply()
            Log.d(TAG, "Remember me preference updated: $enabled")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating remember me preference", e)
            throw e
        }
    }
    
    /**
     * Updates user information (useful for profile updates)
     */
    suspend fun updateUserInfo(
        name: String? = null,
        department: String? = null,
        isOrganizer: Boolean? = null
    ) {
        try {
            val sharedPrefs = context.getSharedPreferences(DATASTORE_NAME, Context.MODE_PRIVATE)
            sharedPrefs.edit().apply {
                name?.let { putString("user_name", it) }
                department?.let { putString("user_department", it) }
                isOrganizer?.let { putBoolean("is_organizer", it) }
            }.apply()
            Log.d(TAG, "User info updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user info", e)
            throw e
        }
    }
    
    /**
     * Checks if the stored authentication data is still valid
     * (e.g., not expired, user still exists, etc.)
     */
    suspend fun isStoredAuthValid(): Boolean {
        return try {
            // For now, use a simple approach to get the project building
            // This can be replaced with proper DataStore implementation later
            val sharedPrefs = context.getSharedPreferences(DATASTORE_NAME, Context.MODE_PRIVATE)
            val isAuthenticated = sharedPrefs.getBoolean("is_authenticated", false)
            val userId = sharedPrefs.getString("user_id", null)
            val rememberMe = sharedPrefs.getBoolean("remember_me", false)
            
            Log.d(TAG, "Checking stored auth validity - isAuthenticated: $isAuthenticated, userId: $userId, rememberMe: $rememberMe")
            
            // If remember me is disabled, don't auto-login
            if (!rememberMe) {
                Log.d(TAG, "Remember me is disabled, auth not valid")
                return false
            }
            
            // Check if we have valid authentication data
            val isValid = isAuthenticated && !userId.isNullOrEmpty()
            Log.d(TAG, "Stored auth validity: $isValid")
            isValid
        } catch (e: Exception) {
            Log.e(TAG, "Error checking stored auth validity", e)
            false
        }
    }
    
    /**
     * Gets the stored user ID synchronously (use sparingly)
     */
    suspend fun getStoredUserId(): String? {
        return try {
            val sharedPrefs = context.getSharedPreferences(DATASTORE_NAME, Context.MODE_PRIVATE)
            val userId = sharedPrefs.getString("user_id", null)
            Log.d(TAG, "Retrieved stored user ID: $userId")
            userId
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving stored user ID", e)
            null
        }
    }
    
    /**
     * Gets the stored authentication state synchronously (use sparingly)
     */
    suspend fun getStoredAuthState(): Boolean {
        return try {
            val sharedPrefs = context.getSharedPreferences(DATASTORE_NAME, Context.MODE_PRIVATE)
            val authState = sharedPrefs.getBoolean("is_authenticated", false)
            Log.d(TAG, "Retrieved stored auth state: $authState")
            authState
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving stored auth state", e)
            false
        }
    }
}
