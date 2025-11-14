package com.domicoder.miunieventos.domain.usecase.auth

import com.domicoder.miunieventos.domain.model.User
import com.domicoder.miunieventos.domain.repository.AuthRepository
import com.google.firebase.auth.AuthCredential
import javax.inject.Inject

/**
 * Caso de uso para iniciar sesión con Google
 */
class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(credential: AuthCredential): Result<User> {
        return authRepository.signInWithGoogle(credential)
    }
}

