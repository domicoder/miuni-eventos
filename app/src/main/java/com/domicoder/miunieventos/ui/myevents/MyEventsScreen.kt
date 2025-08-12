package com.domicoder.miunieventos.ui.myevents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.domicoder.miunieventos.R
import com.domicoder.miunieventos.data.model.RSVPStatus
import com.domicoder.miunieventos.ui.components.EventCard
import com.domicoder.miunieventos.ui.navigation.NavRoutes

@Composable
fun MyEventsScreen(
    navController: NavController,
    currentUserId: String = "",
    viewModel: MyEventsViewModel = hiltViewModel()
) {
    val myEvents by viewModel.myEvents.collectAsState()
    
    // Set the current user ID in the ViewModel
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            viewModel.setCurrentUserId(currentUserId)
        }
    }
    
    if (myEvents.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No tienes eventos confirmados",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Confirma asistencia a eventos para verlos aquí",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(myEvents) { eventWithRSVP ->
                EventCardWithRSVPStatus(
                    eventWithRSVP = eventWithRSVP,
                    onClick = {
                        navController.navigate(NavRoutes.EventDetail.createRoute(eventWithRSVP.event.id))
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
fun EventCardWithRSVPStatus(
    eventWithRSVP: EventWithRSVP,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // RSVP Status Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = when (eventWithRSVP.rsvpStatus) {
                        RSVPStatus.GOING -> Icons.Default.CheckCircle
                        RSVPStatus.MAYBE -> Icons.Default.Help
                        RSVPStatus.NOT_GOING -> Icons.Default.Cancel
                    },
                    contentDescription = null,
                    tint = when (eventWithRSVP.rsvpStatus) {
                        RSVPStatus.GOING -> Color(0xFF4CAF50) // Green
                        RSVPStatus.MAYBE -> Color(0xFFFF9800) // Orange
                        RSVPStatus.NOT_GOING -> Color(0xFFF44336) // Red
                    },
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = when (eventWithRSVP.rsvpStatus) {
                        RSVPStatus.GOING -> "Asistiré"
                        RSVPStatus.MAYBE -> "Tal vez"
                        RSVPStatus.NOT_GOING -> "No asistiré"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = when (eventWithRSVP.rsvpStatus) {
                        RSVPStatus.GOING -> Color(0xFF4CAF50)
                        RSVPStatus.MAYBE -> Color(0xFFFF9800)
                        RSVPStatus.NOT_GOING -> Color(0xFFF44336)
                    },
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Event Card
            EventCard(
                event = eventWithRSVP.event,
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
} 