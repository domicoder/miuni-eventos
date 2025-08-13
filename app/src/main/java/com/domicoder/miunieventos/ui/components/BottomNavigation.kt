package com.domicoder.miunieventos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.domicoder.miunieventos.R
import com.domicoder.miunieventos.ui.navigation.NavRoutes

data class BottomNavItem(
    val route: String,
    val iconVector: ImageVector,
    val stringResourceId: Int
)

@Composable
fun BottomNavigation(
    navController: NavController,
    isOrganizer: Boolean = false,
    userId: String = ""
) {
    val items = listOfNotNull(
        BottomNavItem(
            route = NavRoutes.Discover.route,
            iconVector = Icons.Default.Explore,
            stringResourceId = R.string.discover
        ),
        BottomNavItem(
            route = NavRoutes.MyEvents.route,
            iconVector = Icons.Default.Event,
            stringResourceId = R.string.my_events
        ),
        BottomNavItem(
            route = NavRoutes.Maps.route,
            iconVector = Icons.Default.LocationOn,
            stringResourceId = R.string.maps
        ),
        if (isOrganizer) {
            BottomNavItem(
                route = NavRoutes.ScanQR.route,
                iconVector = Icons.Default.QrCodeScanner,
                stringResourceId = R.string.scan_qr
            )
        } else null,
        BottomNavItem(
            route = "profile", // Use a base route for navigation
            iconVector = Icons.Default.AccountCircle,
            stringResourceId = R.string.profile
        )
    )
    
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route
    
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { 
                    Icon(
                        imageVector = item.iconVector, 
                        contentDescription = stringResource(id = item.stringResourceId),
                        modifier = Modifier.size(24.dp)
                    ) 
                },
                label = {
                    Text(
                        text = stringResource(id = item.stringResourceId),
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 0.dp)
                    ) 
                },
                selected = when {
                    item.route == "profile" -> currentRoute == NavRoutes.Profile.route
                    item.route.startsWith("edit_profile/") -> currentRoute?.startsWith("edit_profile/") == true
                    item.route.startsWith("event_detail/") -> currentRoute?.startsWith("event_detail/") == true
                    else -> currentRoute == item.route
                },
                onClick = {
                    // For profile navigation, handle both authenticated and unauthenticated states
                    if (item.route == "profile") {
                        // Always navigate to the base profile route
                        // The Profile screen will handle authentication state internally
                        navController.navigate(NavRoutes.Profile.route) {
                            navController.graph.startDestinationRoute?.let { route ->
                                popUpTo(route) {
                                    saveState = true
                                }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    } else {
                        // For other routes, use normal navigation
                        navController.navigate(item.route) {
                            navController.graph.startDestinationRoute?.let { route ->
                                popUpTo(route) {
                                    saveState = true
                                }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationPreview() {
    val navController = rememberNavController()
    // In a real app, this would come from a user repository or authentication service
    var isUserOrganizer by remember { mutableStateOf(true) }

    BottomNavigation(
        navController = navController,
        isOrganizer = isUserOrganizer
    )
}

