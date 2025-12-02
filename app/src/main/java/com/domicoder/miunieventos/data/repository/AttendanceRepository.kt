package com.domicoder.miunieventos.data.repository

import com.domicoder.miunieventos.data.model.Attendance
import com.domicoder.miunieventos.data.remote.AttendanceRemoteDataSource
import com.domicoder.miunieventos.data.remote.AttendeeWithDetails
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val remoteDataSource: AttendanceRemoteDataSource
) {
    
    suspend fun recordAttendance(
        eventId: String,
        userId: String,
        organizerId: String,
        notes: String? = null
    ): Result<String> {
        return remoteDataSource.recordAttendance(
            eventId = eventId,
            userId = userId,
            organizerId = organizerId,
            notes = notes
        )
    }
    
    fun getAttendanceByEvent(eventId: String): Flow<List<Attendance>> {
        return remoteDataSource.getAttendanceByEvent(eventId)
    }
    
    fun getAttendeesWithDetails(eventId: String): Flow<List<AttendeeWithDetails>> {
        return remoteDataSource.getAttendeesWithDetails(eventId)
    }
    
    suspend fun getFullAttendeesWithDetails(eventId: String): List<AttendeeWithDetails> {
        return remoteDataSource.getFullAttendeesWithDetails(eventId)
    }
    
    suspend fun getAttendanceCountByEvent(eventId: String): Int {
        return remoteDataSource.getAttendanceCountByEvent(eventId)
    }
    
    suspend fun checkIfUserAttended(eventId: String, userId: String): Boolean {
        return remoteDataSource.checkIfUserAttended(eventId, userId)
    }
    
    suspend fun clearAllAttendance(): Result<Unit> {
        return remoteDataSource.clearAllAttendance()
    }
}
