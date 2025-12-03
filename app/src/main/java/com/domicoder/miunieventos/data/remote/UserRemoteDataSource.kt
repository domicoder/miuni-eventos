package com.domicoder.miunieventos.data.remote

import android.util.Log
import com.domicoder.miunieventos.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "UserRemoteDataSource"
        private const val COLLECTION_USERS = "users"
    }
    
    private val usersCollection = firestore.collection(COLLECTION_USERS)
    
    /**
     * Get all users
     */
    fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val listener = usersCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting users", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val users = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(User::class.java)
                } ?: emptyList()
                
                trySend(users)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get organizers only
     */
    fun getOrganizers(): Flow<List<User>> = callbackFlow {
        val listener = usersCollection
            .whereEqualTo("isOrganizer", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting organizers", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val users = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(User::class.java)
                } ?: emptyList()
                
                trySend(users)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get a single user by ID
     */
    suspend fun getUserById(id: String): User? {
        return try {
            val document = usersCollection.document(id).get().await()
            if (document.exists()) {
                val user = document.toObject(User::class.java)
                Log.d(TAG, "User fetched from Firestore: id=${user?.id}, name=${user?.name}, isOrganizer=${user?.isOrganizer}")
                user
            } else {
                Log.d(TAG, "User document does not exist for ID: $id")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by ID: $id", e)
            null
        }
    }
    
    /**
     * Get a user by email
     */
    suspend fun getUserByEmail(email: String): User? {
        return try {
            val snapshot = usersCollection
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()
            
            snapshot.documents.firstOrNull()?.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by email: $email", e)
            null
        }
    }
    
    /**
     * Insert a new user
     */
    suspend fun insertUser(user: User): Result<String> {
        return try {
            val userId = if (user.id.isNotEmpty()) user.id else usersCollection.document().id
            val userWithId = user.copy(id = userId)
            usersCollection.document(userId).set(userWithId).await()
            Log.d(TAG, "User inserted successfully: $userId")
            Result.success(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting user", e)
            Result.failure(e)
        }
    }
    
    /**
     * Insert multiple users (batch operation)
     */
    suspend fun insertUsers(users: List<User>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            users.forEach { user ->
                val userId = if (user.id.isNotEmpty()) user.id else usersCollection.document().id
                val userWithId = user.copy(id = userId)
                batch.set(usersCollection.document(userId), userWithId)
            }
            batch.commit().await()
            Log.d(TAG, "Users batch inserted successfully: ${users.size} users")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting users batch", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update an existing user
     */
    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Log.d(TAG, "User updated successfully: ${user.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user: ${user.id}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete a user
     */
    suspend fun deleteUser(user: User): Result<Unit> {
        return deleteUserById(user.id)
    }
    
    /**
     * Delete a user by ID
     */
    suspend fun deleteUserById(id: String): Result<Unit> {
        return try {
            usersCollection.document(id).delete().await()
            Log.d(TAG, "User deleted successfully: $id")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user: $id", e)
            Result.failure(e)
        }
    }
}

