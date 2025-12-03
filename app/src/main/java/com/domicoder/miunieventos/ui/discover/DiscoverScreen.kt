package com.domicoder.miunieventos.ui.discover

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.domicoder.miunieventos.R
import com.domicoder.miunieventos.ui.components.EventCard
import com.domicoder.miunieventos.ui.navigation.NavRoutes
import com.domicoder.miunieventos.util.RSVPStateManager
import com.domicoder.miunieventos.data.model.RSVPStatus
import com.domicoder.miunieventos.data.model.Event
import com.domicoder.miunieventos.data.model.RSVP
import androidx.compose.foundation.layout.Arrangement
import java.time.LocalDate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    navController: NavController,
    isAuthenticated: Boolean = false,
    currentUserId: String = "",
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    val allEvents by viewModel.events.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedDepartment by viewModel.selectedDepartment.collectAsState()
    val selectedStartDate by viewModel.selectedStartDate.collectAsState()
    val selectedEndDate by viewModel.selectedEndDate.collectAsState()
    val showOnlySelectedEvents by viewModel.showOnlySelectedEvents.collectAsState()
    
    // Reset the "Solo Seleccionados" filter when user is not authenticated
    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated && showOnlySelectedEvents) {
            viewModel.resetSelectedEventsFilter()
        }
    }
    
    // Get RSVP data for the current user (only for authenticated users)
    val userRsvps by viewModel.getUserRSVPs(currentUserId).collectAsState(initial = emptyList<RSVP>())
    
    // Also get RSVP states from RSVPStateManager for reactive updates (like MyEventsViewModel)
    val rsvpStates by RSVPStateManager.rsvpStates.collectAsState()
    val userRsvpStates = if (isAuthenticated && currentUserId.isNotEmpty()) {
        rsvpStates[currentUserId] ?: emptyMap()
    } else {
        emptyMap()
    }

    // Create a map of event ID to RSVP status (only for authenticated users)
    // Prioritize RSVPStateManager data (reactive) over database data
    val effectiveUserRsvpStates = if (isAuthenticated && currentUserId.isNotEmpty()) {
        userRsvpStates.ifEmpty {
            userRsvps.associate { rsvp -> rsvp.eventId to rsvp.status }
        }
    } else {
        emptyMap<String, RSVPStatus>()
    }
    
    // Filter events based on current filters
    val events = allEvents.filter { event ->
        val matchesQuery = if (searchQuery.isNotBlank()) {
            event.title.contains(searchQuery, ignoreCase = true) ||
            event.description.contains(searchQuery, ignoreCase = true) ||
            event.location.contains(searchQuery, ignoreCase = true)
        } else true
        
        val matchesCategory = if (selectedCategory != null) {
            event.category == selectedCategory
        } else true
        
        val matchesDepartment = if (selectedDepartment != null) {
            event.department == selectedDepartment
        } else true
        
        val matchesStartDate = if (selectedStartDate != null) {
            event.startDateTimeLocal.toLocalDate() >= selectedStartDate
        } else true
        
        val matchesEndDate = if (selectedEndDate != null) {
            event.startDateTimeLocal.toLocalDate() <= selectedEndDate
        } else true
        
        // Filter for selected events (GOING status) when showOnlySelected is true and user is authenticated
        val matchesSelectedFilter = if (showOnlySelectedEvents && isAuthenticated) {
            effectiveUserRsvpStates[event.id] == RSVPStatus.GOING
        } else true
        
        // Only show published events in Discover view
        val isPublishedEvent = event.isPublished
        
        matchesQuery && matchesCategory && matchesDepartment && matchesStartDate && matchesEndDate && matchesSelectedFilter && isPublishedEvent
    }
    
    var searchActive by remember { mutableStateOf(false) }
    
    val categories = listOf(
        stringResource(R.string.category_academic),
        stringResource(R.string.category_cultural),
        stringResource(R.string.category_sports),
        stringResource(R.string.category_social),
        stringResource(R.string.category_workshop),
        stringResource(R.string.category_conference),
        stringResource(R.string.category_other)
    )
    
    val departments = listOf(
        stringResource(R.string.department_software_engineering),
        stringResource(R.string.department_social_sciences),
        stringResource(R.string.department_medicine),
        stringResource(R.string.department_arts),
        stringResource(R.string.department_sports),
        stringResource(R.string.department_student_association),
        stringResource(R.string.department_computer_engineering),
        stringResource(R.string.department_other)
    )
    
    Column(modifier = Modifier.fillMaxSize()) {
        var isSearchExpanded by remember { mutableStateOf(false) }

        if (isSearchExpanded) {
            SearchBar(
                query = searchQuery,
                onQueryChange = viewModel::setSearchQuery,
                onSearch = {
                    viewModel.setSearchQuery(it)
                    isSearchExpanded = false // Auto-collapse after search
                },
                active = searchActive,
                onActiveChange = { searchActive = it },
                placeholder = { Text(stringResource(R.string.search)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { isSearchExpanded = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar búsqueda")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Search suggestions
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.discover),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(
                    onClick = { isSearchExpanded = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        var isFilterExpanded by remember { mutableStateOf(false) }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (selectedCategory != null || selectedDepartment != null || selectedStartDate != null || selectedEndDate != null || showOnlySelectedEvents) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                }
            ),
            border = if (selectedCategory != null || selectedDepartment != null || selectedStartDate != null || selectedEndDate != null || showOnlySelectedEvents) {
                BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            } else null
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Filter Header with Toggle and Clear All button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.filter),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        IconButton(
                            onClick = { isFilterExpanded = !isFilterExpanded },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isFilterExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isFilterExpanded) "Ocultar filtros" else "Mostrar filtros",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    if (selectedCategory != null || selectedDepartment != null || selectedStartDate != null || selectedEndDate != null || showOnlySelectedEvents) {
                        TextButton(
                            onClick = { viewModel.clearFilters() },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "Limpiar Todo",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
                
                
                // Show filters only when expanded
                if (isFilterExpanded) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                LazyRow(
                    contentPadding = PaddingValues(0.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isAuthenticated) {
                        item {
                            FilterChip(
                                selected = showOnlySelectedEvents,
                                onClick = { viewModel.toggleShowOnlySelected() },
                                label = { Text("Solo Seleccionados") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                    
                    // Today filter
                    item {
                        FilterChip(
                            selected = selectedStartDate == LocalDate.now(),
                            onClick = {
                                if (selectedStartDate == LocalDate.now()) {
                                    viewModel.setSelectedStartDate(null)
                                } else {
                                    viewModel.setSelectedStartDate(LocalDate.now())
                                }
                            },
                            label = { Text(stringResource(R.string.today)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                    
                    // This week filter
                    item {
                        FilterChip(
                            selected = selectedStartDate == LocalDate.now() && selectedEndDate == LocalDate.now().plusDays(6),
                            onClick = {
                                if (selectedStartDate == LocalDate.now() && selectedEndDate == LocalDate.now().plusDays(6)) {
                                    viewModel.setSelectedStartDate(null)
                                    viewModel.setSelectedEndDate(null)
                                } else {
                                    viewModel.setSelectedStartDate(LocalDate.now())
                                    viewModel.setSelectedEndDate(LocalDate.now().plusDays(6))
                                }
                            },
                            label = { Text(stringResource(R.string.this_week)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                    
                    // Tomorrow filter
                    item {
                        FilterChip(
                            selected = selectedStartDate == LocalDate.now().plusDays(1),
                            onClick = {
                                if (selectedStartDate == LocalDate.now().plusDays(1)) {
                                    viewModel.setSelectedStartDate(null)
                                } else {
                                    viewModel.setSelectedStartDate(LocalDate.now().plusDays(1))
                                }
                            },
                            label = { Text(stringResource(R.string.tomorrow)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Expandable Categories Section
                var showCategories by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Categorías",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(
                        onClick = { showCategories = !showCategories }
                    ) {
                        Icon(
                            imageVector = if (showCategories) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (showCategories) "Ocultar categorías" else "Mostrar categorías"
                        )
                    }
                }
                
                if (showCategories) {
                    LazyRow(
                        contentPadding = PaddingValues(0.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        items(categories) { category ->
                            FilterChip(
                                selected = category == selectedCategory,
                                onClick = {
                                    if (category == selectedCategory) {
                                        viewModel.setSelectedCategory(null)
                                    } else {
                                        viewModel.setSelectedCategory(category)
                                    }
                                },
                                label = { Text(category) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        }
                    }
                }
                
                // Expandable Departments Section
                var showDepartments by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Departamentos",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(
                        onClick = { showDepartments = !showDepartments }
                    ) {
                        Icon(
                            imageVector = if (showDepartments) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (showDepartments) "Ocultar departamentos" else "Mostrar departamentos"
                        )
                    }
                }
                
                if (showDepartments) {
                    LazyRow(
                        contentPadding = PaddingValues(0.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        items(departments) { department ->
                            FilterChip(
                                selected = department == selectedDepartment,
                                onClick = {
                                    if (department == selectedDepartment) {
                                        viewModel.setSelectedDepartment(null)
                                    } else {
                                        viewModel.setSelectedDepartment(department)
                                    }
                                },
                                label = { Text(department) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer
                                )
                            )
                        }
                    }
                }
                
                
                // Date Range Picker Section
                Text(
                    text = "Rango de Fechas",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Start Date Picker
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.start_date),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        var showStartDatePicker by remember { mutableStateOf(false) }
                        
                        OutlinedButton(
                            onClick = { showStartDatePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedStartDate != null) 
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                                else 
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(
                                text = selectedStartDate?.toString() ?: "Seleccionar fecha",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        if (showStartDatePicker) {
                            val startDatePickerState = rememberDatePickerState(
                                initialSelectedDateMillis = selectedStartDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
                            )
                            
                            DatePickerDialog(
                                onDismissRequest = { showStartDatePicker = false },
                                confirmButton = {
                                    TextButton(
                                        onClick = { 
                                            startDatePickerState.selectedDateMillis?.let { millis ->
                                                val selectedDate = java.time.Instant.ofEpochMilli(millis)
                                                    .atZone(ZoneId.systemDefault())
                                                    .toLocalDate()
                                                viewModel.setSelectedStartDate(selectedDate)
                                            }
                                            showStartDatePicker = false 
                                        }
                                    ) {
                                        Text("OK")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showStartDatePicker = false }) {
                                        Text("Cancelar")
                                    }
                                }
                            ) {
                                DatePicker(
                                    state = startDatePickerState,
                                    title = { Text("Seleccionar fecha de inicio") }
                                )
                            }
                        }
                    }
                    
                    // End Date Picker
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.end_date),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        var showEndDatePicker by remember { mutableStateOf(false) }
                        
                        OutlinedButton(
                            onClick = { showEndDatePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedEndDate != null) 
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                                else 
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(
                                text = selectedEndDate?.toString() ?: "Seleccionar fecha",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        if (showEndDatePicker) {
                            val endDatePickerState = rememberDatePickerState(
                                initialSelectedDateMillis = selectedEndDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
                            )
                            
                            DatePickerDialog(
                                onDismissRequest = { showEndDatePicker = false },
                                confirmButton = {
                                    TextButton(
                                        onClick = { 
                                            endDatePickerState.selectedDateMillis?.let { millis ->
                                                val selectedDate = java.time.Instant.ofEpochMilli(millis)
                                                    .atZone(ZoneId.systemDefault())
                                                    .toLocalDate()
                                                viewModel.setSelectedEndDate(selectedDate)
                                            }
                                            showEndDatePicker = false 
                                        }
                                    ) {
                                        Text("OK")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showEndDatePicker = false }) {
                                        Text("Cancelar")
                                    }
                                }
                            ) {
                                DatePicker(
                                    state = endDatePickerState,
                                    title = { Text("Seleccionar fecha de fin") }
                                )
                            }
                        }
                    }
                }
                
                // Clear Date Range Button
                if (selectedStartDate != null || selectedEndDate != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                viewModel.setSelectedStartDate(null)
                                viewModel.setSelectedEndDate(null)
                            }
                        ) {
                            Text("Limpiar fechas")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
        
        // Filter count indicator
        if (selectedCategory != null || selectedDepartment != null || selectedStartDate != null || selectedEndDate != null || showOnlySelectedEvents) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Mostrando ${events.size} de ${allEvents.size} eventos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (events.size < allEvents.size) {
                    Text(
                        text = "Filtros activos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (events.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.no_events_found),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(events) { event ->
                    // Only show RSVP status for authenticated users
                    val rsvpStatus = if (isAuthenticated) effectiveUserRsvpStates[event.id] else null
                    EventCardWithRSVPStatus(
                        event = event,
                        rsvpStatus = rsvpStatus,
                        onClick = {
                            navController.navigate(NavRoutes.EventDetail.createRoute(event.id))
                        },
                        modifier = Modifier.padding(bottom = 16.dp),
                        isAuthenticating = isAuthenticated
                    )
                }
            }
        }
    }
}

@Composable
fun EventCardWithRSVPStatus(
    event: Event,
    rsvpStatus: RSVPStatus?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAuthenticating: Boolean = false,
) {
    Box(modifier = modifier) {
        // Check icon for confirmed events (GOING status) - only for authenticated users
        if (rsvpStatus == RSVPStatus.GOING) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Evento confirmado",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            )
        }
        
        Column {
            // RSVP Status Badge (if user has RSVP'd)
            rsvpStatus?.let { status ->
                                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                    Icon(
                        imageVector = when (status) {
                            RSVPStatus.GOING -> Icons.Default.CheckCircle
                            RSVPStatus.MAYBE -> Icons.Default.Help
                            RSVPStatus.NOT_GOING -> Icons.Default.Cancel
                        },
                        contentDescription = null,
                        tint = when (status) {
                            RSVPStatus.GOING -> Color(0xFF4CAF50)
                            RSVPStatus.MAYBE -> Color(0xFFFF9800)
                            RSVPStatus.NOT_GOING -> Color(0xFFF44336)
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = when (status) {
                            RSVPStatus.GOING -> "Asistiré"
                            RSVPStatus.MAYBE -> "Tal vez"
                            RSVPStatus.NOT_GOING -> "No asistiré"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = when (status) {
                            RSVPStatus.GOING -> Color(0xFF4CAF50)
                            RSVPStatus.MAYBE -> Color(0xFFFF9800)
                            RSVPStatus.NOT_GOING -> Color(0xFFF44336)
                        },
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Event Card
            EventCard(
                event = event,
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                rsvpStatus = rsvpStatus,
                isAuthenticating = isAuthenticating
            )
        }
    }
}