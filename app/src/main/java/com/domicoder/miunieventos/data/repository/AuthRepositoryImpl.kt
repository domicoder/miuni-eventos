package com.domicoder.miunieventos.data.repository

import android.util.Log
import com.domicoder.miunieventos.data.mapper.UserMapper
import com.domicoder.miunieventos.data.model.User as DataUser
import com.domicoder.miunieventos.data.remote.AuthRemoteDataSource
import com.domicoder.miunieventos.domain.model.User as DomainUser
import com.domicoder.miunieventos.domain.repository.AuthRepository
import com.google.firebase.auth.AuthCredential
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource
) : AuthRepository {
    
    companion object {
        private const val TAG = "AuthRepositoryImpl"
    }
    
    override suspend fun signInWithEmail(email: String, password: String): Result<DomainUser> {
        val result = remoteDataSource.signInWithEmail(email, password)
        return if (result.isSuccess) {
            val firebaseUser = result.getOrNull()!!
            val dataUser = remoteDataSource.firebaseUserToDataUser(firebaseUser)
            Result.success(UserMapper.dataToDomain(dataUser))
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
        }
    }
    
    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        name: String,
        department: String
    ): Result<DomainUser> {
        val result = remoteDataSource.signUpWithEmail(email, password)
        return if (result.isSuccess) {
            val firebaseUser = result.getOrNull()!!
            val dataUser = remoteDataSource.firebaseUserToDataUser(
                firebaseUser,
                name = name,
                department = department
            )
            
            remoteDataSource.saveUserToFirestore(dataUser)
            Result.success(UserMapper.dataToDomain(dataUser))
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
        }
    }
    
    override suspend fun signInWithGoogle(credential: AuthCredential): Result<DomainUser> {
        Log.d(TAG, "signInWithGoogle called")
        Log.d(TAG, "Calling signInWithCredential")
        val result = remoteDataSource.signInWithCredential(credential)
        Log.d(TAG, "signInWithCredential result received, isSuccess: ${result.isSuccess}")
        return if (result.isSuccess) {
            val firebaseUser = result.getOrNull()!!
            Log.d(TAG, "Firebase user obtained: ${firebaseUser.uid}, email: ${firebaseUser.email}")
            Log.d(TAG, "Converting Firebase user to DataUser")
            val dataUser = remoteDataSource.firebaseUserToDataUser(firebaseUser)
            Log.d(TAG, "DataUser created: ${dataUser.id}, ${dataUser.email}")
            
            Log.d(TAG, "Checking Firestore for existing user")
            val firestoreResult = remoteDataSource.getUserFromFirestore(firebaseUser.uid)
            Log.d(TAG, "Firestore result received, isSuccess: ${firestoreResult.isSuccess}")
            if (firestoreResult.isSuccess) {
                val existingUser = firestoreResult.getOrNull()
                Log.d(TAG, "Existing user from Firestore: ${existingUser != null}")
                if (existingUser == null) {
                    Log.d(TAG, "No existing user found, saving to Firestore")
                    remoteDataSource.saveUserToFirestore(dataUser)
                    Log.d(TAG, "User saved to Firestore")
                }
                val finalUser = existingUser ?: dataUser
                Log.d(TAG, "Mapping to domain user and returning success")
                Result.success(UserMapper.dataToDomain(finalUser))
            } else {
                Log.w(TAG, "Firestore check failed, using dataUser")
                Result.success(UserMapper.dataToDomain(dataUser))
            }
        } else {
            Log.e(TAG, "signInWithCredential failed: ${result.exceptionOrNull()?.message}")
            Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
        }
    }
    
    override suspend fun signInWithMicrosoft(credential: AuthCredential): Result<DomainUser> {
        val result = remoteDataSource.signInWithCredential(credential)
        return if (result.isSuccess) {
            val firebaseUser = result.getOrNull()!!
            val dataUser = remoteDataSource.firebaseUserToDataUser(firebaseUser)
            
            val firestoreResult = remoteDataSource.getUserFromFirestore(firebaseUser.uid)
            if (firestoreResult.isSuccess) {
                val existingUser = firestoreResult.getOrNull()
                if (existingUser == null) {
                    remoteDataSource.saveUserToFirestore(dataUser)
                }
                Result.success(UserMapper.dataToDomain(existingUser ?: dataUser))
            } else {
                Result.success(UserMapper.dataToDomain(dataUser))
            }
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
        }
    }
    
    override suspend fun getCurrentUser(): DomainUser? {
        val firebaseUser = remoteDataSource.getCurrentUser() ?: return null
        val dataUser = remoteDataSource.firebaseUserToDataUser(firebaseUser)
        return UserMapper.dataToDomain(dataUser)
    }
    
    override suspend fun signOut(): Result<Unit> {
        return remoteDataSource.signOut()
    }
    
    override suspend fun isUserAuthenticated(): Boolean {
        return remoteDataSource.getCurrentUser() != null
    }
}

