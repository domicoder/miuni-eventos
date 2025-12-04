package com.domicoder.miunieventos.ui.eventdetail

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.domicoder.miunieventos.R
import com.domicoder.miunieventos.data.model.RSVPStatus
import com.domicoder.miunieventos.data.model.Event
import com.domicoder.miunieventos.ui.components.SingleEventMap
import com.domicoder.miunieventos.util.QRCodeGenerator
import java.time.format.DateTimeFormatter
import java.time.ZoneOffset
import android.content.Intent
import android.content.Context
import android.widget.Toast
import android.graphics.Bitmap
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    navController: NavController,
    eventId: String = "",
    isAuthenticated: Boolean = false,
    currentUserId: String = "",
    onLoginRequest: () -> Unit = {},
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val event by viewModel.event.collectAsState()
    val organizer by viewModel.organizer.collectAsState()
    val userRSVP by viewModel.userRSVP.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Attendance data for organizers
    val attendees by viewModel.attendees.collectAsState()
    val attendanceCount by viewModel.attendanceCount.collectAsState()
    
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val context = LocalContext.current
    
    // QR Code Dialog State
    var showQRCodeDialog by remember { mutableStateOf(false) }
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    // Set the current user ID in the ViewModel
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            viewModel.setCurrentUserId(currentUserId)
        }
    }
    
    // Load event when eventId changes
    LaunchedEffect(eventId) {
        if (eventId.isNotEmpty()) {
            viewModel.loadEvent(eventId)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.event_details)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            event?.let { eventData ->
                                shareEvent(eventData, dateFormatter, timeFormatter, context)
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Share, 
                            contentDescription = stringResource(R.string.event_share)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            } else if (event == null) {
                Text(
                    text = error ?: stringResource(R.string.error),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                event?.let { eventData ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Event Image
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(eventData.imageUrl ?: "")
                                .crossfade(true)
                                .build(),
                            contentDescription = eventData.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                        
                        // Event Details
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Title
                            Text(
                                text = eventData.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Date and Time
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Column {
                                    Text(
                                        text = stringResource(R.string.event_date),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Text(
                                        text = "${eventData.startDateTimeLocal.format(dateFormatter)} - ${eventData.startDateTimeLocal.format(timeFormatter)}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Location
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Column {
                                    Text(
                                        text = stringResource(R.string.event_location),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Text(
                                        text = eventData.location,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Map (if event has coordinates)
                            if (eventData.latitude != null && eventData.longitude != null) {
                                Text(
                                    text = "Ubicación en el mapa",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                SingleEventMap(
                                    event = eventData,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            
                            // Organizer
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Column {
                                    Text(
                                        text = stringResource(R.string.event_organizer),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Text(
                                        text = organizer ?: "",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Department
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Column {
                                    Text(
                                        text = stringResource(R.string.event_department),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Text(
                                        text = eventData.department,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Category
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Column {
                                    Text(
                                        text = stringResource(R.string.event_category),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Text(
                                        text = eventData.category,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Description
                            Text(
                                text = stringResource(R.string.event_description),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = eventData.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // RSVP Section
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(R.string.event_rsvp),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                // Show current RSVP status if user is authenticated and has RSVP'd
                                if (isAuthenticated) {
                                    userRSVP?.let { rsvp ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = when (rsvp.status) {
                                                    RSVPStatus.GOING -> Icons.Default.CheckCircle
                                                    RSVPStatus.MAYBE -> Icons.Default.Help
                                                    RSVPStatus.NOT_GOING -> Icons.Default.Cancel
                                                },
                                                contentDescription = null,
                                                tint = when (rsvp.status) {
                                                    RSVPStatus.GOING -> Color(0xFF4CAF50)
                                                    RSVPStatus.MAYBE -> Color(0xFFFF9800)
                                                    RSVPStatus.NOT_GOING -> Color(0xFFF44336)
                                                },
                                                modifier = Modifier.size(20.dp)
                                            )
                                            
                                            Spacer(modifier = Modifier.width(4.dp))
                                            
                                            Text(
                                                text = when (rsvp.status) {
                                                    RSVPStatus.GOING -> "Asistiré"
                                                    RSVPStatus.MAYBE -> "Tal vez"
                                                    RSVPStatus.NOT_GOING -> "No asistiré"
                                                },
                                                style = MaterialTheme.typography.labelMedium,
                                                color = when (rsvp.status) {
                                                    RSVPStatus.GOING -> Color(0xFF4CAF50)
                                                    RSVPStatus.MAYBE -> Color(0xFFFF9800)
                                                    RSVPStatus.NOT_GOING -> Color(0xFFF44336)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (isAuthenticated) {
                                // Show RSVP buttons for authenticated users
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = { viewModel.updateRSVP(RSVPStatus.GOING) },
                                        colors = if (userRSVP?.status == RSVPStatus.GOING)
                                            ButtonDefaults.buttonColors()
                                        else
                                            ButtonDefaults.outlinedButtonColors(),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = stringResource(R.string.event_rsvp_going))
                                    }
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Button(
                                        onClick = { viewModel.updateRSVP(RSVPStatus.MAYBE) },
                                        colors = if (userRSVP?.status == RSVPStatus.MAYBE)
                                            ButtonDefaults.buttonColors()
                                        else
                                            ButtonDefaults.outlinedButtonColors(),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = stringResource(R.string.event_rsvp_maybe))
                                    }
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Button(
                                        onClick = { viewModel.updateRSVP(RSVPStatus.NOT_GOING) },
                                        colors = if (userRSVP?.status == RSVPStatus.NOT_GOING)
                                            ButtonDefaults.buttonColors()
                                        else
                                            ButtonDefaults.outlinedButtonColors(),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = stringResource(R.string.event_rsvp_not_going))
                                    }
                                }
                            } else {
                                // Show login prompt for unauthenticated users
                                Button(
                                    onClick = onLoginRequest,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Text("Inicia sesión para confirmar asistencia")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Show QR Code Button (only for confirmed attendance)
                            if (isAuthenticated && userRSVP?.status == RSVPStatus.GOING) {
                                Button(
                                    onClick = { 
                                        event?.let { eventData ->
                                            showQRCodeDialog(eventData.id, currentUserId) { qrCode ->
                                                qrCodeBitmap = qrCode
                                                showQRCodeDialog = true
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = null
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text("Mostrar Código QR de Asistencia")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Attendance Section for Organizers
                            if (isAuthenticated && eventData.organizerId == currentUserId) {
                                // Organizer view - show attendance list
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            
                                            Spacer(modifier = Modifier.width(8.dp))
                                            
                                            Text(
                                                text = "Asistencia del Evento",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            
                                            Spacer(modifier = Modifier.weight(1f))
                                            
                                            Text(
                                                text = "$attendanceCount estudiantes",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        // Check-in QR Code Button for Organizers
                                        Button(
                                            onClick = { 
                                                event?.let { eventData ->
                                                    val checkInQR = QRCodeGenerator.generateEventCheckInQRCode(eventData.id)
                                                    if (checkInQR != null) {
                                                        qrCodeBitmap = checkInQR
                                                        showQRCodeDialog = true
                                                    }
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.outlinedButtonColors()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.QrCodeScanner,
                                                contentDescription = null
                                            )
                                            
                                            Spacer(modifier = Modifier.width(8.dp))
                                            
                                            Text("Generar QR de Check-in del Evento")
                                        }
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        if (attendees.isEmpty()) {
                                            Text(
                                                text = "Aún no hay estudiantes registrados en la asistencia",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        } else {
                                            // List of attendees
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                attendees.forEach { attendee ->
                                                    Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = MaterialTheme.colorScheme.surface
                                                        )
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(12.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Person,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                            
                                                            Spacer(modifier = Modifier.width(12.dp))
                                                            
                                                            Column(
                                                                modifier = Modifier.weight(1f)
                                                            ) {
                                                                Text(
                                                                    text = attendee.name,
                                                                    style = MaterialTheme.typography.bodyMedium,
                                                                    fontWeight = FontWeight.Medium
                                                                )
                                                                
                                                                Text(
                                                                    text = attendee.email,
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                                
                                                                Text(
                                                                    text = "Llegó: ${attendee.checkInTime.format(DateTimeFormatter.ofPattern("dd MMM HH:mm"))}",
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    color = MaterialTheme.colorScheme.primary
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Add to Calendar Button
                            if (isAuthenticated) {
                                Button(
                                    onClick = { 
                                        event?.let { eventData ->
                                            addEventToCalendar(eventData, context)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(text = stringResource(R.string.event_add_to_calendar))
                                }
                            } else {
                                Button(
                                    onClick = onLoginRequest,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text("Inicia sesión para añadir al calendario")
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // QR Code Dialog
        if (showQRCodeDialog && qrCodeBitmap != null) {
            AlertDialog(
                onDismissRequest = { 
                    showQRCodeDialog = false
                    qrCodeBitmap = null
                },
                title = {
                    Text(
                        text = if (event?.organizerId == currentUserId) "QR de Check-in del Evento" else "Código QR de Asistencia",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (event?.organizerId == currentUserId) 
                                "Escanea este código QR para registrar la llegada de estudiantes al evento" 
                            else 
                                "Muestra este código QR al organizador del evento para confirmar tu asistencia",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        qrCodeBitmap?.let { bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Código QR de Asistencia",
                                modifier = Modifier
                                    .size(200.dp)
                                    .padding(16.dp)
                            )
                        }
                        
                        Text(
                            text = if (event?.organizerId == currentUserId) 
                                "Evento: ${event?.title ?: ""} - QR de Check-in" 
                            else 
                                "Evento: ${event?.title ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { 
                            showQRCodeDialog = false
                            qrCodeBitmap = null
                        }
                    ) {
                        Text("Cerrar")
                    }
                }
            )
        }
    }
    
}

// Function to add event to calendar
private fun addEventToCalendar(
    event: Event,
    context: Context
) {
    val intent = Intent(Intent.ACTION_INSERT).apply {
        type = "vnd.android.cursor.item/event"
        putExtra("title", event.title)
        putExtra("description", event.description)
        putExtra("eventLocation", event.location)
        
        // Convert LocalDateTime to milliseconds for calendar
        val startTime = event.startDateTimeLocal.toInstant(ZoneOffset.UTC).toEpochMilli()
        val endTime = event.endDateTimeLocal.toInstant(ZoneOffset.UTC).toEpochMilli()
        
        putExtra("beginTime", startTime)
        putExtra("endTime", endTime)
        putExtra("allDay", false)
    }
    
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback: show a message if no calendar app is available
        Toast.makeText(
            context,
            "No se encontró una aplicación de calendario",
            Toast.LENGTH_SHORT
        ).show()
    }
}

// Function to share event details with Firebase Hosting URL
private fun shareEvent(
    event: Event,
    dateFormatter: DateTimeFormatter,
    timeFormatter: DateTimeFormatter,
    context: Context
) {
    // Create Firebase Hosting URL
    val firebaseUrl = "https://miuni-eventos.web.app/event/${event.id}"
    
    // Format the event details for sharing
    val shareText = buildString {
        appendLine("🎉 ${event.title}")
        appendLine()
        appendLine("📅 Fecha: ${event.startDateTimeLocal.format(dateFormatter)}")
        appendLine("⏰ Hora: ${event.startDateTimeLocal.format(timeFormatter)} - ${event.endDateTimeLocal.format(timeFormatter)}")
        appendLine("📍 Ubicación: ${event.location}")
        appendLine("🏷️ Categoría: ${event.category}")
        appendLine("🏢 Departamento: ${event.department}")
        appendLine()
        appendLine("📝 ${event.description}")
        appendLine()
        appendLine("¡No te lo pierdas! 🚀")
        appendLine()
        appendLine("🔗 Ver más detalles: $firebaseUrl")
    }
    
    // Create and launch share intent
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "Evento: ${event.title}")
    }
    
    // Launch the share chooser
    context.startActivity(Intent.createChooser(shareIntent, "Compartir evento"))
}

// Function to show QR code dialog
private fun showQRCodeDialog(eventId: String, userId: String, onShowDialog: (Bitmap) -> Unit) {
    // Generate QR code for attendance
    val qrCode = QRCodeGenerator.generateAttendanceQRCode(eventId, userId)
    if (qrCode != null) {
        onShowDialog(qrCode)
    }
}