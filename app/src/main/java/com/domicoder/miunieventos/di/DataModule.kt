package com.domicoder.miunieventos.di

import com.domicoder.miunieventos.data.MockDataProvider
import com.domicoder.miunieventos.data.model.Event
import com.domicoder.miunieventos.data.model.RSVP
import com.domicoder.miunieventos.data.model.User
import com.domicoder.miunieventos.data.repository.EventRepository
import com.domicoder.miunieventos.data.repository.RSVPRepository
import com.domicoder.miunieventos.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Singleton

/**
 * This module provides mock implementations of repositories for testing.
 * In a real app, these would be replaced with actual implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    
    @Provides
    @Singleton
    fun provideEventRepository(): EventRepository {
        return object : EventRepository(null) {
            override fun getAllEvents(): Flow<List<Event>> = flowOf(MockDataProvider.events)
            
            override fun getUpcomingEvents(): Flow<List<Event>> = flowOf(MockDataProvider.events)
            
            override fun getEventsByCategory(category: String): Flow<List<Event>> = 
                flowOf(MockDataProvider.events.filter { it.category == category })
            
            override fun getEventsByDepartment(department: String): Flow<List<Event>> = 
                flowOf(MockDataProvider.events.filter { it.department == department })
            
            override fun getEventsByOrganizer(organizerId: String): Flow<List<Event>> = 
                flowOf(MockDataProvider.events.filter { it.organizerId == organizerId })
            
            override suspend fun getEventById(id: String): Event? = MockDataProvider.getEventById(id)
            
            override suspend fun insertEvent(event: Event) { /* No-op in mock */ }
            
            override suspend fun insertEvents(events: List<Event>) { /* No-op in mock */ }
            
            override suspend fun updateEvent(event: Event) { /* No-op in mock */ }
            
            override suspend fun deleteEvent(event: Event) { /* No-op in mock */ }
            
            override suspend fun deleteEventById(id: String) { /* No-op in mock */ }
        }
    }
    
    @Provides
    @Singleton
    fun provideUserRepository(): UserRepository {
        return object : UserRepository(null) {
            override fun getAllUsers(): Flow<List<User>> = flowOf(MockDataProvider.users)
            
            override fun getOrganizers(): Flow<List<User>> = 
                flowOf(MockDataProvider.users.filter { it.isOrganizer })
            
            override suspend fun getUserById(id: String): User? = MockDataProvider.getUserById(id)
            
            override suspend fun getUserByEmail(email: String): User? = 
                MockDataProvider.users.find { it.email == email }
            
            override suspend fun insertUser(user: User) { /* No-op in mock */ }
            
            override suspend fun insertUsers(users: List<User>) { /* No-op in mock */ }
            
            override suspend fun updateUser(user: User) { /* No-op in mock */ }
            
            override suspend fun deleteUser(user: User) { /* No-op in mock */ }
            
            override suspend fun deleteUserById(id: String) { /* No-op in mock */ }
        }
    }
    
    @Provides
    @Singleton
    fun provideRSVPRepository(): RSVPRepository {
        return object : RSVPRepository(null) {
            override fun getAllRSVPs(): Flow<List<RSVP>> = flowOf(MockDataProvider.rsvps)
            
            override fun getRSVPsByEventId(eventId: String): Flow<List<RSVP>> = 
                flowOf(MockDataProvider.rsvps.filter { it.eventId == eventId })
            
            override fun getRSVPsByUserId(userId: String): Flow<List<RSVP>> = 
                flowOf(MockDataProvider.rsvps.filter { it.userId == userId })
            
            override fun getRSVPsByUserAndStatus(userId: String, status: com.domicoder.miunieventos.data.model.RSVPStatus): Flow<List<RSVP>> = 
                flowOf(MockDataProvider.rsvps.filter { it.userId == userId && it.status == status })
            
            override suspend fun getRSVPById(id: Long): RSVP? = 
                MockDataProvider.rsvps.find { it.id == id }
            
            override suspend fun getRSVPByEventAndUser(eventId: String, userId: String): RSVP? = 
                MockDataProvider.getRSVPByEventAndUser(eventId, userId)
            
            override suspend fun countRSVPsByEventAndStatus(eventId: String, status: com.domicoder.miunieventos.data.model.RSVPStatus): Int = 
                MockDataProvider.rsvps.count { it.eventId == eventId && it.status == status }
            
            override suspend fun insertRSVP(rsvp: RSVP): Long = 1L // Mock ID
            
            override suspend fun updateRSVP(rsvp: RSVP) { /* No-op in mock */ }
            
            override suspend fun upsertRSVP(rsvp: RSVP) { /* No-op in mock */ }
            
            override suspend fun deleteRSVP(rsvp: RSVP) { /* No-op in mock */ }
            
            override suspend fun deleteRSVPByEventAndUser(eventId: String, userId: String) { /* No-op in mock */ }
        }
    }
} 