package com.domicoder.miunieventos.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import java.time.LocalDateTime
import java.time.ZoneId

data class Attendance(
    @DocumentId
    val id: String = "",
    val eventId: String = "",
    val userId: String = "",
    @get:PropertyName("checkInTime")
    @set:PropertyName("checkInTime")
    var checkInTimeTimestamp: Timestamp? = null,
    val organizerId: String = "", // Who scanned the QR code
    val notes: String? = null
) {
    val checkInTime: LocalDateTime
        get() = checkInTimeTimestamp?.toDate()?.toInstant()
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
        
        /**
         * Creates an Attendance with LocalDateTime values (convenience constructor)
         */
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
                checkInTimeTimestamp = fromLocalDateTime(checkInTime),
                organizerId = organizerId,
                notes = notes
            )
        }
        
        /**
         * Generates a composite ID for Attendance documents
         */
        fun generateId(eventId: String, userId: String): String {
            return "${eventId}_${userId}"
        }
    }
}
