package com.domicoder.miunieventos.ui.organizedevents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domicoder.miunieventos.data.model.Event
import com.domicoder.miunieventos.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrganizedEventsViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {
    
    private val _organizedEvents = MutableStateFlow<List<Event>>(emptyList())
    val organizedEvents: StateFlow<List<Event>> = _organizedEvents.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private var currentOrganizerId: String = ""
    
    fun setOrganizerId(organizerId: String) {
        if (currentOrganizerId != organizerId) {
            currentOrganizerId = organizerId
            loadOrganizedEvents()
        }
    }
    
    fun loadOrganizedEvents() {
        if (currentOrganizerId.isEmpty()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val events = eventRepository.getEventsByOrganizer(currentOrganizerId).first()
                _organizedEvents.value = events
            } catch (e: Exception) {
                _error.value = "Error al cargar eventos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refreshEvents() {
        loadOrganizedEvents()
    }
    
    fun clearError() {
        _error.value = null
    }
}
