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
import com.domicoder.miunieventos.data.repository.AttendanceRepository
import com.domicoder.miunieventos.data.model.RSVPStatus

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val rsvpRepository: RSVPRepository,
    private val eventRepository: EventRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {
    
    private val _scanResult = MutableStateFlow<ScanResult>(ScanResult.Idle)
    val scanResult: StateFlow<ScanResult> = _scanResult
    
    fun processQrCode(qrContent: String) {
        viewModelScope.launch {
            _scanResult.value = ScanResult.Processing
            
            try {
                // Check if it's a check-in QR code (for organizers)
                if (qrContent.startsWith("checkin:")) {
                    processCheckInQRCode(qrContent)
                    return@launch
                }
                
                // Validate QR code format for attendance
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
                
                // Check if RSVP exists and user is going
                val rsvp = rsvpRepository.getRSVPByEventAndUser(eventId, userId)
                if (rsvp == null) {
                    _scanResult.value = ScanResult.Error("No se encontró confirmación de asistencia para este usuario en el evento: ${event.title}")
                    return@launch
                }
                
                // Debug logging
                println("DEBUG: Found RSVP for user $userId in event $eventId with status: ${rsvp.status}")
                
                // Check if user is actually going to the event
                if (rsvp.status != RSVPStatus.GOING) {
                    _scanResult.value = ScanResult.Error("Este usuario no confirmó que asistiría al evento. Estado: ${when(rsvp.status) { RSVPStatus.MAYBE -> "Tal vez" else -> "No asistiré" }}")
                    return@launch
                }
                
                println("DEBUG: User $userId has RSVP status GOING, checking attendance...")
                
                // Check if already checked in (using attendance table, not RSVP)
                val existingAttendance = attendanceRepository.checkIfUserAttended(eventId, userId)
                println("DEBUG: Existing attendance check result: $existingAttendance")
                
                if (existingAttendance) {
                    _scanResult.value = ScanResult.Error("El usuario ya fue registrado en este evento")
                    return@launch
                }
                
                // Debug logging
                println("DEBUG: User $userId not found in attendance for event $eventId")
                println("DEBUG: Proceeding to record attendance...")
                
                // Check if event is currently happening (optional validation)
                val now = LocalDateTime.now()
                if (now.isBefore(event.startDateTimeLocal) || now.isAfter(event.endDateTimeLocal)) {
                    _scanResult.value = ScanResult.Error("El evento no está en curso. Horario: ${event.startDateTimeLocal} - ${event.endDateTimeLocal}")
                    return@launch
                }
                
                // Record attendance for organizers to see
                attendanceRepository.recordAttendance(
                    eventId = eventId,
                    userId = userId,
                    organizerId = event.organizerId
                )
                
                _scanResult.value = ScanResult.Success("Asistencia registrada con éxito")
            } catch (e: Exception) {
                _scanResult.value = ScanResult.Error("Error inesperado: ${e.message ?: "Error desconocido"}")
            }
        }
    }
    
    private suspend fun processCheckInQRCode(qrContent: String) {
        try {
            val eventId = qrContent.substringAfter("checkin:")
            
            if (eventId.isBlank()) {
                _scanResult.value = ScanResult.Error("ID de evento inválido en el QR de check-in")
                return
            }
            
            // Check if event exists
            val event = eventRepository.getEventById(eventId)
            if (event == null) {
                _scanResult.value = ScanResult.Error("Evento no encontrado con ID: $eventId")
                return
            }
            
            // For check-in QR codes, we need to get the user ID from somewhere
            // This could be from user input, or we could generate a different type of QR
            _scanResult.value = ScanResult.Error("QR de check-in detectado. Use el QR de asistencia del estudiante para registrar llegada.")
            
        } catch (e: Exception) {
            _scanResult.value = ScanResult.Error("Error procesando QR de check-in: ${e.message}")
        }
    }
    
    fun resetScanResult() {
        _scanResult.value = ScanResult.Idle
    }
    
    /**
     * Process a test QR code for development purposes
     * This method is now deprecated and should be removed in production
     */
    @Deprecated("Use processQrCode with real QR codes instead")
    fun processTestQrCode() {
        // For development/testing, you can create a test QR code manually
        // or use the scanner to scan a real QR code
        _scanResult.value = ScanResult.Error("Función de prueba deshabilitada. Use códigos QR reales.")
    }
}

sealed class ScanResult {
    object Idle : ScanResult()
    object Processing : ScanResult()
    data class Success(val message: String) : ScanResult()
    data class Error(val message: String) : ScanResult()
} 