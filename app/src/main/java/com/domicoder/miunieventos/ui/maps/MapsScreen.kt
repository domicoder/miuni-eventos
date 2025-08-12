package com.domicoder.miunieventos.ui.maps

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.domicoder.miunieventos.data.model.Event
import com.domicoder.miunieventos.ui.components.EventMap
import com.domicoder.miunieventos.ui.navigation.NavRoutes
import com.domicoder.miunieventos.util.PermissionHandler
import com.domicoder.miunieventos.util.rememberLocationPermissionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.CameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsScreen(
    navController: NavController,
    viewModel: MapsViewModel = hiltViewModel()
) {
    val events by viewModel.events.collectAsState()
    val context = LocalContext.current
    val defaultLocation = LatLng(18.45640641117086, -69.9245333245128)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    // Handle location permissions
    val (hasLocationPermission, requestLocationPermission) = rememberLocationPermissionState()
    val currentHasPermission = PermissionHandler.hasLocationPermission(context)
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    var mapProperties by remember {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = currentHasPermission
            )
        )
    }
    
    var mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = currentHasPermission,
                mapToolbarEnabled = true
            )
        )
    }
    
    // Update map properties when permission changes
    if (hasLocationPermission != currentHasPermission) {
        mapProperties = mapProperties.copy(isMyLocationEnabled = hasLocationPermission)
        mapUiSettings = mapUiSettings.copy(myLocationButtonEnabled = hasLocationPermission)
    }
    
    // Ensure we don't enable location features without permission
    val finalMapProperties = if (!currentHasPermission) {
        mapProperties.copy(isMyLocationEnabled = false)
    } else {
        mapProperties
    }
    
    val finalMapUiSettings = if (!currentHasPermission) {
        mapUiSettings.copy(myLocationButtonEnabled = false)
    } else {
        mapUiSettings
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Locations") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (currentHasPermission) {
                        // Center map on user's location
                        // TODO: Implement location centering logic
                    } else {
                        showPermissionDialog = true
                    }
                }
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "My Location")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
                        EventMap(
                events = events,
                onEventClick = { event ->
                    navController.navigate(NavRoutes.EventDetail.createRoute(event.id))
                },
                cameraPositionState = cameraPositionState,
                mapProperties = finalMapProperties,
                mapUiSettings = finalMapUiSettings
            )
        }
    }
    
    // Permission request dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Se requiere permiso de ubicación") },
            text = { 
                Text("Esta aplicación necesita permiso de ubicación para mostrar tu ubicación actual en el mapa y ayudarte a encontrar eventos cercanos.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        requestLocationPermission()
                    }
                ) {
                    Text("Conceder permiso")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showPermissionDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
} 