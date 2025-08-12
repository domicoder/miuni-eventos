package com.domicoder.miunieventos.ui.myevents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domicoder.miunieventos.data.model.Event
import com.domicoder.miunieventos.data.model.RSVP
import com.domicoder.miunieventos.data.model.RSVPStatus
import com.domicoder.miunieventos.data.repository.EventRepository
import com.domicoder.miunieventos.data.repository.RSVPRepository
import com.domicoder.miunieventos.util.RSVPStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyEventsViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val rsvpRepository: RSVPRepository
) : ViewModel() {
    
    // This would come from a UserRepository or AuthRepository in a real app
    private val _currentUserId = MutableStateFlow("") // Will be set by the screen
    val currentUserId: StateFlow<String> = _currentUserId
    
    // Get all RSVPs for the current user (any status)
    private val userRSVPs = currentUserId.flatMapLatest { userId ->
        rsvpRepository.getRSVPsByUserId(userId)
    }
    
    // Map RSVPs to events with RSVP status using reactive state
    val myEvents = combine(
        userRSVPs,
        RSVPStateManager.rsvpStates
    ) { rsvps, rsvpStates ->
        val userId = currentUserId.value
        
        if (rsvps.isEmpty()) {
            emptyList<EventWithRSVP>()
        } else {
            // Create events with RSVP status from the reactive state
            rsvps.mapNotNull { rsvp ->
                eventRepository.getEventById(rsvp.eventId)?.let { event ->
                    EventWithRSVP(event, rsvp.status)
                }
            }.sortedBy { it.event.startDateTime }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    fun setCurrentUserId(userId: String) {
        _currentUserId.value = userId
    }
}

// Data class to hold event with RSVP status
data class EventWithRSVP(
    val event: Event,
    val rsvpStatus: RSVPStatus
) 