package com.domicoder.miunieventos.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domicoder.miunieventos.data.repository.AuthRepository
import com.domicoder.miunieventos.data.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _authResult = MutableStateFlow<AuthResult?>(null)
    val authResult: StateFlow<AuthResult?> = _authResult
    
    fun login(email: String, password: String, rememberMe: Boolean = true) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = authRepository.authenticateUser(email, password)
                _authResult.value = result
                
                if (result is AuthResult.Error) {
                    _error.value = result.message
                }
            } catch (e: Exception) {
                _error.value = "Error inesperado: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun createAccount(email: String, password: String, name: String, department: String, rememberMe: Boolean = true) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = authRepository.createUser(email, password, name, department)
                _authResult.value = result
                
                if (result is AuthResult.Error) {
                    _error.value = result.message
                }
            } catch (e: Exception) {
                _error.value = "Error inesperado: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearAuthResult() {
        _authResult.value = null
    }
}
