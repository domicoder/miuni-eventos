package com.domicoder.miunieventos.data.repository

import com.domicoder.miunieventos.data.local.UserDao
import com.domicoder.miunieventos.data.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao
) {

//    TODO: replace with Firebase auth
    suspend fun authenticateUser(email: String, password: String): AuthResult {
        return try {
            val user = userDao.getUserByEmail(email)
            if (user != null) {
                // In a real app, you would hash and verify the password
                // For MVP, we'll use a simple check
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

//    TODO: replace with Firebase auth
    suspend fun createUser(email: String, password: String, name: String, department: String): AuthResult {
        return try {
            // Check if user already exists
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                return AuthResult.Error("El usuario ya existe")
            }
            
            // Create new user
            val newUser = User(
                id = generateUserId(email),
                name = name,
                email = email,
                photoUrl = null,
                department = department,
                isOrganizer = false // Default to false, can be changed later
            )
            
            userDao.insertUser(newUser)
            AuthResult.Success(newUser)
        } catch (e: Exception) {
            AuthResult.Error("Error al crear usuario: ${e.message}")
        }
    }

//    TODO: check is needed, or remove it
    suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)
    }
    
    private fun isValidPassword(email: String, password: String): Boolean {
        // For MVP, use simple password validation
        // In production, this should use proper password hashing
//        TODO: replace with Firebase auth
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
