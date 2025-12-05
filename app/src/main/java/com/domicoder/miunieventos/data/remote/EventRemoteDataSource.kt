package com.domicoder.miunieventos.data.remote

import android.util.Log
import com.domicoder.miunieventos.data.model.Event
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "EventRemoteDataSource"
        private const val COLLECTION_EVENTS = "events"
    }
    
    private val eventsCollection = firestore.collection(COLLECTION_EVENTS)
    
    /**
     * Get all events ordered by start date
     */
    fun getAllEvents(): Flow<List<Event>> = callbackFlow {
        val listener = eventsCollection
            .orderBy("startDateTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting events", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Event::class.java)
                } ?: emptyList()
                
                trySend(events)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get upcoming events (events that haven't started yet)
     */
    fun getUpcomingEvents(): Flow<List<Event>> = callbackFlow {
        val now = Timestamp.now()
        
        val listener = eventsCollection
            .whereGreaterThanOrEqualTo("startDateTime", now)
            .orderBy("startDateTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting upcoming events", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Event::class.java)
                } ?: emptyList()
                
                trySend(events)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get events by category
     */
    fun getEventsByCategory(category: String): Flow<List<Event>> = callbackFlow {
        val now = Timestamp.now()
        
        val listener = eventsCollection
            .whereEqualTo("category", category)
            .whereGreaterThanOrEqualTo("startDateTime", now)
            .orderBy("startDateTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting events by category", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Event::class.java)
                } ?: emptyList()
                
                trySend(events)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get events by department
     */
    fun getEventsByDepartment(department: String): Flow<List<Event>> = callbackFlow {
        val now = Timestamp.now()
        
        val listener = eventsCollection
            .whereEqualTo("department", department)
            .whereGreaterThanOrEqualTo("startDateTime", now)
            .orderBy("startDateTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting events by department", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Event::class.java)
                } ?: emptyList()
                
                trySend(events)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get events by organizer
     */
    fun getEventsByOrganizer(organizerId: String): Flow<List<Event>> = callbackFlow {
        val listener = eventsCollection
            .whereEqualTo("organizerId", organizerId)
            .orderBy("startDateTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting events by organizer", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Event::class.java)
                } ?: emptyList()
                
                trySend(events)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get a single event by ID
     */
    suspend fun getEventById(id: String): Event? {
        return try {
            val document = eventsCollection.document(id).get().await()
            document.toObject(Event::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting event by ID: $id", e)
            null
        }
    }
    
    /**
     * Insert a new event
     */
    suspend fun insertEvent(event: Event): Result<String> {
        return try {
            val eventId = if (event.id.isNotEmpty()) event.id else eventsCollection.document().id
            val eventWithId = event.copy(id = eventId)
            eventsCollection.document(eventId).set(eventWithId).await()
            Log.d(TAG, "Event inserted successfully: $eventId")
            Result.success(eventId)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting event", e)
            Result.failure(e)
        }
    }
    
    /**
     * Insert multiple events (batch operation)
     */
    suspend fun insertEvents(events: List<Event>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            events.forEach { event ->
                val eventId = if (event.id.isNotEmpty()) event.id else eventsCollection.document().id
                val eventWithId = event.copy(id = eventId)
                batch.set(eventsCollection.document(eventId), eventWithId)
            }
            batch.commit().await()
            Log.d(TAG, "Events batch inserted successfully: ${events.size} events")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting events batch", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update an existing event
     */
    suspend fun updateEvent(event: Event): Result<Unit> {
        return try {
            eventsCollection.document(event.id).set(event).await()
            Log.d(TAG, "Event updated successfully: ${event.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating event: ${event.id}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete an event
     */
    suspend fun deleteEvent(event: Event): Result<Unit> {
        return deleteEventById(event.id)
    }
    
    /**
     * Delete an event by ID
     */
    suspend fun deleteEventById(id: String): Result<Unit> {
        return try {
            eventsCollection.document(id).delete().await()
            Log.d(TAG, "Event deleted successfully: $id")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting event: $id", e)
            Result.failure(e)
        }
    }
}

