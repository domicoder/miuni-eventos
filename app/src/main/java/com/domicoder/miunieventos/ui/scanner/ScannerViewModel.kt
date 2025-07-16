package com.domicoder.miunieventos.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domicoder.miunieventos.data.model.RSVP
import com.domicoder.miunieventos.data.repository.EventRepository
import com.domicoder.miunieventos.data.repository.RSVPRepository
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
                // QR code format: "event_id:user_id"
                val parts = qrContent.split(":")
                if (parts.size != 2) {
                    _scanResult.value = ScanResult.Error("Invalid QR code format")
                    return@launch
                }
                
                val eventId = parts[0]
                val userId = parts[1]
                
                // Check if event exists
                val event = eventRepository.getEventById(eventId)
                if (event == null) {
                    _scanResult.value = ScanResult.Error("Event not found")
                    return@launch
                }
                
                // Check if RSVP exists
                val rsvp = rsvpRepository.getRSVPByEventAndUser(eventId, userId)
                if (rsvp == null) {
                    _scanResult.value = ScanResult.Error("No RSVP found for this user")
                    return@launch
                }
                
                // Check if already checked in
                if (rsvp.checkedIn) {
                    _scanResult.value = ScanResult.Error("User already checked in")
                    return@launch
                }
                
                // Update RSVP with check-in info
                val updatedRSVP = rsvp.copy(
                    checkedIn = true,
                    checkedInAt = LocalDateTime.now()
                )
                
                rsvpRepository.updateRSVP(updatedRSVP)
                
                _scanResult.value = ScanResult.Success(updatedRSVP)
            } catch (e: Exception) {
                _scanResult.value = ScanResult.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun resetScanResult() {
        _scanResult.value = ScanResult.Idle
    }
}

sealed class ScanResult {
    object Idle : ScanResult()
    object Processing : ScanResult()
    data class Success(val rsvp: RSVP) : ScanResult()
    data class Error(val message: String) : ScanResult()
} 