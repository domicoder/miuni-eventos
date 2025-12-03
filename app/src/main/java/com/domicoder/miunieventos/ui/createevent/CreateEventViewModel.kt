package com.domicoder.miunieventos.ui.createevent

import android.util.Log
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
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "CreateEventViewModel"
    }
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _createSuccess = MutableStateFlow(false)
    val createSuccess: StateFlow<Boolean> = _createSuccess.asStateFlow()
    
    private val _createdEventId = MutableStateFlow<String?>(null)
    val createdEventId: StateFlow<String?> = _createdEventId.asStateFlow()
    
    fun createEvent(
        title: String,
        description: String,
        location: String,
        latitude: Double? = null,
        longitude: Double? = null,
        category: String,
        department: String,
        organizerId: String,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime,
        imageUrl: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Generate a unique event ID
                val eventId = UUID.randomUUID().toString()
                
                val event = Event.create(
                    id = eventId,
                    title = title,
                    description = description,
                    imageUrl = imageUrl,
                    location = location,
                    latitude = latitude,
                    longitude = longitude,
                    startDateTime = startDateTime,
                    endDateTime = endDateTime,
                    category = category,
                    department = department,
                    organizerId = organizerId,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                
                val result = eventRepository.insertEvent(event)
                
                if (result.isSuccess) {
                    Log.d(TAG, "Event created successfully: ${result.getOrNull()}")
                    _createdEventId.value = result.getOrNull()
                    _createSuccess.value = true
                } else {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Error desconocido al crear evento"
                    Log.e(TAG, "Failed to create event: $errorMessage")
                    _error.value = errorMessage
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception creating event", e)
                _error.value = "Error al crear evento: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun resetCreateSuccess() {
        _createSuccess.value = false
        _createdEventId.value = null
    }
}

