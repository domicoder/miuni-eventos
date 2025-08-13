package com.domicoder.miunieventos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val eventId: String,
    val userId: String,
    val checkInTime: LocalDateTime,
    val organizerId: String, // Who scanned the QR code
    val notes: String? = null
)
