package com.domicoder.miunieventos.data.remote

import com.domicoder.miunieventos.data.model.User as DataUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRemoteDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("No se pudo obtener el usuario después del inicio de sesión"))
        } catch (e: FirebaseAuthException) {
            Result.failure(Exception(getAuthErrorMessage(e)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signUpWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("No se pudo crear el usuario"))
        } catch (e: FirebaseAuthException) {
            Result.failure(Exception(getAuthErrorMessage(e)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signInWithCredential(credential: AuthCredential): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithCredential(credential).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("No se pudo obtener el usuario después del inicio de sesión"))
        } catch (e: FirebaseAuthException) {
            Result.failure(Exception(getAuthErrorMessage(e)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
    
    suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun saveUserToFirestore(user: DataUser): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(user.id)
                .set(user)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserFromFirestore(userId: String): Result<DataUser?> {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            if (document.exists()) {
                val user = document.toObject(DataUser::class.java)
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun firebaseUserToDataUser(firebaseUser: FirebaseUser, name: String? = null, department: String? = null): DataUser {
        val firestoreUser = getUserFromFirestore(firebaseUser.uid).getOrNull()
        
        return firestoreUser ?: DataUser(
            id = firebaseUser.uid,
            name = name ?: firebaseUser.displayName ?: firebaseUser.email?.split("@")?.firstOrNull() ?: "Usuario",
            email = firebaseUser.email ?: "",
            photoUrl = firebaseUser.photoUrl?.toString(),
            department = department,
            isOrganizer = false
        )
    }
    
    private fun getAuthErrorMessage(exception: FirebaseAuthException): String {
        return when (exception.errorCode) {
            "ERROR_INVALID_EMAIL" -> "El formato del correo electrónico no es válido"
            "ERROR_WRONG_PASSWORD" -> "La contraseña es incorrecta"
            "ERROR_USER_NOT_FOUND" -> "No existe una cuenta con este correo electrónico"
            "ERROR_USER_DISABLED" -> "Esta cuenta ha sido deshabilitada"
            "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos fallidos. Intenta más tarde"
            "ERROR_OPERATION_NOT_ALLOWED" -> "Esta operación no está permitida"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Ya existe una cuenta con este correo electrónico"
            "ERROR_WEAK_PASSWORD" -> "La contraseña es demasiado débil"
            "ERROR_NETWORK_REQUEST_FAILED" -> "Error de conexión. Verifica tu internet"
            else -> exception.message ?: "Error de autenticación desconocido"
        }
    }
}

