package com.domicoder.miunieventos.ui.createevent

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domicoder.miunieventos.data.model.Category
import com.domicoder.miunieventos.data.model.Department
import com.domicoder.miunieventos.data.model.Event
import com.domicoder.miunieventos.data.model.EventStatus
import com.domicoder.miunieventos.data.remote.ImageStorageDataSource
import com.domicoder.miunieventos.data.repository.ConfigRepository
import com.domicoder.miunieventos.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val imageStorageDataSource: ImageStorageDataSource,
    private val configRepository: ConfigRepository
) : ViewModel() {
    
    val categories: StateFlow<List<Category>> = configRepository.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val departments: StateFlow<List<Department>> = configRepository.getDepartments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _createSuccess = MutableStateFlow(false)
    val createSuccess: StateFlow<Boolean> = _createSuccess.asStateFlow()
    
    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()
    
    fun setSelectedImage(uri: Uri?) {
        _selectedImageUri.value = uri
    }
    
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
        status: EventStatus = EventStatus.DRAFT
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val eventId = UUID.randomUUID().toString()
                var imageUrl: String? = null
                
                _selectedImageUri.value?.let { uri ->
                    _isUploadingImage.value = true
                    val uploadResult = imageStorageDataSource.uploadEventImage(uri, eventId)
                    _isUploadingImage.value = false
                    
                    if (uploadResult.isSuccess) {
                        imageUrl = uploadResult.getOrNull()
                    } else {
                        _error.value = "Error al subir la imagen"
                        _isLoading.value = false
                        return@launch
                    }
                }
                
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
                    status = status,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                
                val result = eventRepository.insertEvent(event)
                
                if (result.isSuccess) {
                    _createSuccess.value = true
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Error al crear evento"
                }
            } catch (e: Exception) {
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
        _selectedImageUri.value = null
    }
}
