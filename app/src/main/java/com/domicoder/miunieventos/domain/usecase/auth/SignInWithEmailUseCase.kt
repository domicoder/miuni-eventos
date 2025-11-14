package com.domicoder.miunieventos.domain.usecase.auth

import com.domicoder.miunieventos.domain.model.User
import com.domicoder.miunieventos.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Caso de uso para iniciar sesión con email y contraseña
 */
class SignInWithEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("El email y la contraseña no pueden estar vacíos"))
        }
        return authRepository.signInWithEmail(email, password)
    }
}

