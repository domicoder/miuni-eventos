package com.domicoder.miunieventos.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.domicoder.miunieventos.data.model.Event
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY startDateTime ASC")
    fun getAllEvents(): Flow<List<Event>>
    
    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventById(id: String): Event?
    
    @Query("SELECT * FROM events WHERE startDateTime >= :now ORDER BY startDateTime ASC")
    fun getUpcomingEvents(now: LocalDateTime = LocalDateTime.now()): Flow<List<Event>>
    
    @Query("SELECT * FROM events WHERE category = :category AND startDateTime >= :now ORDER BY startDateTime ASC")
    fun getEventsByCategory(category: String, now: LocalDateTime = LocalDateTime.now()): Flow<List<Event>>
    
    @Query("SELECT * FROM events WHERE department = :department AND startDateTime >= :now ORDER BY startDateTime ASC")
    fun getEventsByDepartment(department: String, now: LocalDateTime = LocalDateTime.now()): Flow<List<Event>>
    
    @Query("SELECT * FROM events WHERE organizerId = :organizerId ORDER BY startDateTime ASC")
    fun getEventsByOrganizer(organizerId: String): Flow<List<Event>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<Event>)
    
    @Update
    suspend fun updateEvent(event: Event)
    
    @Delete
    suspend fun deleteEvent(event: Event)
    
    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: String)
} 