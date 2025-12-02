package com.domicoder.miunieventos.ui.login

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domicoder.miunieventos.data.repository.AuthResult
import com.domicoder.miunieventos.data.model.User
import com.domicoder.miunieventos.domain.usecase.auth.SignInWithEmailUseCase
import com.domicoder.miunieventos.domain.usecase.auth.SignInWithGoogleUseCase
import com.domicoder.miunieventos.util.GoogleSignInHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInWithEmailUseCase: SignInWithEmailUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val googleSignInHelper: GoogleSignInHelper
) : ViewModel() {
    
    companion object {
        private const val TAG = "LoginViewModel"
    }
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _authResult = MutableStateFlow<AuthResult?>(null)
    val authResult: StateFlow<AuthResult?> = _authResult
    
    private val _googleSignInIntent = MutableStateFlow<Intent?>(null)
    val googleSignInIntent: StateFlow<Intent?> = _googleSignInIntent
    
    // Necesitamos hacer el StateFlow mutable para poder limpiarlo desde la UI
    // Usaremos una función para limpiarlo
    fun clearGoogleSignInIntent() {
        _googleSignInIntent.value = null
    }
    
    fun login(email: String, password: String, rememberMe: Boolean = true) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            signInWithEmailUseCase(email, password)
                .fold(
                    onSuccess = { user ->
                        _isLoading.value = false
                        _authResult.value = AuthResult.Success(
                            User(
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
                        _error.value = exception.message ?: "Error al iniciar sesión"
                        _authResult.value = AuthResult.Error(exception.message ?: "Error al iniciar sesión")
                    }
                )
        }
    }
    
    fun setError(message: String) {
        _error.value = message
    }
    
    fun clearAuthResult() {
        _authResult.value = null
    }
    
    /**
     * Inicia el flujo de Google Sign-In
     * Retorna un Intent que debe ser usado con Activity Result Launcher
     */
    fun startGoogleSignIn(activity: android.app.Activity) {
        Log.d(TAG, "startGoogleSignIn called")
        try {
            Log.d(TAG, "Getting sign in intent...")
            val intent = googleSignInHelper.getSignInIntent(activity)
            Log.d(TAG, "Sign in intent received, setting to StateFlow")
            _googleSignInIntent.value = intent
            Log.d(TAG, "Sign in intent set successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting Google Sign-In", e)
            _error.value = "Error al iniciar Google Sign-In: ${e.message}"
        }
    }
    
    /**
     * Procesa el resultado de Google Sign-In
     */
    fun handleGoogleSignInResult(data: Intent?) {
        Log.d(TAG, "handleGoogleSignInResult called, data is null: ${data == null}")
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            Log.d(TAG, "Processing Google Sign-In result...")
            
            val credentialResult = googleSignInHelper.handleSignInResult(data)
            Log.d(TAG, "Credential result received, isSuccess: ${credentialResult.isSuccess}")
            
            credentialResult.fold(
                onSuccess = { credential ->
                    Log.d(TAG, "Credential obtained, calling signInWithGoogleUseCase")
                    signInWithGoogleUseCase(credential)
                        .fold(
                            onSuccess = { user ->
                                Log.d(TAG, "Google sign-in successful, user: ${user.email}")
                                _isLoading.value = false
                                val dataUser = com.domicoder.miunieventos.data.model.User(
                                    id = user.id,
                                    name = user.name,
                                    email = user.email,
                                    photoUrl = user.photoUrl,
                                    department = user.department,
                                    isOrganizer = user.isOrganizer
                                )
                                Log.d(TAG, "Setting authResult to Success")
                                _authResult.value = AuthResult.Success(dataUser)
                                Log.d(TAG, "authResult set successfully")
                            },
                            onFailure = { exception ->
                                Log.e(TAG, "Error in signInWithGoogleUseCase: ${exception.message}", exception)
                                _isLoading.value = false
                                _error.value = exception.message ?: "Error al autenticar con Google"
                                _authResult.value = AuthResult.Error(exception.message ?: "Error al autenticar con Google")
                            }
                        )
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error processing credential result: ${exception.message}", exception)
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
}
