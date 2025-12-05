package com.domicoder.miunieventos.data.repository

import com.domicoder.miunieventos.data.model.Event
import com.domicoder.miunieventos.data.remote.EventRemoteDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class EventRepository @Inject constructor(
    private val remoteDataSource: EventRemoteDataSource
) {
    open fun getAllEvents(): Flow<List<Event>> = remoteDataSource.getAllEvents()
    
    open fun getUpcomingEvents(): Flow<List<Event>> = remoteDataSource.getUpcomingEvents()
    
    open fun getEventsByCategory(category: String): Flow<List<Event>> = 
        remoteDataSource.getEventsByCategory(category)
    
    open fun getEventsByDepartment(department: String): Flow<List<Event>> = 
        remoteDataSource.getEventsByDepartment(department)
    
    open fun getEventsByOrganizer(organizerId: String): Flow<List<Event>> = 
        remoteDataSource.getEventsByOrganizer(organizerId)
    
    open suspend fun getEventById(id: String): Event? = remoteDataSource.getEventById(id)
    
    open suspend fun insertEvent(event: Event): Result<String> = remoteDataSource.insertEvent(event)
    
    open suspend fun insertEvents(events: List<Event>): Result<Unit> = remoteDataSource.insertEvents(events)
    
    open suspend fun updateEvent(event: Event): Result<Unit> = remoteDataSource.updateEvent(event)
    
    open suspend fun deleteEvent(event: Event): Result<Unit> = remoteDataSource.deleteEvent(event)
    
    open suspend fun deleteEventById(id: String): Result<Unit> = remoteDataSource.deleteEventById(id)
}
