package com.domicoder.miunieventos.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.domicoder.miunieventos.ui.components.BottomNavigation
import com.domicoder.miunieventos.ui.discover.DiscoverScreen
import com.domicoder.miunieventos.ui.eventdetail.EventDetailScreen
import com.domicoder.miunieventos.ui.maps.MapsScreen
import com.domicoder.miunieventos.ui.myevents.MyEventsScreen
import com.domicoder.miunieventos.ui.profile.ProfileScreen
import com.domicoder.miunieventos.ui.scanner.ScannerScreen

@Composable
fun AppNavigation(
    startDestination: String = NavRoutes.Discover.route
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // In a real app, this would come from a user repository or authentication service
    var isUserOrganizer by remember { mutableStateOf(true) }
    
    val shouldShowBottomBar = remember(currentRoute) {
        when {
            currentRoute == NavRoutes.Discover.route -> true
            currentRoute == NavRoutes.MyEvents.route -> true
            currentRoute == NavRoutes.Maps.route -> true
            currentRoute == NavRoutes.ScanQR.route -> true
            currentRoute == NavRoutes.Profile.route -> true
            else -> false
        }
    }
    
    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomNavigation(
                    navController = navController,
                    isOrganizer = isUserOrganizer
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoutes.Discover.route) {
                DiscoverScreen(navController = navController)
            }
            
            composable(NavRoutes.MyEvents.route) {
                MyEventsScreen(navController = navController)
            }
            
            composable(NavRoutes.ScanQR.route) {
                ScannerScreen()
            }
            
            composable(NavRoutes.Maps.route) {
                MapsScreen(navController = navController)
            }
            
            composable(NavRoutes.Profile.route) {
                ProfileScreen(navController = navController)
            }
            
            composable(
                route = NavRoutes.EventDetail.route,
                arguments = listOf(
                    navArgument("eventId") {
                        type = NavType.StringType
                    }
                )
            ) {
                EventDetailScreen(navController = navController)
            }
            
            // Additional routes would be added here
        }
    }
} 