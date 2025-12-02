package com.domicoder.miunieventos.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
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
    @get:PropertyName("startDateTime")
    @set:PropertyName("startDateTime")
    var startDateTimeTimestamp: Timestamp? = null,
    @get:PropertyName("endDateTime")
    @set:PropertyName("endDateTime")
    var endDateTimeTimestamp: Timestamp? = null,
    val category: String = "",
    val department: String = "",
    val organizerId: String = "",
    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAtTimestamp: Timestamp? = null,
    @get:PropertyName("updatedAt")
    @set:PropertyName("updatedAt")
    var updatedAtTimestamp: Timestamp? = null
) {
    // Computed properties for LocalDateTime conversion
    val startDateTime: LocalDateTime
        get() = startDateTimeTimestamp?.toDate()?.toInstant()
            ?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
            ?: LocalDateTime.now()
    
    val endDateTime: LocalDateTime
        get() = endDateTimeTimestamp?.toDate()?.toInstant()
            ?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
            ?: LocalDateTime.now()
    
    val createdAt: LocalDateTime
        get() = createdAtTimestamp?.toDate()?.toInstant()
            ?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
            ?: LocalDateTime.now()
    
    val updatedAt: LocalDateTime
        get() = updatedAtTimestamp?.toDate()?.toInstant()
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
         * Creates an Event with LocalDateTime values (convenience constructor)
         */
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
                startDateTimeTimestamp = fromLocalDateTime(startDateTime),
                endDateTimeTimestamp = fromLocalDateTime(endDateTime),
                category = category,
                department = department,
                organizerId = organizerId,
                createdAtTimestamp = fromLocalDateTime(createdAt),
                updatedAtTimestamp = fromLocalDateTime(updatedAt)
            )
        }
    }
    
    /**
     * Creates a copy with updated LocalDateTime values
     */
    fun copyWithDateTime(
        id: String = this.id,
        title: String = this.title,
        description: String = this.description,
        imageUrl: String? = this.imageUrl,
        location: String = this.location,
        locationId: String? = this.locationId,
        latitude: Double? = this.latitude,
        longitude: Double? = this.longitude,
        startDateTime: LocalDateTime = this.startDateTime,
        endDateTime: LocalDateTime = this.endDateTime,
        category: String = this.category,
        department: String = this.department,
        organizerId: String = this.organizerId,
        createdAt: LocalDateTime = this.createdAt,
        updatedAt: LocalDateTime = this.updatedAt
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
            startDateTimeTimestamp = fromLocalDateTime(startDateTime),
            endDateTimeTimestamp = fromLocalDateTime(endDateTime),
            category = category,
            department = department,
            organizerId = organizerId,
            createdAtTimestamp = fromLocalDateTime(createdAt),
            updatedAtTimestamp = fromLocalDateTime(updatedAt)
        )
    }
}
