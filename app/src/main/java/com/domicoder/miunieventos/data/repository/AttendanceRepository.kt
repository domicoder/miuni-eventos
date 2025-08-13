package com.domicoder.miunieventos.data.repository

import com.domicoder.miunieventos.data.local.AttendanceDao
import com.domicoder.miunieventos.data.local.AttendeeWithDetails
import com.domicoder.miunieventos.data.model.Attendance
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject

class AttendanceRepository @Inject constructor(
    private val attendanceDao: AttendanceDao
) {
    
    suspend fun recordAttendance(
        eventId: String,
        userId: String,
        organizerId: String,
        notes: String? = null
    ) {
        val attendance = Attendance(
            eventId = eventId,
            userId = userId,
            checkInTime = LocalDateTime.now(),
            organizerId = organizerId,
            notes = notes
        )
        attendanceDao.insertAttendance(attendance)
    }
    
    fun getAttendanceByEvent(eventId: String): Flow<List<Attendance>> {
        return attendanceDao.getAttendanceByEvent(eventId)
    }
    
    fun getAttendeesWithDetails(eventId: String): Flow<List<AttendeeWithDetails>> {
        return attendanceDao.getAttendeesWithDetails(eventId)
    }
    
    suspend fun getAttendanceCountByEvent(eventId: String): Int {
        return attendanceDao.getAttendanceCountByEvent(eventId)
    }
    
    suspend fun checkIfUserAttended(eventId: String, userId: String): Boolean {
        return attendanceDao.getAttendanceByEventAndUser(eventId, userId) != null
    }
}
