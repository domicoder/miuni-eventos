package com.domicoder.miunieventos.data.repository

import android.util.Log
import com.domicoder.miunieventos.data.model.Category
import com.domicoder.miunieventos.data.model.Department
import com.domicoder.miunieventos.data.model.Event
import com.domicoder.miunieventos.data.model.RSVP
import com.domicoder.miunieventos.data.model.RSVPStatus
import com.domicoder.miunieventos.data.model.User
import com.domicoder.miunieventos.data.remote.AttendanceRemoteDataSource
import com.domicoder.miunieventos.data.remote.ConfigRemoteDataSource
import com.domicoder.miunieventos.data.remote.EventRemoteDataSource
import com.domicoder.miunieventos.data.remote.RSVPRemoteDataSource
import com.domicoder.miunieventos.data.remote.UserRemoteDataSource
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreInitializer @Inject constructor(
    private val eventRemoteDataSource: EventRemoteDataSource,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val rsvpRemoteDataSource: RSVPRemoteDataSource,
    private val attendanceRemoteDataSource: AttendanceRemoteDataSource,
    private val configRemoteDataSource: ConfigRemoteDataSource
) {
    companion object {
        private const val TAG = "FirestoreInitializer"
    }
    
    suspend fun initializeDatabase() {
        try {
            initializeConfig()
            
            if (isDatabaseEmpty()) {
                Log.d(TAG, "Database is empty, inserting initial data...")
                insertInitialData()
            } else {
                Log.d(TAG, "Database already has data, skipping initialization")
            }
            
            clearAttendanceData()
        } catch (e: Exception) {
            Log.e(TAG, "Error during database initialization", e)
        }
    }
    
    private suspend fun initializeConfig() {
        val categories = listOf(
            Category(name = "Académico", order = 1),
            Category(name = "Cultural", order = 2),
            Category(name = "Deportivo", order = 3),
            Category(name = "Conferencia", order = 4),
            Category(name = "Social", order = 5),
            Category(name = "Taller", order = 6),
            Category(name = "Charla", order = 7),
            Category(name = "Networking", order = 8),
            Category(name = "Otro", order = 99)
        )
        
        val departments = listOf(
            Department(name = "Ingeniería Software", order = 1),
            Department(name = "Tecnología", order = 2),
            Department(name = "Educación", order = 3),
            Department(name = "Medicina", order = 4),
            Department(name = "Artes", order = 5),
            Department(name = "Deportes", order = 6),
            Department(name = "Asociación Estudiantil", order = 7),
            Department(name = "Administración", order = 8),
            Department(name = "Derecho", order = 9),
            Department(name = "Comunicación", order = 10),
            Department(name = "Negocios", order = 11),
            Department(name = "Otro", order = 99)
        )
        
        configRemoteDataSource.initializeCategories(categories)
        configRemoteDataSource.initializeDepartments(departments)
        Log.d(TAG, "Config initialization completed")
    }
    
    private suspend fun isDatabaseEmpty(): Boolean {
        return try {
            val users = userRemoteDataSource.getAllUsers().first()
            users.isEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if database is empty", e)
            true
        }
    }
    
    private suspend fun clearAttendanceData() {
        try {
            attendanceRemoteDataSource.clearAllAttendance()
            Log.d(TAG, "Attendance data cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing attendance data: ${e.message}")
        }
    }
    
    private suspend fun insertInitialData() {
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
        
        val usersResult = userRemoteDataSource.insertUsers(users)
        if (usersResult.isSuccess) {
            Log.d(TAG, "Initial users inserted successfully")
        } else {
            Log.e(TAG, "Failed to insert initial users: ${usersResult.exceptionOrNull()?.message}")
        }
        
        val now = LocalDateTime.now()
        val events = listOf(
            Event.create(
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
                department = "Tecnología",
                organizerId = "user1",
                createdAt = now,
                updatedAt = now
            ),
            Event.create(
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
            Event.create(
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
            Event.create(
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
            Event.create(
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
        
        val eventsResult = eventRemoteDataSource.insertEvents(events)
        if (eventsResult.isSuccess) {
            Log.d(TAG, "Initial events inserted successfully")
        } else {
            Log.e(TAG, "Failed to insert initial events: ${eventsResult.exceptionOrNull()?.message}")
        }
        
        val rsvps = listOf(
            RSVP.create(eventId = "event1", userId = "user3", status = RSVPStatus.GOING),
            RSVP.create(eventId = "event2", userId = "user3", status = RSVPStatus.MAYBE),
            RSVP.create(eventId = "event3", userId = "user1", status = RSVPStatus.GOING)
        )
        
        rsvps.forEach { rsvp ->
            rsvpRemoteDataSource.insertRSVP(rsvp)
        }
        
        Log.d(TAG, "Initial data insertion completed")
    }
}
