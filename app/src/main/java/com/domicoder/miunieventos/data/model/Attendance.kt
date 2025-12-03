package com.domicoder.miunieventos.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.time.LocalDateTime
import java.time.ZoneId

data class Attendance(
    @DocumentId
    val id: String = "",
    val eventId: String = "",
    val userId: String = "",
    var checkInTime: Timestamp? = null,
    val organizerId: String = "",
    val notes: String? = null
) {
    @get:Exclude
    val checkInTimeLocal: LocalDateTime
        get() = checkInTime?.toDate()?.toInstant()
            ?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
            ?: LocalDateTime.now()

    companion object {
        fun fromLocalDateTime(dateTime: LocalDateTime): Timestamp {
            return Timestamp(
                java.util.Date.from(
                    dateTime.atZone(ZoneId.systemDefault()).toInstant()
                )
            )
        }

        fun create(
            id: String = "",
            eventId: String,
            userId: String,
            checkInTime: LocalDateTime = LocalDateTime.now(),
            organizerId: String,
            notes: String? = null
        ): Attendance {
            return Attendance(
                id = id,
                eventId = eventId,
                userId = userId,
                checkInTime = fromLocalDateTime(checkInTime),
                organizerId = organizerId,
                notes = notes
            )
        }

        fun generateId(eventId: String, userId: String): String {
            return "${eventId}_${userId}"
        }
    }
}
