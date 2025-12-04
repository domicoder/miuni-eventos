package com.domicoder.miunieventos.ui.eventedit

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
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
import javax.inject.Inject

@HiltViewModel
open class EditEventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val imageStorageDataSource: ImageStorageDataSource,
    private val configRepository: ConfigRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: String = savedStateHandle["eventId"] ?: ""

    val categories: StateFlow<List<Category>> = configRepository.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val departments: StateFlow<List<Department>> = configRepository.getDepartments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _removeExistingImage = MutableStateFlow(false)
    val removeExistingImage: StateFlow<Boolean> = _removeExistingImage.asStateFlow()

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

    fun setSelectedImage(uri: Uri?) {
        _selectedImageUri.value = uri
        if (uri != null) {
            _removeExistingImage.value = false
        }
    }

    fun removeImage() {
        _selectedImageUri.value = null
        _removeExistingImage.value = true
    }

    fun updateEvent(
        title: String,
        description: String,
        location: String,
        latitude: Double?,
        longitude: Double?,
        category: String,
        department: String,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime,
        status: EventStatus
    ) {
        if (eventId.isEmpty()) return

        viewModelScope.launch {
            _isUpdating.value = true
            _error.value = null

            try {
                val currentEvent = _event.value
                if (currentEvent != null) {
                    var imageUrl = currentEvent.imageUrl

                    if (_removeExistingImage.value) {
                        currentEvent.imageUrl?.let { url ->
                            imageStorageDataSource.deleteEventImage(url)
                        }
                        imageUrl = null
                    }

                    _selectedImageUri.value?.let { uri ->
                        _isUploadingImage.value = true
                        val uploadResult = imageStorageDataSource.uploadEventImage(uri, eventId)
                        _isUploadingImage.value = false

                        if (uploadResult.isSuccess) {
                            currentEvent.imageUrl?.let { oldUrl ->
                                imageStorageDataSource.deleteEventImage(oldUrl)
                            }
                            imageUrl = uploadResult.getOrNull()
                        } else {
                            _error.value = "Error al subir la imagen"
                            _isUpdating.value = false
                            return@launch
                        }
                    }

                    val updatedEvent = currentEvent.copyWithDateTime(
                        title = title,
                        description = description,
                        imageUrl = imageUrl,
                        location = location,
                        latitude = latitude,
                        longitude = longitude,
                        category = category,
                        department = department,
                        startDateTime = startDateTime,
                        endDateTime = endDateTime,
                        status = status,
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

    fun resetDeleteSuccess() {
        _deleteSuccess.value = false
    }

    /**
     * Delete the current event.
     * Only the organizer who owns the event can delete it.
     * 
     * @param currentUserId The ID of the current user attempting to delete
     */
    fun deleteEvent(currentUserId: String) {
        if (eventId.isEmpty()) return

        viewModelScope.launch {
            _isDeleting.value = true
            _error.value = null

            try {
                val currentEvent = _event.value
                if (currentEvent == null) {
                    _error.value = "No se pudo cargar el evento para eliminar"
                    _isDeleting.value = false
                    return@launch
                }

                // Verify ownership
                if (currentEvent.organizerId != currentUserId) {
                    _error.value = "Solo el organizador del evento puede eliminarlo"
                    _isDeleting.value = false
                    return@launch
                }

                // Delete the event image if it exists
                currentEvent.imageUrl?.let { imageUrl ->
                    imageStorageDataSource.deleteEventImage(imageUrl)
                }

                // Delete the event from Firestore
                val result = eventRepository.deleteEventById(eventId)
                if (result.isSuccess) {
                    _deleteSuccess.value = true
                } else {
                    _error.value = "Error al eliminar evento: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _error.value = "Error al eliminar evento: ${e.message}"
            } finally {
                _isDeleting.value = false
            }
        }
    }

    /**
     * Check if the given user is the owner of this event
     */
    fun isEventOwner(userId: String): Boolean {
        return _event.value?.organizerId == userId
    }
}
