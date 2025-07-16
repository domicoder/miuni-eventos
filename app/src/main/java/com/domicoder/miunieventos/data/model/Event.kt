package com.domicoder.miunieventos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "events")
data class Event(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val location: String,
    val latitude: Double?,
    val longitude: Double?,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val category: String,
    val department: String,
    val organizerId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) 