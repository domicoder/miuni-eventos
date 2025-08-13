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
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userStateManager: UserStateManager
) : ViewModel() {
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
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
    
    fun refreshUser() {
        loadUser()
    }
    
    fun getCurrentUserId(): String {
        return userStateManager.getCurrentUserId() ?: ""
    }
}
