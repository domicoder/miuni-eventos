package com.domicoder.miunieventos.ui.eventedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domicoder.miunieventos.data.model.Event
import com.domicoder.miunieventos.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
open class EditEventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val eventId: String = savedStateHandle["eventId"] ?: ""
    
    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()
    
    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()
    
    init {
        loadEvent()
    }
    
    private fun loadEvent() {
        if (eventId.isEmpty()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val eventData = eventRepository.getEventById(eventId)
                _event.value = eventData
                
                if (eventData == null) {
                    _error.value = "Evento no encontrado"
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar evento: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateEvent(
        title: String,
        description: String,
        location: String,
        category: String,
        department: String,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ) {
        if (eventId.isEmpty()) return
        
        viewModelScope.launch {
            _isUpdating.value = true
            _error.value = null
            
            try {
                val currentEvent = _event.value
                if (currentEvent != null) {
                    val updatedEvent = currentEvent.copyWithDateTime(
                        title = title,
                        description = description,
                        location = location,
                        category = category,
                        department = department,
                        startDateTime = startDateTime,
                        endDateTime = endDateTime,
                        updatedAt = LocalDateTime.now()
                    )
                    
                    val result = eventRepository.updateEvent(updatedEvent)
                    if (result.isSuccess) {
                        _event.value = updatedEvent
                        _updateSuccess.value = true
                    } else {
                        _error.value = "Error al actualizar evento: ${result.exceptionOrNull()?.message}"
                    }
                } else {
                    _error.value = "No se pudo cargar el evento para editar"
                }
            } catch (e: Exception) {
                _error.value = "Error al actualizar evento: ${e.message}"
            } finally {
                _isUpdating.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun resetUpdateSuccess() {
        _updateSuccess.value = false
    }
}
