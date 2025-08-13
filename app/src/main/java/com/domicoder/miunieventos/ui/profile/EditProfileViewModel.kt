package com.domicoder.miunieventos.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domicoder.miunieventos.data.model.User
import com.domicoder.miunieventos.data.repository.UserRepository
import com.domicoder.miunieventos.util.UserStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userStateManager: UserStateManager
) : ViewModel() {
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating
    
    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess
    
    init {
        loadUser()
    }
    
    private fun loadUser() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUserId = userStateManager.getCurrentUserId()
                if (currentUserId != null) {
                    val user = userRepository.getUserById(currentUserId)
                    _user.value = user
                    _error.value = null
                } else {
                    _error.value = "No user ID available"
                }
            } catch (e: Exception) {
                _error.value = "Error loading user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateProfile(name: String, department: String) {
        viewModelScope.launch {
            _isUpdating.value = true
            try {
                val currentUser = _user.value
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(
                        name = name,
                        department = department
                    )
                    userRepository.updateUser(updatedUser)
                    _user.value = updatedUser
                    _updateSuccess.value = true
                    _error.value = null
                    
                    // Update the user state manager with the new information
                    userStateManager.updateUserProfile(
                        name = name,
                        department = department
                    )
                } else {
                    _error.value = "User not found"
                }
            } catch (e: Exception) {
                _error.value = "Error updating profile: ${e.message}"
            } finally {
                _isUpdating.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun resetUpdateSuccess() {
        _updateSuccess.value = false
    }
}
