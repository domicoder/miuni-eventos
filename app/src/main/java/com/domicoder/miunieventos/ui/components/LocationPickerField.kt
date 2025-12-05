package com.domicoder.miunieventos.ui.components

import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.domicoder.miunieventos.util.PermissionHandler
import com.domicoder.miunieventos.util.rememberLocationPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Data class representing a selected location
 */
data class SelectedLocation(
    val address: String,
    val latitude: Double,
    val longitude: Double
)

/**
 * A reusable location picker field component that allows selecting a location on a map.
 * 
 * @param locationText The current location text/address
 * @param latitude The current latitude (null if not set)
 * @param longitude The current longitude (null if not set)
 * @param onLocationSelected Callback when a location is selected
 * @param modifier Modifier for the component
 * @param label Label text for the field
 * @param placeholder Placeholder text when no location is selected
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerField(
    locationText: String,
    latitude: Double?,
    longitude: Double?,
    onLocationSelected: (SelectedLocation) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Ubicación *",
    placeholder: String = "Selecciona la ubicación del evento"
) {
    var showMapPicker by remember { mutableStateOf(false) }
    val hasLocation = latitude != null && longitude != null
    
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showMapPicker = true },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (hasLocation) MaterialTheme.colorScheme.primary 
                else MaterialTheme.colorScheme.outline
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (hasLocation) MaterialTheme.colorScheme.primary 
                              else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        if (locationText.isNotBlank()) {
                            Text(
                                text = locationText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (hasLocation) {
                                Text(
                                    text = "📍 ${String.format(Locale.US, "%.6f", latitude)}, ${String.format(Locale.US, "%.6f", longitude)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Icon(
                        imageVector = Icons.Default.EditLocationAlt,
                        contentDescription = "Seleccionar ubicación",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Show mini map preview if location is set
                if (hasLocation) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        val position = LatLng(latitude!!, longitude!!)
                        val cameraPositionState = rememberCameraPositionState {
                            this.position = CameraPosition.fromLatLngZoom(position, 15f)
                        }
                        
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = MapProperties(isMyLocationEnabled = false),
                            uiSettings = MapUiSettings(
                                zoomControlsEnabled = false,
                                myLocationButtonEnabled = false,
                                mapToolbarEnabled = false,
                                scrollGesturesEnabled = false,
                                zoomGesturesEnabled = false,
                                tiltGesturesEnabled = false,
                                rotationGesturesEnabled = false
                            )
                        ) {
                            Marker(
                                state = MarkerState(position = position),
                                title = locationText
                            )
                        }
                        
                        // Overlay to capture clicks
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { showMapPicker = true }
                        )
                    }
                }
            }
        }
    }
    
    // Full screen map picker dialog
    if (showMapPicker) {
        LocationPickerDialog(
            initialLatitude = latitude,
            initialLongitude = longitude,
            initialAddress = locationText,
            onDismiss = { showMapPicker = false },
            onLocationConfirmed = { selectedLocation ->
                onLocationSelected(selectedLocation)
                showMapPicker = false
            }
        )
    }
}

/**
 * Full screen dialog for picking a location on the map
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationPickerDialog(
    initialLatitude: Double?,
    initialLongitude: Double?,
    initialAddress: String,
    onDismiss: () -> Unit,
    onLocationConfirmed: (SelectedLocation) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Default location: Santo Domingo, Dominican Republic (Universidad Domínico Americano area)
    val defaultLocation = LatLng(18.45640641117086, -69.9245333245128)
    
    val initialPosition = if (initialLatitude != null && initialLongitude != null) {
        LatLng(initialLatitude, initialLongitude)
    } else {
        defaultLocation
    }
    
    var selectedPosition by remember { mutableStateOf<LatLng?>(
        if (initialLatitude != null && initialLongitude != null) initialPosition else null
    ) }
    var addressText by remember { mutableStateOf(initialAddress) }
    var isLoadingAddress by remember { mutableStateOf(false) }
    var manualAddressInput by remember { mutableStateOf(initialAddress) }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 15f)
    }
    
    // Permission handling
    val (hasLocationPermission, requestLocationPermission) = rememberLocationPermissionState()
    val currentHasPermission = PermissionHandler.hasLocationPermission(context)
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    // Reverse geocode function
    fun reverseGeocode(latLng: LatLng) {
        scope.launch {
            isLoadingAddress = true
            try {
                val address = withContext(Dispatchers.IO) {
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                        addresses?.firstOrNull()?.let { addr ->
                            buildString {
                                addr.thoroughfare?.let { append(it) }
                                addr.subThoroughfare?.let { 
                                    if (isNotEmpty()) append(" ")
                                    append(it) 
                                }
                                addr.locality?.let { 
                                    if (isNotEmpty()) append(", ")
                                    append(it) 
                                }
                                addr.adminArea?.let { 
                                    if (isNotEmpty()) append(", ")
                                    append(it) 
                                }
                            }.ifEmpty { 
                                "${String.format(Locale.US, "%.6f", latLng.latitude)}, ${String.format(Locale.US, "%.6f", latLng.longitude)}"
                            }
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
                
                addressText = address ?: "${String.format(Locale.US, "%.6f", latLng.latitude)}, ${String.format(Locale.US, "%.6f", latLng.longitude)}"
                manualAddressInput = addressText
            } finally {
                isLoadingAddress = false
            }
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Seleccionar Ubicación", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    },
                    actions = {
                        if (selectedPosition != null) {
                            IconButton(
                                onClick = {
                                    selectedPosition?.let { pos ->
                                        onLocationConfirmed(
                                            SelectedLocation(
                                                address = manualAddressInput.ifBlank { addressText },
                                                latitude = pos.latitude,
                                                longitude = pos.longitude
                                            )
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Check, 
                                    contentDescription = "Confirmar",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Map
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(
                            isMyLocationEnabled = currentHasPermission
                        ),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = true,
                            myLocationButtonEnabled = false,
                            mapToolbarEnabled = false
                        ),
                        onMapClick = { latLng ->
                            selectedPosition = latLng
                            reverseGeocode(latLng)
                        }
                    ) {
                        selectedPosition?.let { position ->
                            Marker(
                                state = MarkerState(position = position),
                                title = "Ubicación seleccionada"
                            )
                        }
                    }
                    
                    // My location button
                    FloatingActionButton(
                        onClick = {
                            if (currentHasPermission) {
                                // TODO: Get current location and move camera
                            } else {
                                showPermissionDialog = true
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = "Mi ubicación",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Instructions overlay
                    if (selectedPosition == null) {
                        Card(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Place,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Toca en el mapa para seleccionar la ubicación",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
                
                // Location info panel
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        if (selectedPosition != null) {
                            // Editable address field
                            OutlinedTextField(
                                value = manualAddressInput,
                                onValueChange = { manualAddressInput = it },
                                label = { Text("Nombre/Dirección del lugar") },
                                placeholder = { Text("Ej: Auditorio Principal, Edificio A") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = false,
                                maxLines = 2,
                                leadingIcon = {
                                    if (isLoadingAddress) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "📍 ${String.format(Locale.US, "%.6f", selectedPosition!!.latitude)}, ${String.format(Locale.US, "%.6f", selectedPosition!!.longitude)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = {
                                    selectedPosition?.let { pos ->
                                        onLocationConfirmed(
                                            SelectedLocation(
                                                address = manualAddressInput.ifBlank { addressText },
                                                latitude = pos.latitude,
                                                longitude = pos.longitude
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = manualAddressInput.isNotBlank()
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Confirmar Ubicación", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Place,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ninguna ubicación seleccionada",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Permission dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permiso de ubicación") },
            text = { 
                Text("Para usar tu ubicación actual, necesitamos permiso de acceso a la ubicación.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        requestLocationPermission()
                    }
                ) {
                    Text("Conceder")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

