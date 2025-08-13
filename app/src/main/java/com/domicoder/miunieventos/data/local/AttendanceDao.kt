package com.domicoder.miunieventos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.domicoder.miunieventos.data.model.Attendance
import com.domicoder.miunieventos.data.model.User
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface AttendanceDao {
    
    @Insert
    suspend fun insertAttendance(attendance: Attendance)
    
    @Query("SELECT * FROM attendance WHERE eventId = :eventId")
    fun getAttendanceByEvent(eventId: String): Flow<List<Attendance>>
    
    @Query("SELECT * FROM attendance WHERE eventId = :eventId AND userId = :userId")
    suspend fun getAttendanceByEventAndUser(eventId: String, userId: String): Attendance?
    
    @Query("SELECT COUNT(*) FROM attendance WHERE eventId = :eventId")
    suspend fun getAttendanceCountByEvent(eventId: String): Int
    
    @Query("DELETE FROM attendance")
    suspend fun clearAllAttendance()
    
    @Query("SELECT * FROM attendance WHERE eventId = :eventId ORDER BY checkInTime DESC")
    fun getAttendanceByEventOrdered(eventId: String): Flow<List<Attendance>>
    
    @Transaction
    @Query("""
        SELECT u.id, u.name, u.email, u.photoUrl, u.department, u.isOrganizer, a.checkInTime, a.notes 
        FROM users u 
        INNER JOIN attendance a ON u.id = a.userId 
        WHERE a.eventId = :eventId 
        ORDER BY a.checkInTime DESC
    """)
    fun getAttendeesWithDetails(eventId: String): Flow<List<AttendeeWithDetails>>
}

data class AttendeeWithDetails(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String?,
    val department: String?,
    val isOrganizer: Boolean,
    val checkInTime: LocalDateTime,
    val notes: String?
)
