package com.domicoder.miunieventos.data.remote

import android.util.Log
import com.domicoder.miunieventos.data.model.RSVP
import com.domicoder.miunieventos.data.model.RSVPStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RSVPRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "RSVPRemoteDataSource"
        private const val COLLECTION_RSVPS = "rsvps"
    }
    
    private val rsvpsCollection = firestore.collection(COLLECTION_RSVPS)
    
    /**
     * Get all RSVPs
     */
    fun getAllRSVPs(): Flow<List<RSVP>> = callbackFlow {
        val listener = rsvpsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting RSVPs", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val rsvps = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(RSVP::class.java)
                } ?: emptyList()
                
                trySend(rsvps)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get RSVPs by event ID
     */
    fun getRSVPsByEventId(eventId: String): Flow<List<RSVP>> = callbackFlow {
        val listener = rsvpsCollection
            .whereEqualTo("eventId", eventId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting RSVPs by event", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val rsvps = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(RSVP::class.java)
                } ?: emptyList()
                
                trySend(rsvps)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get RSVPs by user ID
     */
    fun getRSVPsByUserId(userId: String): Flow<List<RSVP>> = callbackFlow {
        val listener = rsvpsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting RSVPs by user", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val rsvps = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(RSVP::class.java)
                } ?: emptyList()
                
                trySend(rsvps)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get RSVPs by user ID and status
     */
    fun getRSVPsByUserAndStatus(userId: String, status: RSVPStatus): Flow<List<RSVP>> = callbackFlow {
        val listener = rsvpsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", status.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting RSVPs by user and status", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val rsvps = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(RSVP::class.java)
                } ?: emptyList()
                
                trySend(rsvps)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get a single RSVP by ID
     */
    suspend fun getRSVPById(id: String): RSVP? {
        return try {
            val document = rsvpsCollection.document(id).get().await()
            document.toObject(RSVP::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting RSVP by ID: $id", e)
            null
        }
    }
    
    /**
     * Get RSVP by event and user
     */
    suspend fun getRSVPByEventAndUser(eventId: String, userId: String): RSVP? {
        return try {
            val rsvpId = RSVP.generateId(eventId, userId)
            val document = rsvpsCollection.document(rsvpId).get().await()
            document.toObject(RSVP::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting RSVP by event and user", e)
            null
        }
    }
    
    /**
     * Count RSVPs by event and status
     */
    suspend fun countRSVPsByEventAndStatus(eventId: String, status: RSVPStatus): Int {
        return try {
            val snapshot = rsvpsCollection
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("status", status.name)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            Log.e(TAG, "Error counting RSVPs", e)
            0
        }
    }
    
    /**
     * Insert a new RSVP
     */
    suspend fun insertRSVP(rsvp: RSVP): Result<String> {
        return try {
            val rsvpId = if (rsvp.id.isNotEmpty()) rsvp.id else RSVP.generateId(rsvp.eventId, rsvp.userId)
            val rsvpWithId = rsvp.copy(id = rsvpId)
            rsvpsCollection.document(rsvpId).set(rsvpWithId).await()
            Log.d(TAG, "RSVP inserted successfully: $rsvpId")
            Result.success(rsvpId)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting RSVP", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update an existing RSVP
     */
    suspend fun updateRSVP(rsvp: RSVP): Result<Unit> {
        return try {
            val rsvpId = if (rsvp.id.isNotEmpty()) rsvp.id else RSVP.generateId(rsvp.eventId, rsvp.userId)
            rsvpsCollection.document(rsvpId).set(rsvp.copy(id = rsvpId)).await()
            Log.d(TAG, "RSVP updated successfully: $rsvpId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating RSVP", e)
            Result.failure(e)
        }
    }
    
    /**
     * Upsert RSVP (insert or update)
     */
    suspend fun upsertRSVP(rsvp: RSVP): Result<Unit> {
        return try {
            val rsvpId = RSVP.generateId(rsvp.eventId, rsvp.userId)
            val rsvpWithId = rsvp.copy(id = rsvpId)
            rsvpsCollection.document(rsvpId).set(rsvpWithId).await()
            Log.d(TAG, "RSVP upserted successfully: $rsvpId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error upserting RSVP", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete an RSVP
     */
    suspend fun deleteRSVP(rsvp: RSVP): Result<Unit> {
        return try {
            val rsvpId = if (rsvp.id.isNotEmpty()) rsvp.id else RSVP.generateId(rsvp.eventId, rsvp.userId)
            rsvpsCollection.document(rsvpId).delete().await()
            Log.d(TAG, "RSVP deleted successfully: $rsvpId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting RSVP", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete RSVP by event and user
     */
    suspend fun deleteRSVPByEventAndUser(eventId: String, userId: String): Result<Unit> {
        return try {
            val rsvpId = RSVP.generateId(eventId, userId)
            rsvpsCollection.document(rsvpId).delete().await()
            Log.d(TAG, "RSVP deleted successfully: $rsvpId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting RSVP by event and user", e)
            Result.failure(e)
        }
    }
}

