package com.domicoder.miunieventos.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import java.time.LocalDateTime
import java.time.ZoneId

data class RSVP(
    @DocumentId
    val id: String = "",
    val eventId: String = "",
    val userId: String = "",
    val status: RSVPStatus = RSVPStatus.GOING,
    val checkedIn: Boolean = false,
    @get:PropertyName("checkedInAt")
    @set:PropertyName("checkedInAt")
    var checkedInAtTimestamp: Timestamp? = null,
    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAtTimestamp: Timestamp? = null
) {
    val checkedInAt: LocalDateTime?
        get() = checkedInAtTimestamp?.toDate()?.toInstant()
            ?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
    
    val createdAt: LocalDateTime
        get() = createdAtTimestamp?.toDate()?.toInstant()
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
         * Creates an RSVP with LocalDateTime values (convenience constructor)
         */
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
                checkedInAtTimestamp = checkedInAt?.let { fromLocalDateTime(it) },
                createdAtTimestamp = fromLocalDateTime(createdAt)
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
