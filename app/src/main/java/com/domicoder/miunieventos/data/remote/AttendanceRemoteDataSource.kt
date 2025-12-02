package com.domicoder.miunieventos.data.remote

import android.util.Log
import com.domicoder.miunieventos.data.model.Attendance
import com.domicoder.miunieventos.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data class to hold attendee information with attendance details
 */
data class AttendeeWithDetails(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val department: String? = null,
    val isOrganizer: Boolean = false,
    val checkInTime: LocalDateTime = LocalDateTime.now(),
    val notes: String? = null
)

@Singleton
class AttendanceRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "AttendanceRemoteDS"
        private const val COLLECTION_ATTENDANCE = "attendance"
        private const val COLLECTION_USERS = "users"
    }
    
    private val attendanceCollection = firestore.collection(COLLECTION_ATTENDANCE)
    private val usersCollection = firestore.collection(COLLECTION_USERS)
    
    /**
     * Get attendance records by event ID
     */
    fun getAttendanceByEvent(eventId: String): Flow<List<Attendance>> = callbackFlow {
        val listener = attendanceCollection
            .whereEqualTo("eventId", eventId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting attendance by event", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val attendanceList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Attendance::class.java)
                } ?: emptyList()
                
                trySend(attendanceList)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get attendance records by event ID, ordered by check-in time (descending)
     */
    fun getAttendanceByEventOrdered(eventId: String): Flow<List<Attendance>> = callbackFlow {
        val listener = attendanceCollection
            .whereEqualTo("eventId", eventId)
            .orderBy("checkInTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting ordered attendance by event", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val attendanceList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Attendance::class.java)
                } ?: emptyList()
                
                trySend(attendanceList)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get attendees with their user details
     */
    fun getAttendeesWithDetails(eventId: String): Flow<List<AttendeeWithDetails>> = callbackFlow {
        val listener = attendanceCollection
            .whereEqualTo("eventId", eventId)
            .orderBy("checkInTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting attendees with details", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val attendanceList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Attendance::class.java)
                } ?: emptyList()
                
                // Fetch user details for each attendance
                val attendeesWithDetails = mutableListOf<AttendeeWithDetails>()
                
                attendanceList.forEach { attendance ->
                    try {
                        // We need to use a different approach for real-time user data
                        // For now, we'll create the AttendeeWithDetails with attendance info only
                        attendeesWithDetails.add(
                            AttendeeWithDetails(
                                id = attendance.userId,
                                checkInTime = attendance.checkInTime,
                                notes = attendance.notes
                            )
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error creating attendee details", e)
                    }
                }
                
                trySend(attendeesWithDetails)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get attendance by event and user
     */
    suspend fun getAttendanceByEventAndUser(eventId: String, userId: String): Attendance? {
        return try {
            val attendanceId = Attendance.generateId(eventId, userId)
            val document = attendanceCollection.document(attendanceId).get().await()
            document.toObject(Attendance::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting attendance by event and user", e)
            null
        }
    }
    
    /**
     * Get attendance count by event
     */
    suspend fun getAttendanceCountByEvent(eventId: String): Int {
        return try {
            val snapshot = attendanceCollection
                .whereEqualTo("eventId", eventId)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            Log.e(TAG, "Error counting attendance", e)
            0
        }
    }
    
    /**
     * Check if user has already attended an event
     */
    suspend fun checkIfUserAttended(eventId: String, userId: String): Boolean {
        return getAttendanceByEventAndUser(eventId, userId) != null
    }
    
    /**
     * Insert a new attendance record
     */
    suspend fun insertAttendance(attendance: Attendance): Result<String> {
        return try {
            val attendanceId = if (attendance.id.isNotEmpty()) {
                attendance.id
            } else {
                Attendance.generateId(attendance.eventId, attendance.userId)
            }
            val attendanceWithId = attendance.copy(id = attendanceId)
            attendanceCollection.document(attendanceId).set(attendanceWithId).await()
            Log.d(TAG, "Attendance inserted successfully: $attendanceId")
            Result.success(attendanceId)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting attendance", e)
            Result.failure(e)
        }
    }
    
    /**
     * Record attendance (convenience method)
     */
    suspend fun recordAttendance(
        eventId: String,
        userId: String,
        organizerId: String,
        notes: String? = null
    ): Result<String> {
        val attendance = Attendance.create(
            eventId = eventId,
            userId = userId,
            checkInTime = LocalDateTime.now(),
            organizerId = organizerId,
            notes = notes
        )
        return insertAttendance(attendance)
    }
    
    /**
     * Clear all attendance records (for development/testing)
     */
    suspend fun clearAllAttendance(): Result<Unit> {
        return try {
            val snapshot = attendanceCollection.get().await()
            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Log.d(TAG, "All attendance records cleared")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing attendance", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get full attendees with user details (fetches user data from users collection)
     */
    suspend fun getFullAttendeesWithDetails(eventId: String): List<AttendeeWithDetails> {
        return try {
            val attendanceSnapshot = attendanceCollection
                .whereEqualTo("eventId", eventId)
                .orderBy("checkInTime", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val attendanceList = attendanceSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Attendance::class.java)
            }
            
            attendanceList.mapNotNull { attendance ->
                try {
                    val userDoc = usersCollection.document(attendance.userId).get().await()
                    val user = userDoc.toObject(User::class.java)
                    
                    user?.let {
                        AttendeeWithDetails(
                            id = it.id,
                            name = it.name,
                            email = it.email,
                            photoUrl = it.photoUrl,
                            department = it.department,
                            isOrganizer = it.isOrganizer,
                            checkInTime = attendance.checkInTime,
                            notes = attendance.notes
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching user for attendance", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting full attendees with details", e)
            emptyList()
        }
    }
}

