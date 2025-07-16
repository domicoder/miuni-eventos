package com.domicoder.miunieventos.ui.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domicoder.miunieventos.data.model.Event
import com.domicoder.miunieventos.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MapsViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {
    
    val events: StateFlow<List<Event>> = eventRepository.getUpcomingEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun getEventsWithLocation(): StateFlow<List<Event>> {
        return events
    }
} 