package com.domicoder.miunieventos.domain.usecase.auth

import com.domicoder.miunieventos.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Caso de uso para cerrar sesión
 */
class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.signOut()
    }
}

