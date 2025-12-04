package com.domicoder.miunieventos.ui.eventdetail

import android.util.Log
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

/**
 * Represents the access status for an event
 */
enum class EventAccessStatus {
    LOADING,           // Still checking access
    ALLOWED,           // User can access the event
    DENIED_PAST,       // Event has ended and user didn't attend
    DENIED_IN_PROGRESS // Event is in progress and user didn't RSVP
}

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

    // Access control
    private val _accessStatus = MutableStateFlow(EventAccessStatus.LOADING)
    val accessStatus: StateFlow<EventAccessStatus> = _accessStatus

    private val _userHasAttended = MutableStateFlow(false)
    val userHasAttended: StateFlow<Boolean> = _userHasAttended

    companion object {
        private const val TAG = "EventDetailViewModel"
    }
    
    init {
        loadEvent()
    }
    
    fun loadEvent(eventId: String = this.eventId) {
        viewModelScope.launch {
            _isLoading.value = true
            _accessStatus.value = EventAccessStatus.LOADING
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

                    // Check if user has attended this event
                    val hasAttended = if (_currentUserId.value.isNotEmpty()) {
                        attendanceRepository.checkIfUserAttended(eventId, _currentUserId.value)
                    } else {
                        false
                    }
                    _userHasAttended.value = hasAttended

                    // Check access based on event status
                    val accessStatus = checkEventAccess(
                        event = event,
                        userId = _currentUserId.value,
                        rsvp = rsvp,
                        hasAttended = hasAttended,
                        isOrganizer = event.organizerId == _currentUserId.value
                    )
                    _accessStatus.value = accessStatus
                    Log.d(TAG, "Access status for event $eventId: $accessStatus (userId: ${_currentUserId.value})")
                    
                    // Initialize RSVPStateManager with current user's RSVPs
                    if (_currentUserId.value.isNotEmpty()) {
                        val userRSVPs = rsvpRepository.getRSVPsByUserId(_currentUserId.value).first()
                        RSVPStateManager.initializeFromDatabase(userRSVPs)
                    }
                } else {
                    _accessStatus.value = EventAccessStatus.DENIED_PAST // Event not found
                }
                
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
                _accessStatus.value = EventAccessStatus.ALLOWED // On error, allow access to show error
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Determines if the user can access this event based on:
     * - Event hasn't started yet: Anyone can access
     * - Event is in progress: Only users with RSVP (GOING/MAYBE) or organizer can access
     * - Event has ended: Only users who attended or organizer can access
     */
    private fun checkEventAccess(
        event: Event,
        userId: String,
        rsvp: RSVP?,
        hasAttended: Boolean,
        isOrganizer: Boolean
    ): EventAccessStatus {
        val now = LocalDateTime.now()
        val startTime = event.startDateTimeLocal
        val endTime = event.endDateTimeLocal

        Log.d(TAG, "Checking access - now: $now, start: $startTime, end: $endTime")
        Log.d(TAG, "User info - userId: $userId, hasRSVP: ${rsvp != null}, rsvpStatus: ${rsvp?.status}, hasAttended: $hasAttended, isOrganizer: $isOrganizer")

        // Organizer always has access
        if (isOrganizer) {
            Log.d(TAG, "Access ALLOWED: User is organizer")
            return EventAccessStatus.ALLOWED
        }

        // If user is not authenticated, only allow access to upcoming events
        if (userId.isEmpty()) {
            return if (now.isBefore(startTime)) {
                Log.d(TAG, "Access ALLOWED: Unauthenticated user, event hasn't started")
                EventAccessStatus.ALLOWED
            } else if (now.isBefore(endTime)) {
                Log.d(TAG, "Access DENIED: Unauthenticated user, event in progress")
                EventAccessStatus.DENIED_IN_PROGRESS
            } else {
                Log.d(TAG, "Access DENIED: Unauthenticated user, event has ended")
                EventAccessStatus.DENIED_PAST
            }
        }

        return when {
            // Event hasn't started yet - anyone can access
            now.isBefore(startTime) -> {
                Log.d(TAG, "Access ALLOWED: Event hasn't started yet")
                EventAccessStatus.ALLOWED
            }
            
            // Event is in progress - only users with RSVP can access
            now.isBefore(endTime) -> {
                val hasValidRsvp = rsvp != null && (rsvp.status == RSVPStatus.GOING || rsvp.status == RSVPStatus.MAYBE)
                if (hasValidRsvp || hasAttended) {
                    Log.d(TAG, "Access ALLOWED: Event in progress, user has RSVP or attended")
                    EventAccessStatus.ALLOWED
                } else {
                    Log.d(TAG, "Access DENIED: Event in progress, user has no RSVP")
                    EventAccessStatus.DENIED_IN_PROGRESS
                }
            }
            
            // Event has ended - only users who attended can access
            else -> {
                if (hasAttended) {
                    Log.d(TAG, "Access ALLOWED: Event ended, user attended")
                    EventAccessStatus.ALLOWED
                } else {
                    Log.d(TAG, "Access DENIED: Event ended, user didn't attend")
                    EventAccessStatus.DENIED_PAST
                }
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
