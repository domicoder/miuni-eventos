package com.domicoder.miunieventos.ui.components

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
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.domicoder.miunieventos.R
import com.domicoder.miunieventos.ui.navigation.NavRoutes

data class BottomNavItem(
    val route: String,
    val iconVector: androidx.compose.ui.graphics.vector.ImageVector,
    val stringResourceId: Int
)

@Composable
fun BottomNavigation(
    navController: NavController,
    isOrganizer: Boolean = false
) {
    val items = listOf(
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
            route = NavRoutes.Profile.route,
            iconVector = Icons.Default.AccountCircle,
            stringResourceId = R.string.profile
        )
    ).filterNotNull()
    
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route
    
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.iconVector, contentDescription = null) },
                label = { Text(text = stringResource(id = item.stringResourceId)) },
                selected = currentRoute == item.route,
                onClick = {
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
            )
        }
    }
} 