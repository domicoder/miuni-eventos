package com.domicoder.miunieventos.data.remote

import android.util.Log
import com.domicoder.miunieventos.data.model.User as DataUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRemoteDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    
    companion object {
        private const val TAG = "AuthRemoteDataSource"
    }
    
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
        Log.d(TAG, "signInWithCredential called")
        Log.d(TAG, "Thread: ${Thread.currentThread().name}")
        Log.d(TAG, "FirebaseAuth instance: ${firebaseAuth.javaClass.name}")
        Log.d(TAG, "Current user before sign in: ${firebaseAuth.currentUser?.uid}")
        Log.d(TAG, "Credential provider: ${credential.provider}")
        
        return withContext(Dispatchers.IO) {
            Log.d(TAG, "Switched to IO thread: ${Thread.currentThread().name}")
            
            var task: com.google.android.gms.tasks.Task<com.google.firebase.auth.AuthResult>? = null
            try {
                Log.d(TAG, "About to call firebaseAuth.signInWithCredential")
                Log.d(TAG, "FirebaseAuth app name: ${firebaseAuth.app.name}")
                Log.d(TAG, "FirebaseAuth app options projectId: ${firebaseAuth.app.options.projectId}")
                
                task = try {
                    firebaseAuth.signInWithCredential(credential)
                } catch (e: Exception) {
                    Log.e(TAG, "Exception creating task: ${e.message}", e)
                    throw e
                }
                Log.d(TAG, "Task created successfully, isComplete: ${task.isComplete}")
                
                if (task.isComplete) {
                    Log.d(TAG, "Task already complete, checking result")
                    val result = task.result
                    Log.d(TAG, "Task result obtained, user: ${result?.user?.uid}")
                    result?.user?.let {
                        Log.d(TAG, "User obtained successfully: ${it.email}, uid: ${it.uid}")
                        return@withContext Result.success(it)
                    } ?: run {
                        Log.e(TAG, "User is null in completed task")
                        val exception = task.exception
                        if (exception != null) {
                            Log.e(TAG, "Task exception: ${exception.message}", exception)
                            return@withContext Result.failure(exception)
                        }
                        return@withContext Result.failure(Exception("No se pudo obtener el usuario después del inicio de sesión"))
                    }
                }
                
                Log.d(TAG, "Task not complete, waiting for result with timeout...")
                val result = withTimeout(30000L) {
                    Log.d(TAG, "Inside withTimeout, calling task.await()")
                    val awaitResult = task.await()
                    Log.d(TAG, "task.await() completed")
                    awaitResult
                }
                
                Log.d(TAG, "signInWithCredential completed, user: ${result.user?.uid}")
                result.user?.let {
                    Log.d(TAG, "User obtained successfully: ${it.email}, uid: ${it.uid}")
                    Result.success(it)
                } ?: run {
                    Log.e(TAG, "User is null after signInWithCredential")
                    Result.failure(Exception("No se pudo obtener el usuario después del inicio de sesión"))
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Timeout waiting for Firebase signInWithCredential after 30 seconds", e)
                task?.let {
                    Log.e(TAG, "Task state: isComplete=${it.isComplete}, isSuccessful=${it.isSuccessful}, isCanceled=${it.isCanceled}")
                    if (it.isComplete && it.exception != null) {
                        Log.e(TAG, "Task exception: ${it.exception?.message}", it.exception)
                    }
                }
                Result.failure(Exception("Tiempo de espera agotado. Verifica tu conexión a internet."))
            } catch (e: FirebaseAuthException) {
                Log.e(TAG, "FirebaseAuthException: ${e.message}, errorCode: ${e.errorCode}", e)
                Result.failure(Exception(getAuthErrorMessage(e)))
            } catch (e: Exception) {
                Log.e(TAG, "Exception in signInWithCredential: ${e.message}", e)
                Log.e(TAG, "Exception type: ${e.javaClass.name}", e)
                e.printStackTrace()
                Result.failure(e)
            }
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

