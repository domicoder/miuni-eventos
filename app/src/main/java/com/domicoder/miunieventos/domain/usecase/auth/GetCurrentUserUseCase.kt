package com.domicoder.miunieventos.domain.usecase.auth

import com.domicoder.miunieventos.domain.model.User
import com.domicoder.miunieventos.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Caso de uso para obtener el usuario actual autenticado
 */
class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): User? {
        return authRepository.getCurrentUser()
    }
}

