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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    navController: NavController,
    isAuthenticated: Boolean = false,
    currentUserId: String = "",
    onLoginRequest: () -> Unit = {},
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    val allEvents by viewModel.events.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedDepartment by viewModel.selectedDepartment.collectAsState()
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
        if (userRsvpStates.isNotEmpty()) {
            userRsvpStates
        } else {
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
        
        // Filter for selected events (GOING status) when showOnlySelected is true and user is authenticated
        val matchesSelectedFilter = if (showOnlySelectedEvents && isAuthenticated) {
            effectiveUserRsvpStates[event.id] == RSVPStatus.GOING
        } else true
        
        matchesQuery && matchesCategory && matchesDepartment && matchesSelectedFilter
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
    
    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            query = searchQuery,
            onQueryChange = viewModel::setSearchQuery,
            onSearch = { viewModel.setSearchQuery(it) },
            active = searchActive,
            onActiveChange = { searchActive = it },
            placeholder = { Text(stringResource(R.string.search)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Search suggestions could go here
        }
        
        // Categories filter
        Text(
            text = stringResource(R.string.filter),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            // Filter for showing only selected events (only for authenticated users)
            if (isAuthenticated) {
                item {
                    FilterChip(
                        selected = showOnlySelectedEvents,
                        onClick = { viewModel.toggleShowOnlySelected() },
                        label = { Text("Solo Seleccionados") },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

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
                    modifier = Modifier.padding(end = 8.dp)
                )
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
                        modifier = Modifier.padding(bottom = 16.dp)
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
    modifier: Modifier = Modifier
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
                isConfirmed = rsvpStatus == RSVPStatus.GOING
            )
        }
    }
} 