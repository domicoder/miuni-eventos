package com.domicoder.miunieventos.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.time.LocalDateTime
import java.time.ZoneId

data class RSVP(
    @DocumentId
    val id: String = "",
    val eventId: String = "",
    val userId: String = "",
    val status: RSVPStatus = RSVPStatus.GOING,
    val checkedIn: Boolean = false,
    var checkedInAt: Timestamp? = null,
    var createdAt: Timestamp? = null
) {
    @get:Exclude
    val checkedInAtLocal: LocalDateTime?
        get() = checkedInAt?.toDate()?.toInstant()
            ?.atZone(ZoneId.systemDefault())?.toLocalDateTime()

    @get:Exclude
    val createdAtLocal: LocalDateTime
        get() = createdAt?.toDate()?.toInstant()
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
            status: RSVPStatus,
            checkedIn: Boolean = false,
            checkedInAt: LocalDateTime? = null,
            createdAt: LocalDateTime = LocalDateTime.now()
        ): RSVP {
            return RSVP(
                id = id,
                eventId = eventId,
                userId = userId,
                status = status,
                checkedIn = checkedIn,
                checkedInAt = checkedInAt?.let { fromLocalDateTime(it) },
                createdAt = fromLocalDateTime(createdAt)
            )
        }

        fun generateId(eventId: String, userId: String): String {
            return "${eventId}_${userId}"
        }
    }
}

enum class RSVPStatus {
    GOING,
    MAYBE,
    NOT_GOING
}
