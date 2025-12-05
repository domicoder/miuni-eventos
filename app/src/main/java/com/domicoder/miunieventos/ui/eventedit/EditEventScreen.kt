package com.domicoder.miunieventos.ui.eventedit

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.domicoder.miunieventos.data.model.EventStatus
import com.domicoder.miunieventos.ui.components.ImagePickerField
import com.domicoder.miunieventos.ui.components.LocationPickerField
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    navController: NavController,
    currentUserId: String = "",
    viewModel: EditEventViewModel = hiltViewModel()
) {
    val event by viewModel.event.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val isUploadingImage by viewModel.isUploadingImage.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()
    val deleteSuccess by viewModel.deleteSuccess.collectAsState()
    val isDeleting by viewModel.isDeleting.collectAsState()
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()
    val removeExistingImage by viewModel.removeExistingImage.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val departments by viewModel.departments.collectAsState()
    val attendanceCount by viewModel.attendanceCount.collectAsState()
    val rsvpGoingCount by viewModel.rsvpGoingCount.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    val isEventOwner = event?.organizerId == currentUserId

    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var category by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var startTime by remember { mutableStateOf<LocalTime?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var endTime by remember { mutableStateOf<LocalTime?>(null) }
    var eventStatus by remember { mutableStateOf(EventStatus.DRAFT) }
    var existingImageUrl by remember { mutableStateOf<String?>(null) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var departmentExpanded by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    LaunchedEffect(event) {
        event?.let { eventData ->
            title = eventData.title
            description = eventData.description
            location = eventData.location
            latitude = eventData.latitude
            longitude = eventData.longitude
            category = eventData.category
            department = eventData.department
            eventStatus = eventData.eventStatus
            existingImageUrl = eventData.imageUrl

            startDate = eventData.startDateTimeLocal.toLocalDate()
            startTime = eventData.startDateTimeLocal.toLocalTime()
            endDate = eventData.endDateTimeLocal.toLocalDate()
            endTime = eventData.endDateTimeLocal.toLocalTime()
        }
    }

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            viewModel.resetUpdateSuccess()
            navController.popBackStack()
        }
    }

    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            viewModel.resetDeleteSuccess()
            // Pop back to the organized events list
            navController.popBackStack()
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "Eliminar Evento",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas eliminar este evento? Esta acción no se puede deshacer y se eliminarán también todas las confirmaciones de asistencia asociadas."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteEvent(currentUserId)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onError,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    enabled = !isDeleting
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    fun showStartDatePicker() {
        val calendar = Calendar.getInstance()
        startDate?.let {
            calendar.set(it.year, it.monthValue - 1, it.dayOfMonth)
        }

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                startDate = LocalDate.of(year, month + 1, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun showStartTimePicker() {
        val calendar = Calendar.getInstance()
        startTime?.let {
            calendar.set(Calendar.HOUR_OF_DAY, it.hour)
            calendar.set(Calendar.MINUTE, it.minute)
        }

        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                startTime = LocalTime.of(hourOfDay, minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    fun showEndDatePicker() {
        val calendar = Calendar.getInstance()
        endDate?.let {
            calendar.set(it.year, it.monthValue - 1, it.dayOfMonth)
        }

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                endDate = LocalDate.of(year, month + 1, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun showEndTimePicker() {
        val calendar = Calendar.getInstance()
        endTime?.let {
            calendar.set(Calendar.HOUR_OF_DAY, it.hour)
            calendar.set(Calendar.MINUTE, it.minute)
        }

        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                endTime = LocalTime.of(hourOfDay, minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    val isFormValid = title.isNotBlank() &&
            description.isNotBlank() &&
            location.isNotBlank() &&
            category.isNotBlank() &&
            department.isNotBlank() &&
            startDate != null &&
            startTime != null &&
            endDate != null &&
            endTime != null

    fun handleSave() {
        if (isFormValid && startDate != null && startTime != null && endDate != null && endTime != null) {
            val startDateTime = LocalDateTime.of(startDate, startTime)
            val endDateTime = LocalDateTime.of(endDate, endTime)

            viewModel.updateEvent(
                title = title,
                description = description,
                location = location,
                latitude = latitude,
                longitude = longitude,
                category = category,
                department = department,
                startDateTime = startDateTime,
                endDateTime = endDateTime,
                status = eventStatus
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Editar Evento",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    // Only show delete button if user is the event owner
                    if (isEventOwner && !isLoading) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            enabled = !isUpdating && !isDeleting
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar evento",
                                tint = MaterialTheme.colorScheme.error
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
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                ImagePickerField(
                    selectedImageUri = selectedImageUri,
                    existingImageUrl = if (removeExistingImage) null else existingImageUrl,
                    onImageSelected = { viewModel.setSelectedImage(it) },
                    onRemoveImage = { viewModel.removeImage() },
                    isUploading = isUploadingImage
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Attendance Stats Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // RSVP Going Count
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$rsvpGoingCount",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Confirmados",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Attendance Count
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.HowToReg,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$attendanceCount",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "Asistieron",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Estado del Evento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = eventStatus == EventStatus.DRAFT,
                        onClick = { eventStatus = EventStatus.DRAFT },
                        label = { Text("Borrador") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )

                    FilterChip(
                        selected = eventStatus == EventStatus.PUBLISHED,
                        onClick = { eventStatus = EventStatus.PUBLISHED },
                        label = { Text("Publicado") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                Text(
                    text = if (eventStatus == EventStatus.DRAFT)
                        "Los borradores no son visibles para los usuarios"
                    else
                        "El evento será visible para todos los usuarios",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título del Evento *") },
                    placeholder = { Text("Ej: Conferencia de Tecnología") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Title,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción *") },
                    placeholder = { Text("Describe tu evento...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                LocationPickerField(
                    locationText = location,
                    latitude = latitude,
                    longitude = longitude,
                    onLocationSelected = { selectedLocation ->
                        location = selectedLocation.address
                        latitude = selectedLocation.latitude
                        longitude = selectedLocation.longitude
                    },
                    label = "Ubicación *",
                    placeholder = "Selecciona la ubicación del evento"
                )

                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría *") },
                        placeholder = { Text("Selecciona una categoría") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    category = cat.name
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = departmentExpanded,
                    onExpandedChange = { departmentExpanded = !departmentExpanded }
                ) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Departamento *") },
                        placeholder = { Text("Selecciona un departamento") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = departmentExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = departmentExpanded,
                        onDismissRequest = { departmentExpanded = false }
                    ) {
                        departments.forEach { dept ->
                            DropdownMenuItem(
                                text = { Text(dept.name) },
                                onClick = {
                                    department = dept.name
                                    departmentExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Fecha y Hora",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Inicio",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showStartDatePicker() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (startDate != null) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = startDate?.format(dateFormatter) ?: "Fecha *",
                            color = if (startDate != null) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = { showStartTimePicker() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (startTime != null) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = startTime?.format(timeFormatter) ?: "Hora *",
                            color = if (startTime != null) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Fin",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showEndDatePicker() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (endDate != null) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = endDate?.format(dateFormatter) ?: "Fecha *",
                            color = if (endDate != null) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = { showEndTimePicker() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (endTime != null) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = endTime?.format(timeFormatter) ?: "Hora *",
                            color = if (endTime != null) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { handleSave() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = isFormValid && !isUpdating,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isUploadingImage) "Subiendo imagen..." else "Guardando...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Guardar Cambios",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = error ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
