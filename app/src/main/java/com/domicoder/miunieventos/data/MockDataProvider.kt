package com.domicoder.miunieventos.data

import com.domicoder.miunieventos.data.model.Event
import com.domicoder.miunieventos.data.model.RSVP
import com.domicoder.miunieventos.data.model.RSVPStatus
import com.domicoder.miunieventos.data.model.User
import java.time.LocalDateTime
import java.util.UUID

/**
 * This class provides mock data for testing the app.
 * In a real app, this data would come from a backend API or local database.
 */
object MockDataProvider {
    
    val users = listOf(
        User(
            id = "user1",
            name = "Juan Pérez",
            email = "juan.perez@universidad.edu",
            photoUrl = "https://i.pravatar.cc/300?u=user1",
            department = "Ingeniería Informática",
            isOrganizer = true
        ),
        User(
            id = "user2",
            name = "María González",
            email = "maria.gonzalez@universidad.edu",
            photoUrl = "https://i.pravatar.cc/300?u=user2",
            department = "Ciencias Sociales",
            isOrganizer = true
        ),
        User(
            id = "user3",
            name = "Carlos Rodríguez",
            email = "carlos.rodriguez@universidad.edu",
            photoUrl = "https://i.pravatar.cc/300?u=user3",
            department = "Medicina",
            isOrganizer = false
        )
    )
    
    val events = listOf(
        Event(
            id = "event1",
            title = "Conferencia de Inteligencia Artificial",
            description = "Conferencia sobre los últimos avances en inteligencia artificial y su aplicación en la industria.",
            imageUrl = "https://picsum.photos/seed/ai/800/400",
            location = "Auditorio Principal",
            latitude = 19.432608,
            longitude = -99.133209,
            startDateTime = LocalDateTime.now().plusDays(2),
            endDateTime = LocalDateTime.now().plusDays(2).plusHours(2),
            category = "Académico",
            department = "Ingeniería Informática",
            organizerId = "user1"
        ),
        Event(
            id = "event2",
            title = "Taller de Fotografía",
            description = "Aprende los fundamentos de la fotografía digital y técnicas de composición.",
            imageUrl = "https://picsum.photos/seed/photo/800/400",
            location = "Sala de Artes",
            latitude = 19.432608,
            longitude = -99.133209,
            startDateTime = LocalDateTime.now().plusDays(5),
            endDateTime = LocalDateTime.now().plusDays(5).plusHours(3),
            category = "Cultural",
            department = "Artes",
            organizerId = "user2"
        ),
        Event(
            id = "event3",
            title = "Torneo de Fútbol Interfacultades",
            description = "Participa en el torneo de fútbol entre las diferentes facultades de la universidad.",
            imageUrl = "https://picsum.photos/seed/soccer/800/400",
            location = "Campo Deportivo",
            latitude = 19.432608,
            longitude = -99.133209,
            startDateTime = LocalDateTime.now().plusDays(7),
            endDateTime = LocalDateTime.now().plusDays(7).plusHours(5),
            category = "Deportivo",
            department = "Deportes",
            organizerId = "user2"
        ),
        Event(
            id = "event4",
            title = "Charla: Salud Mental en Estudiantes",
            description = "Charla informativa sobre la importancia de la salud mental en estudiantes universitarios.",
            imageUrl = "https://picsum.photos/seed/health/800/400",
            location = "Aula Magna",
            latitude = 19.432608,
            longitude = -99.133209,
            startDateTime = LocalDateTime.now().plusDays(3),
            endDateTime = LocalDateTime.now().plusDays(3).plusHours(2),
            category = "Conferencia",
            department = "Medicina",
            organizerId = "user1"
        ),
        Event(
            id = "event5",
            title = "Fiesta de Fin de Semestre",
            description = "Celebra el fin del semestre con música, comida y diversión.",
            imageUrl = "https://picsum.photos/seed/party/800/400",
            location = "Plaza Central",
            latitude = 19.432608,
            longitude = -99.133209,
            startDateTime = LocalDateTime.now().plusDays(14),
            endDateTime = LocalDateTime.now().plusDays(14).plusHours(6),
            category = "Social",
            department = "Asociación Estudiantil",
            organizerId = "user2"
        )
    )
    
    val rsvps = listOf(
        RSVP(
            id = 1,
            eventId = "event1",
            userId = "user3",
            status = RSVPStatus.GOING
        ),
        RSVP(
            id = 2,
            eventId = "event2",
            userId = "user3",
            status = RSVPStatus.MAYBE
        ),
        RSVP(
            id = 3,
            eventId = "event3",
            userId = "user1",
            status = RSVPStatus.GOING
        )
    )
    
    fun getEventById(eventId: String): Event? {
        return events.find { it.id == eventId }
    }
    
    fun getUserById(userId: String): User? {
        return users.find { it.id == userId }
    }
    
    fun getRSVPByEventAndUser(eventId: String, userId: String): RSVP? {
        return rsvps.find { it.eventId == eventId && it.userId == userId }
    }
    
    fun generateQRCode(eventId: String, userId: String): String {
        // In a real app, this would generate an actual QR code
        // For this example, we'll just return the data that would be encoded
        return "$eventId:$userId"
    }
} 