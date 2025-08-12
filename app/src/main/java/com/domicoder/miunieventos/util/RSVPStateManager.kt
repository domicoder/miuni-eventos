package com.domicoder.miunieventos.util

import com.domicoder.miunieventos.data.model.RSVP
import com.domicoder.miunieventos.data.model.RSVPStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object RSVPStateManager {
    
    // Store RSVP states for all events by user
    private val _rsvpStates = MutableStateFlow<Map<String, Map<String, RSVPStatus>>>(emptyMap())
    val rsvpStates: StateFlow<Map<String, Map<String, RSVPStatus>>> = _rsvpStates.asStateFlow()
    
    // Get RSVP status for a specific event and user
    fun getRSVPStatus(userId: String, eventId: String): RSVPStatus? {
        return _rsvpStates.value[userId]?.get(eventId)
    }
    
    // Update RSVP status for a specific event and user
    fun updateRSVPStatus(userId: String, eventId: String, status: RSVPStatus) {
        _rsvpStates.update { currentStates ->
            val userStates = currentStates[userId]?.toMutableMap() ?: mutableMapOf()
            userStates[eventId] = status
            currentStates + (userId to userStates)
        }
    }
    
    // Remove RSVP status for a specific event and user
    fun removeRSVPStatus(userId: String, eventId: String) {
        _rsvpStates.update { currentStates ->
            val userStates = currentStates[userId]?.toMutableMap() ?: mutableMapOf()
            userStates.remove(eventId)
            currentStates + (userId to userStates)
        }
    }
    
    // Get all RSVP statuses for a user
    fun getUserRSVPs(userId: String): Map<String, RSVPStatus> {
        return _rsvpStates.value[userId] ?: emptyMap()
    }
    
    // Clear all RSVP states (useful for logout)
    fun clearAllStates() {
        _rsvpStates.value = emptyMap()
    }
    
    // Initialize RSVP states from database (call this when app starts)
    fun initializeFromDatabase(rsvps: List<RSVP>) {
        val newStates = rsvps.groupBy { it.userId }
            .mapValues { (_, userRsvps) ->
                userRsvps.associate { it.eventId to it.status }
            }
        _rsvpStates.value = newStates
    }
}
