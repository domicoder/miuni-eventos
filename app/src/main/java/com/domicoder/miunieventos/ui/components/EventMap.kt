package com.domicoder.miunieventos.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.domicoder.miunieventos.data.model.Event
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.CameraPositionState

@Composable
fun EventMap(
    events: List<Event>,
    onEventClick: (Event) -> Unit,
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState = rememberCameraPositionState(),
    mapProperties: MapProperties = MapProperties(),
    mapUiSettings: MapUiSettings = MapUiSettings()
) {
    val eventsWithLocation = remember(events) {
        derivedStateOf {
            events.filter { event ->
                event.latitude != null && event.longitude != null
            }
        }
    }
    
    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings
    ) {
        eventsWithLocation.value.forEach { event ->
            val position = LatLng(event.latitude!!, event.longitude!!)
            
            Marker(
                state = MarkerState(position = position),
                title = event.title,
                snippet = event.location,
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                onClick = {
                    onEventClick(event)
                    true
                }
            )
        }
    }
}

@Composable
fun SingleEventMap(
    event: Event,
    modifier: Modifier = Modifier,
    onMapClick: (() -> Unit)? = null
) {
    if (event.latitude == null || event.longitude == null) {
        return
    }
    
    val eventPosition = LatLng(event.latitude, event.longitude)
    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(eventPosition, 15f)
    }
    
    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = false
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false
        ),
        onMapClick = { onMapClick?.invoke() }
    ) {
        Marker(
            state = MarkerState(position = eventPosition),
            title = event.title,
            snippet = event.location,
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
        )
    }
} 