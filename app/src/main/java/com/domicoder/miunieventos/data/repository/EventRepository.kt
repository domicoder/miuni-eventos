package com.domicoder.miunieventos.data.repository

import com.domicoder.miunieventos.data.local.EventDao
import com.domicoder.miunieventos.data.model.Event
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class EventRepository @Inject constructor(
    private val eventDao: EventDao?
) {
    open fun getAllEvents(): Flow<List<Event>> = eventDao!!.getAllEvents()
    
    open fun getUpcomingEvents(): Flow<List<Event>> = eventDao!!.getUpcomingEvents()
    
    open fun getEventsByCategory(category: String): Flow<List<Event>> = 
        eventDao!!.getEventsByCategory(category)
    
    open fun getEventsByDepartment(department: String): Flow<List<Event>> = 
        eventDao!!.getEventsByDepartment(department)
    
    open fun getEventsByOrganizer(organizerId: String): Flow<List<Event>> = 
        eventDao!!.getEventsByOrganizer(organizerId)
    
    open suspend fun getEventById(id: String): Event? = eventDao!!.getEventById(id)
    
    open suspend fun insertEvent(event: Event) = eventDao!!.insertEvent(event)
    
    open suspend fun insertEvents(events: List<Event>) = eventDao!!.insertEvents(events)
    
    open suspend fun updateEvent(event: Event) = eventDao!!.updateEvent(event)
    
    open suspend fun deleteEvent(event: Event) = eventDao!!.deleteEvent(event)
    
    open suspend fun deleteEventById(id: String) = eventDao!!.deleteEventById(id)
} 