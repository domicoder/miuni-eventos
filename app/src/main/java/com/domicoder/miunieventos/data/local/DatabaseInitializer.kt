package com.domicoder.miunieventos.data.local

import com.domicoder.miunieventos.data.model.Event
import com.domicoder.miunieventos.data.model.RSVP
import com.domicoder.miunieventos.data.model.RSVPStatus
import com.domicoder.miunieventos.data.model.User
import com.domicoder.miunieventos.data.model.Attendance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseInitializer @Inject constructor(
    private val eventDao: EventDao,
    private val userDao: UserDao,
    private val rsvpDao: RSVPDao,
    private val attendanceDao: AttendanceDao
) {
    
    suspend fun initializeDatabase() {
        if (isDatabaseEmpty()) {
            insertInitialData()
        }
        
        // Clear any existing attendance data to fix QR scanning issues
        clearAttendanceData()
    }
    
    private suspend fun isDatabaseEmpty(): Boolean {
        val users = userDao.getAllUsers().first()
        return users.isEmpty()
    }
    
    private suspend fun clearAttendanceData() {
        try {
            // Clear all existing attendance records to ensure clean state
            attendanceDao.clearAllAttendance()
            println("DEBUG: Attendance data cleared successfully")
        } catch (e: Exception) {
            println("DEBUG: Error clearing attendance data: ${e.message}")
        }
    }
    
    private suspend fun insertInitialData() {
        // Insert initial users
        val users = listOf(
            User(
                id = "user1",
                name = "Juanito Alimaña",
                email = "juanito.alimana@unicda.edu.do",
                photoUrl = "https://i.pravatar.cc/300?u=user1",
                department = "Ingeniería Software",
                isOrganizer = true
            ),
            User(
                id = "user2",
                name = "María González",
                email = "maria.gonzalez@unicda.edu.do",
                photoUrl = "https://i.pravatar.cc/300?u=user2",
                department = "Ciencias Sociales",
                isOrganizer = true
            ),
            User(
                id = "user3",
                name = "Carlos Rodríguez",
                email = "carlos.rodriguez@unicda.edu.do",
                photoUrl = "https://i.pravatar.cc/300?u=user3",
                department = "Medicina",
                isOrganizer = false
            )
        )
        userDao.insertUsers(users)
        
        // Insert initial events
        val now = LocalDateTime.now()
        val events = listOf(
            Event(
                id = "event1",
                title = "Conferencia de Inteligencia Artificial",
                description = "Conferencia sobre los últimos avances en inteligencia artificial y su aplicación en la industria.",
                imageUrl = "https://picsum.photos/seed/ai/800/400",
                location = "Auditorio Principal",
                latitude = 18.456498004110962,
                longitude = -69.92454405334836,
                startDateTime = now.plusDays(2),
                endDateTime = now.plusDays(2).plusHours(2),
                category = "Académico",
                department = "Ingeniería Informática",
                organizerId = "user1",
                createdAt = now,
                updatedAt = now
            ),
            Event(
                id = "event2",
                title = "Taller de Fotografía",
                description = "Aprende los fundamentos de la fotografía digital y técnicas de composición.",
                imageUrl = "https://picsum.photos/seed/photo/800/400",
                location = "Sala de Artes",
                latitude = 18.456498004110962,
                longitude = -69.92454405334836,
                startDateTime = now.plusDays(5),
                endDateTime = now.plusDays(5).plusHours(3),
                category = "Cultural",
                department = "Artes",
                organizerId = "user2",
                createdAt = now,
                updatedAt = now
            ),
            Event(
                id = "event3",
                title = "Torneo de Fútbol Interfacultades",
                description = "Participa en el torneo de fútbol entre las diferentes facultades de la universidad.",
                imageUrl = "https://picsum.photos/seed/soccer/800/400",
                location = "Campo Deportivo",
                latitude = 18.456498004110962,
                longitude = -69.92454405334836,
                startDateTime = now.plusDays(7),
                endDateTime = now.plusDays(7).plusHours(5),
                category = "Deportivo",
                department = "Deportes",
                organizerId = "user2",
                createdAt = now,
                updatedAt = now
            ),
            Event(
                id = "event4",
                title = "Charla: Salud Mental en Estudiantes",
                description = "Charla informativa sobre la importancia de la salud mental en estudiantes universitarios.",
                imageUrl = "https://picsum.photos/seed/health/800/400",
                location = "Aula Magna",
                latitude = 18.456498004110962,
                longitude = -69.92454405334836,
                startDateTime = now.plusDays(3),
                endDateTime = now.plusDays(3).plusHours(2),
                category = "Conferencia",
                department = "Medicina",
                organizerId = "user1",
                createdAt = now,
                updatedAt = now
            ),
            Event(
                id = "event5",
                title = "Fiesta de Fin de Semestre",
                description = "Celebra el fin del semestre con música, comida y diversión.",
                imageUrl = "https://picsum.photos/seed/party/800/400",
                location = "Plaza Central",
                latitude = 18.456498004110962,
                longitude = -69.92454405334836,
                startDateTime = now.plusDays(14),
                endDateTime = now.plusDays(14).plusHours(6),
                category = "Social",
                department = "Asociación Estudiantil",
                organizerId = "user2",
                createdAt = now,
                updatedAt = now
            )
        )
        eventDao.insertEvents(events)
        
        // Insert initial RSVPs
        val rsvps = listOf(
            RSVP(
                eventId = "event1",
                userId = "user3",
                status = RSVPStatus.GOING
            ),
            RSVP(
                eventId = "event2",
                userId = "user3",
                status = RSVPStatus.MAYBE
            ),
            RSVP(
                eventId = "event3",
                userId = "user1",
                status = RSVPStatus.GOING
            )
        )
        rsvps.forEach { rsvp ->
            rsvpDao.insertRSVP(rsvp)
        }
        
        // Note: Sample attendance data removed to allow proper testing of QR scanning
        // Users should be able to scan QR codes and register attendance during actual events
    }
}
