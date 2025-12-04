package com.domicoder.miunieventos.util

import android.content.Intent
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object DeepLinkManager {
    private const val TAG = "DeepLinkManager"
    
    // State flow to communicate deep link events
    private val _deepLinkEvent = MutableStateFlow<String?>(null)
    val deepLinkEvent: StateFlow<String?> = _deepLinkEvent.asStateFlow()
    
    // Function to handle deep links from MainActivity
    fun handleDeepLink(intent: Intent?) {
        intent?.let {
            Log.d(TAG, "Handling intent: action=${it.action}, data=${it.data}")
            Log.d(TAG, "Intent data - scheme: ${it.data?.scheme}, host: ${it.data?.host}, path: ${it.data?.path}")
            
            // Handle custom scheme links for testing
            if (it.data?.scheme == "miunieventos" && it.data?.host == "event") {
                val eventId = it.data?.getQueryParameter("id")
                Log.d(TAG, "Query parameter 'id': $eventId")
                if (eventId != null) {
                    Log.d(TAG, "Custom scheme deep link received for event: $eventId")
                    _deepLinkEvent.value = eventId
                } else {
                    Log.w(TAG, "No event ID found in query parameters")
                }
            } else {
                Log.d(TAG, "Intent doesn't match deep link pattern (miunieventos://event)")
            }
        } ?: Log.d(TAG, "Intent is null")
    }
    
    // Function to clear the deep link event after handling
    fun clearDeepLinkEvent() {
        _deepLinkEvent.value = null
    }
    
    // Function to manually trigger a deep link (for testing)
    fun triggerDeepLink(eventId: String) {
        Log.d(TAG, "Manually triggering deep link for event: $eventId")
        _deepLinkEvent.value = eventId
    }
}
