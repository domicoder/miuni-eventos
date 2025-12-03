package com.domicoder.miunieventos.data.mapper

import com.domicoder.miunieventos.data.model.User as DataUser
import com.domicoder.miunieventos.domain.model.User as DomainUser

/**
 * Mapper para convertir entre modelos de datos y modelos de dominio
 */
object UserMapper {
    
    /**
     * Convierte un User de la capa de datos a User de dominio
     */
    fun dataToDomain(dataUser: DataUser): DomainUser {
        return DomainUser(
            id = dataUser.id,
            name = dataUser.name,
            email = dataUser.email,
            photoUrl = dataUser.photoUrl,
            department = dataUser.department,
            isOrganizer = dataUser.isOrganizer
        )
    }
    
    /**
     * Convierte un User de dominio a User de la capa de datos
     */
    fun domainToData(domainUser: DomainUser): DataUser {
        return DataUser(
            id = domainUser.id,
            name = domainUser.name,
            email = domainUser.email,
            photoUrl = domainUser.photoUrl,
            department = domainUser.department,
            organizer = domainUser.isOrganizer
        )
    }
}

