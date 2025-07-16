package com.domicoder.miunieventos.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "rsvps",
    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["eventId"]),
        Index(value = ["userId"]),
        Index(value = ["eventId", "userId"], unique = true)
    ]
)
data class RSVP(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventId: String,
    val userId: String,
    val status: RSVPStatus,
    val checkedIn: Boolean = false,
    val checkedInAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class RSVPStatus {
    GOING,
    MAYBE,
    NOT_GOING
} 