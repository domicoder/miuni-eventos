package com.domicoder.miunieventos.ui.eventdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domicoder.miunieventos.data.model.Event
import com.domicoder.miunieventos.data.model.RSVP
import com.domicoder.miunieventos.data.model.RSVPStatus
import com.domicoder.miunieventos.data.repository.EventRepository
import com.domicoder.miunieventos.data.repository.RSVPRepository
import com.domicoder.miunieventos.data.repository.UserRepository
import com.domicoder.miunieventos.util.RSVPStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val rsvpRepository: RSVPRepository,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val eventId: String = savedStateHandle["eventId"] ?: ""
    
    // This would come from a UserRepository or AuthRepository in a real app
    private val _currentUserId = MutableStateFlow("") // Will be set by the screen
    val currentUserId: StateFlow<String> = _currentUserId
    
    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event
    
    private val _userRSVP = MutableStateFlow<RSVP?>(null)
    val userRSVP: StateFlow<RSVP?> = _userRSVP
    
    private val _organizer = MutableStateFlow<String?>(null)
    val organizer: StateFlow<String?> = _organizer
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    init {
        loadEvent()
    }
    
    fun loadEvent(eventId: String = this.eventId) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val event = eventRepository.getEventById(eventId)
                _event.value = event
                
                if (event != null) {
                    // Load organizer name
                    val organizer = userRepository.getUserById(event.organizerId)
                    _organizer.value = organizer?.name
                    
                    // Load user's RSVP if exists
                    val rsvp = rsvpRepository.getRSVPByEventAndUser(eventId, _currentUserId.value)
                    _userRSVP.value = rsvp
                }
                
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateRSVP(status: RSVPStatus) {
        viewModelScope.launch {
            try {
                val currentRSVP = _userRSVP.value
                val newRSVP = if (currentRSVP != null) {
                    currentRSVP.copy(status = status)
                } else {
                    RSVP(
                        eventId = eventId,
                        userId = _currentUserId.value,
                        status = status
                    )
                }
                
                rsvpRepository.upsertRSVP(newRSVP)
                _userRSVP.value = newRSVP
                
                // Update the shared RSVP state manager
                RSVPStateManager.updateRSVPStatus(_currentUserId.value, eventId, status)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun setCurrentUserId(userId: String) {
        _currentUserId.value = userId
        // Reload event data with the new user ID to get their RSVP status
        if (eventId.isNotEmpty()) {
            loadEvent()
        }
    }
    
    fun deleteRSVP() {
        viewModelScope.launch {
            try {
                _userRSVP.value?.let { rsvp ->
                    rsvpRepository.deleteRSVP(rsvp)
                    _userRSVP.value = null
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
} 