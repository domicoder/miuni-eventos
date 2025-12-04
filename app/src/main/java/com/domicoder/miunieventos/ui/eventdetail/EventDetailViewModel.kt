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
import com.domicoder.miunieventos.data.repository.AttendanceRepository
import com.domicoder.miunieventos.data.remote.AttendeeWithDetails
import com.domicoder.miunieventos.util.RSVPStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val rsvpRepository: RSVPRepository,
    private val userRepository: UserRepository,
    private val attendanceRepository: AttendanceRepository,
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
    
    // Attendance tracking for organizers
    private val _attendees = MutableStateFlow<List<AttendeeWithDetails>>(emptyList())
    val attendees: StateFlow<List<AttendeeWithDetails>> = _attendees
    
    private val _attendanceCount = MutableStateFlow(0)
    val attendanceCount: StateFlow<Int> = _attendanceCount
    
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
                    
                    // Load attendance data for organizers (using suspend function for full details)
                    val attendees = attendanceRepository.getFullAttendeesWithDetails(eventId)
                    _attendees.value = attendees
                    
                    val attendanceCount = attendanceRepository.getAttendanceCountByEvent(eventId)
                    _attendanceCount.value = attendanceCount
                    
                    // Initialize RSVPStateManager with current user's RSVPs
                    if (_currentUserId.value.isNotEmpty()) {
                        val userRSVPs = rsvpRepository.getRSVPsByUserId(_currentUserId.value).first()
                        RSVPStateManager.initializeFromDatabase(userRSVPs)
                    }
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
                if (status == RSVPStatus.NOT_GOING) {
                    _userRSVP.value?.let { rsvp ->
                        rsvpRepository.deleteRSVP(rsvp)
                    }
                    _userRSVP.value = null
                    RSVPStateManager.removeRSVPStatus(_currentUserId.value, eventId)
                } else {
                    val currentRSVP = _userRSVP.value
                    val newRSVP = if (currentRSVP != null) {
                        RSVP.create(
                            id = currentRSVP.id,
                            eventId = eventId,
                            userId = _currentUserId.value,
                            status = status
                        )
                    } else {
                        RSVP.create(
                            eventId = eventId,
                            userId = _currentUserId.value,
                            status = status
                        )
                    }

                    rsvpRepository.upsertRSVP(newRSVP)
                    _userRSVP.value = newRSVP
                    RSVPStateManager.updateRSVPStatus(_currentUserId.value, eventId, status)
                }
            } catch (e: Exception) {
                _error.value = "Error al actualizar RSVP: ${e.message}"
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
    
    fun recordAttendance(userId: String, organizerId: String, notes: String? = null) {
        viewModelScope.launch {
            try {
                attendanceRepository.recordAttendance(
                    eventId = eventId,
                    userId = userId,
                    organizerId = organizerId,
                    notes = notes
                )
                
                // Refresh attendance data
                val attendees = attendanceRepository.getFullAttendeesWithDetails(eventId)
                _attendees.value = attendees
                
                val attendanceCount = attendanceRepository.getAttendanceCountByEvent(eventId)
                _attendanceCount.value = attendanceCount
            } catch (e: Exception) {
                _error.value = "Error al registrar asistencia: ${e.message}"
            }
        }
    }
}
