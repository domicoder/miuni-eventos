package com.domicoder.miunieventos.domain.model

/**
 * Entidad de dominio para User
 * Esta es la representación pura del modelo de negocio, sin dependencias de frameworks
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String?,
    val department: String?,
    val isOrganizer: Boolean = false
)

