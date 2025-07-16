package com.domicoder.miunieventos.ui.myevents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.domicoder.miunieventos.R
import com.domicoder.miunieventos.ui.components.EventCard
import com.domicoder.miunieventos.ui.navigation.NavRoutes

@Composable
fun MyEventsScreen(
    navController: NavController,
    viewModel: MyEventsViewModel = hiltViewModel()
) {
    val myEvents by viewModel.myEvents.collectAsState()
    
    if (myEvents.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.no_rsvps_found),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(myEvents) { event ->
                EventCard(
                    event = event,
                    onClick = {
                        navController.navigate(NavRoutes.EventDetail.createRoute(event.id))
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
} 