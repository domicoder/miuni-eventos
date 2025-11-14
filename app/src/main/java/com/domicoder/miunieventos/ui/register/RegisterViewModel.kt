package com.domicoder.miunieventos.ui.register

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domicoder.miunieventos.data.repository.AuthResult
import com.domicoder.miunieventos.domain.usecase.auth.SignInWithGoogleUseCase
import com.domicoder.miunieventos.domain.usecase.auth.SignUpWithEmailUseCase
import com.domicoder.miunieventos.util.GoogleSignInHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val signUpWithEmailUseCase: SignUpWithEmailUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val googleSignInHelper: GoogleSignInHelper
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _authResult = MutableStateFlow<AuthResult?>(null)
    val authResult: StateFlow<AuthResult?> = _authResult
    
    private val _googleSignInIntent = MutableStateFlow<Intent?>(null)
    val googleSignInIntent: StateFlow<Intent?> = _googleSignInIntent
    
    fun clearGoogleSignInIntent() {
        _googleSignInIntent.value = null
    }
    
    fun createAccount(email: String, password: String, name: String, department: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            signUpWithEmailUseCase(email, password, name, department)
                .fold(
                    onSuccess = { user ->
                        _isLoading.value = false
                        _authResult.value = AuthResult.Success(
                            com.domicoder.miunieventos.data.model.User(
                                id = user.id,
                                name = user.name,
                                email = user.email,
                                photoUrl = user.photoUrl,
                                department = user.department,
                                isOrganizer = user.isOrganizer
                            )
                        )
                    },
                    onFailure = { exception ->
                        _isLoading.value = false
                        _error.value = exception.message ?: "Error al crear la cuenta"
                        _authResult.value = AuthResult.Error(exception.message ?: "Error al crear la cuenta")
                    }
                )
        }
    }
    
    /**
     * Inicia el flujo de Google Sign-In
     * Retorna un Intent que debe ser usado con Activity Result Launcher
     */
    fun startGoogleSignIn(activity: android.app.Activity) {
        try {
            val intent = googleSignInHelper.getSignInIntent(activity)
            _googleSignInIntent.value = intent
        } catch (e: Exception) {
            _error.value = "Error al iniciar Google Sign-In: ${e.message}"
        }
    }
    
    /**
     * Procesa el resultado de Google Sign-In
     */
    fun handleGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val credentialResult = googleSignInHelper.handleSignInResult(data)
            
            credentialResult.fold(
                onSuccess = { credential ->
                    signInWithGoogleUseCase(credential)
                        .fold(
                            onSuccess = { user ->
                                _isLoading.value = false
                                _authResult.value = AuthResult.Success(
                                    com.domicoder.miunieventos.data.model.User(
                                        id = user.id,
                                        name = user.name,
                                        email = user.email,
                                        photoUrl = user.photoUrl,
                                        department = user.department,
                                        isOrganizer = user.isOrganizer
                                    )
                                )
                            },
                            onFailure = { exception ->
                                _isLoading.value = false
                                _error.value = exception.message ?: "Error al autenticar con Google"
                                _authResult.value = AuthResult.Error(exception.message ?: "Error al autenticar con Google")
                            }
                        )
                },
                onFailure = { exception ->
                    _isLoading.value = false
                    _error.value = exception.message ?: "Error al procesar el resultado de Google Sign-In"
                    _authResult.value = AuthResult.Error(exception.message ?: "Error al procesar el resultado de Google Sign-In")
                }
            )
        }
    }
    
    fun signInWithMicrosoft() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // TODO: Implement Microsoft Azure AD Sign-In
                _error.value = "El inicio de sesión con Microsoft se implementará próximamente"
            } catch (e: Exception) {
                _error.value = "Error con Microsoft Sign-In: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun setError(message: String) {
        _error.value = message
    }
    
    fun clearAuthResult() {
        _authResult.value = null
    }
}

