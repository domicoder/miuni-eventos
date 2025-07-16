package com.domicoder.miunieventos.ui.myevents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domicoder.miunieventos.data.model.Event
import com.domicoder.miunieventos.data.model.RSVP
import com.domicoder.miunieventos.data.model.RSVPStatus
import com.domicoder.miunieventos.data.repository.EventRepository
import com.domicoder.miunieventos.data.repository.RSVPRepository
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
    private val _currentUserId = MutableStateFlow("user_id")
    val currentUserId: StateFlow<String> = _currentUserId
    
    // Get RSVPs for the current user with status GOING
    private val userRSVPs = currentUserId.flatMapLatest { userId ->
        rsvpRepository.getRSVPsByUserAndStatus(userId, RSVPStatus.GOING)
    }
    
    // Map RSVPs to events
    val myEvents = userRSVPs.flatMapLatest { rsvps ->
        if (rsvps.isEmpty()) {
            return@flatMapLatest MutableStateFlow(emptyList<Event>())
        }
        
        // Create a flow for each event
        val eventFlows = rsvps.map { rsvp ->
            eventRepository.getEventById(rsvp.eventId)?.let { event ->
                MutableStateFlow(event)
            } ?: MutableStateFlow(null)
        }
        
        // Combine all event flows
        if (eventFlows.isEmpty()) {
            return@flatMapLatest MutableStateFlow(emptyList<Event>())
        }
        
        combine(eventFlows) { events ->
            events.filterNotNull()
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