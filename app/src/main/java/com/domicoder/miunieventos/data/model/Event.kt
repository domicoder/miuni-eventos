package com.domicoder.miunieventos.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.time.LocalDateTime
import java.time.ZoneId

data class Event(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val location: String = "",
    val locationId: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    var startDateTime: Timestamp? = null,
    var endDateTime: Timestamp? = null,
    val category: String = "",
    val department: String = "",
    val organizerId: String = "",
    var createdAt: Timestamp? = null,
    var updatedAt: Timestamp? = null
) {
    @get:Exclude
    val startDateTimeLocal: LocalDateTime
        get() = startDateTime?.toDate()?.toInstant()
            ?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
            ?: LocalDateTime.now()

    @get:Exclude
    val endDateTimeLocal: LocalDateTime
        get() = endDateTime?.toDate()?.toInstant()
            ?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
            ?: LocalDateTime.now()

    @get:Exclude
    val createdAtLocal: LocalDateTime
        get() = createdAt?.toDate()?.toInstant()
            ?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
            ?: LocalDateTime.now()

    @get:Exclude
    val updatedAtLocal: LocalDateTime
        get() = updatedAt?.toDate()?.toInstant()
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
            title: String,
            description: String,
            imageUrl: String? = null,
            location: String,
            locationId: String? = null,
            latitude: Double? = null,
            longitude: Double? = null,
            startDateTime: LocalDateTime,
            endDateTime: LocalDateTime,
            category: String,
            department: String,
            organizerId: String,
            createdAt: LocalDateTime = LocalDateTime.now(),
            updatedAt: LocalDateTime = LocalDateTime.now()
        ): Event {
            return Event(
                id = id,
                title = title,
                description = description,
                imageUrl = imageUrl,
                location = location,
                locationId = locationId,
                latitude = latitude,
                longitude = longitude,
                startDateTime = fromLocalDateTime(startDateTime),
                endDateTime = fromLocalDateTime(endDateTime),
                category = category,
                department = department,
                organizerId = organizerId,
                createdAt = fromLocalDateTime(createdAt),
                updatedAt = fromLocalDateTime(updatedAt)
            )
        }
    }

    fun copyWithDateTime(
        id: String = this.id,
        title: String = this.title,
        description: String = this.description,
        imageUrl: String? = this.imageUrl,
        location: String = this.location,
        locationId: String? = this.locationId,
        latitude: Double? = this.latitude,
        longitude: Double? = this.longitude,
        startDateTime: LocalDateTime = this.startDateTimeLocal,
        endDateTime: LocalDateTime = this.endDateTimeLocal,
        category: String = this.category,
        department: String = this.department,
        organizerId: String = this.organizerId,
        createdAt: LocalDateTime = this.createdAtLocal,
        updatedAt: LocalDateTime = this.updatedAtLocal
    ): Event {
        return copy(
            id = id,
            title = title,
            description = description,
            imageUrl = imageUrl,
            location = location,
            locationId = locationId,
            latitude = latitude,
            longitude = longitude,
            startDateTime = fromLocalDateTime(startDateTime),
            endDateTime = fromLocalDateTime(endDateTime),
            category = category,
            department = department,
            organizerId = organizerId,
            createdAt = fromLocalDateTime(createdAt),
            updatedAt = fromLocalDateTime(updatedAt)
        )
    }
}
