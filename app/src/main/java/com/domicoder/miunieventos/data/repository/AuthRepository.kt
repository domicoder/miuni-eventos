package com.domicoder.miunieventos.data.repository

import com.domicoder.miunieventos.data.model.User
import com.domicoder.miunieventos.data.remote.UserRemoteDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val userRemoteDataSource: UserRemoteDataSource
) {

    suspend fun authenticateUser(email: String, password: String): AuthResult {
        return try {
            val user = userRemoteDataSource.getUserByEmail(email)
            if (user != null) {
                if (isValidPassword(email, password)) {
                    AuthResult.Success(user)
                } else {
                    AuthResult.Error("Contraseña incorrecta")
                }
            } else {
                AuthResult.Error("Usuario no encontrado")
            }
        } catch (e: Exception) {
            AuthResult.Error("Error de autenticación: ${e.message}")
        }
    }

    suspend fun createUser(email: String, password: String, name: String, department: String): AuthResult {
        return try {
            // Check if user already exists
            val existingUser = userRemoteDataSource.getUserByEmail(email)
            if (existingUser != null) {
                return AuthResult.Error("El usuario ya existe")
            }
            
            val newUser = User(
                id = generateUserId(email),
                name = name,
                email = email,
                photoUrl = null,
                department = department,
                organizer = false
            )
            
            val result = userRemoteDataSource.insertUser(newUser)
            if (result.isSuccess) {
                AuthResult.Success(newUser)
            } else {
                AuthResult.Error("Error al crear usuario: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            AuthResult.Error("Error al crear usuario: ${e.message}")
        }
    }

    suspend fun getUserById(userId: String): User? {
        return userRemoteDataSource.getUserById(userId)
    }
    
    private fun isValidPassword(email: String, password: String): Boolean {
        // For legacy support with demo accounts
        // In production, use Firebase Auth for password verification
        return when (email) {
            "juanito.alimana@unicda.edu.do" -> password == "123456"
            "maria.gonzalez@unicda.edu.do" -> password == "123456"
            "carlos.rodriguez@unicda.edu.do" -> password == "123456"
            else -> password.isNotBlank() && password.length >= 6
        }
    }
    
    private fun generateUserId(email: String): String {
        // Generate a unique user ID based on email
        return email.replace("@", "_").replace(".", "_")
    }
}

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
