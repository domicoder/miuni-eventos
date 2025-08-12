package com.domicoder.miunieventos.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domicoder.miunieventos.data.model.RSVP
import com.domicoder.miunieventos.data.repository.EventRepository
import com.domicoder.miunieventos.data.repository.RSVPRepository
import com.domicoder.miunieventos.util.QRCodeGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val rsvpRepository: RSVPRepository,
    private val eventRepository: EventRepository
) : ViewModel() {
    
    private val _scanResult = MutableStateFlow<ScanResult>(ScanResult.Idle)
    val scanResult: StateFlow<ScanResult> = _scanResult
    
    fun processQrCode(qrContent: String) {
        viewModelScope.launch {
            _scanResult.value = ScanResult.Processing
            
            try {
                // Validate QR code format first
                if (!QRCodeGenerator.isValidQRFormat(qrContent)) {
                    _scanResult.value = ScanResult.Error("Formato de QR inválido. Debe ser: event_id:user_id")
                    return@launch
                }
                
                // Extract event and user IDs using utility functions
                val eventId = QRCodeGenerator.extractEventId(qrContent)
                val userId = QRCodeGenerator.extractUserId(qrContent)
                
                if (eventId == null || userId == null) {
                    _scanResult.value = ScanResult.Error("No se pudieron extraer los IDs del QR")
                    return@launch
                }
                
                // Check if event exists
                val event = eventRepository.getEventById(eventId)
                if (event == null) {
                    _scanResult.value = ScanResult.Error("Evento no encontrado con ID: $eventId")
                    return@launch
                }
                
                // Check if RSVP exists
                val rsvp = rsvpRepository.getRSVPByEventAndUser(eventId, userId)
                if (rsvp == null) {
                    _scanResult.value = ScanResult.Error("No se encontró confirmación de asistencia para este usuario en el evento: ${event.title}")
                    return@launch
                }
                
                // Check if already checked in
                if (rsvp.checkedIn) {
                    _scanResult.value = ScanResult.Error("El usuario ya fue registrado en este evento")
                    return@launch
                }
                
                // Check if event is currently happening (optional validation)
                val now = LocalDateTime.now()
                if (event.startDateTime != null && event.endDateTime != null) {
                    if (now.isBefore(event.startDateTime) || now.isAfter(event.endDateTime)) {
                        _scanResult.value = ScanResult.Error("El evento no está en curso. Horario: ${event.startDateTime} - ${event.endDateTime}")
                        return@launch
                    }
                }
                
                // Update RSVP with check-in info
                val updatedRSVP = rsvp.copy(
                    checkedIn = true,
                    checkedInAt = now
                )
                
                rsvpRepository.updateRSVP(updatedRSVP)
                
                _scanResult.value = ScanResult.Success(updatedRSVP)
            } catch (e: Exception) {
                _scanResult.value = ScanResult.Error("Error inesperado: ${e.message ?: "Error desconocido"}")
            }
        }
    }
    
    fun resetScanResult() {
        _scanResult.value = ScanResult.Idle
    }
    
    /**
     * Process a test QR code for development purposes
     */
    fun processTestQrCode() {
        val testQR = QRCodeGenerator.getRandomTestQRCode()
        processQrCode(testQR)
    }
}

sealed class ScanResult {
    object Idle : ScanResult()
    object Processing : ScanResult()
    data class Success(val rsvp: RSVP) : ScanResult()
    data class Error(val message: String) : ScanResult()
} 