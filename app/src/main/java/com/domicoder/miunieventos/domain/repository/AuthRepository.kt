package com.domicoder.miunieventos.domain.repository

import com.domicoder.miunieventos.domain.model.User
import com.google.firebase.auth.AuthCredential

interface AuthRepository {
    
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    
    suspend fun signUpWithEmail(email: String, password: String, name: String, department: String): Result<User>
    
    suspend fun signInWithGoogle(credential: AuthCredential): Result<User>
    
    suspend fun signInWithMicrosoft(credential: AuthCredential): Result<User>
    
    suspend fun getCurrentUser(): User?
    
    suspend fun signOut(): Result<Unit>
    
    suspend fun isUserAuthenticated(): Boolean
}

