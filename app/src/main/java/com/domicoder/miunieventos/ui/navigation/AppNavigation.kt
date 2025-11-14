package com.domicoder.miunieventos.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import android.util.Log
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.domicoder.miunieventos.ui.components.BottomNavigation
import com.domicoder.miunieventos.ui.components.LoginPromptScreen
import com.domicoder.miunieventos.ui.discover.DiscoverScreen
import com.domicoder.miunieventos.ui.eventdetail.EventDetailScreen
import com.domicoder.miunieventos.ui.login.LoginScreen
import com.domicoder.miunieventos.ui.register.RegisterScreen
import com.domicoder.miunieventos.ui.maps.MapsScreen
import com.domicoder.miunieventos.ui.myevents.MyEventsScreen
import com.domicoder.miunieventos.ui.profile.ProfileScreen
import com.domicoder.miunieventos.ui.profile.EditProfileScreen
import com.domicoder.miunieventos.ui.scanner.ScannerScreen
import com.domicoder.miunieventos.util.DeepLinkManager
import com.domicoder.miunieventos.util.RSVPStateManager
import com.domicoder.miunieventos.util.UserStateManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.domicoder.miunieventos.ui.organizedevents.OrganizedEventsScreen
import com.domicoder.miunieventos.ui.eventedit.EditEventScreen

@Composable
fun AppNavigation(
    userStateManager: UserStateManager
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    
    // Get user state from UserStateManager
    val currentUser by userStateManager.currentUser.collectAsState()
    val isAuthenticated by userStateManager.isAuthenticated.collectAsState()
    val isUserOrganizer by userStateManager.isOrganizer.collectAsState()
    
    // Deep link handling
    val deepLinkEvent by DeepLinkManager.deepLinkEvent.collectAsState()
    
    // Handle deep link navigation
    LaunchedEffect(deepLinkEvent) {
        deepLinkEvent?.let { eventId ->
            Log.d("AppNavigation", "Deep link received, navigating to event: $eventId")
            navController.navigate(NavRoutes.EventDetail.createRoute(eventId))
            DeepLinkManager.clearDeepLinkEvent()
        }
    }
    
    // Initialize RSVP states from database when user changes
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            // Initialize RSVP states for the current user
            // This would typically come from a repository
            // For now, we'll initialize with empty state
            RSVPStateManager.initializeFromDatabase(emptyList())
        }
    }
    
    // Handle login success
    val onLoginSuccess = { userId: String, rememberMe: Boolean ->
        // Set the current user in UserStateManager to persist the login state
        // This will trigger the authentication state update
        coroutineScope.launch {
            userStateManager.setCurrentUserId(userId, rememberMe)
        }
        navController.navigate(NavRoutes.Discover.route) {
            popUpTo(NavRoutes.Login.route) { inclusive = true }
        }
    }
    
    // Handle logout
    val onLogout = {
        coroutineScope.launch {
            userStateManager.logout()
        }
        navController.navigate(NavRoutes.Login.route) {
            popUpTo(0) { inclusive = true }
        }
    }
    
    // Show main app - login is optional
    Scaffold(
        bottomBar = {
            BottomNavigation(
                navController = navController,
                isOrganizer = isUserOrganizer,
                userId = currentUser?.id ?: ""
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.Discover.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoutes.Discover.route) {
                DiscoverScreen(
                    navController = navController,
                    isAuthenticated = isAuthenticated,
                    currentUserId = currentUser?.id ?: ""
                )
            }
            
            composable(NavRoutes.MyEvents.route) {
                if (isAuthenticated) {
                    MyEventsScreen(
                        navController = navController,
                        currentUserId = currentUser?.id ?: ""
                    )
                } else {
                    // Show login prompt instead of redirecting
                    LoginPromptScreen(
                        onLoginRequest = {
                            navController.navigate(NavRoutes.Login.route)
                        },
                        onRegisterRequest = {
                            navController.navigate(NavRoutes.Register.route)
                        }
                    )
                }
            }
            
            composable(NavRoutes.ScanQR.route) {
                if (isAuthenticated) {
                    ScannerScreen()
                } else {
                    // Show login prompt instead of redirecting
                    LoginPromptScreen(
                        onLoginRequest = {
                            navController.navigate(NavRoutes.Login.route)
                        },
                        onRegisterRequest = {
                            navController.navigate(NavRoutes.Register.route)
                        }
                    )
                }
            }
            
            composable(NavRoutes.Maps.route) {
                MapsScreen(navController = navController)
            }
            
            composable(NavRoutes.Profile.route) {
                if (isAuthenticated && currentUser != null) {
                    ProfileScreen(
                        navController = navController,
                        onLogout = onLogout
                    )
                } else {
                    // Show login prompt instead of redirecting
                    LoginPromptScreen(
                        onLoginRequest = {
                            navController.navigate(NavRoutes.Login.route)
                        },
                        onRegisterRequest = {
                            navController.navigate(NavRoutes.Register.route)
                        }
                    )
                }
            }
            
            composable(
                route = NavRoutes.OrganizedEvents.route,
                arguments = listOf(
                    navArgument("organizerId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val organizerId = backStackEntry.arguments?.getString("organizerId") ?: ""
                OrganizedEventsScreen(
                    navController = navController,
                    organizerId = organizerId
                )
            }
            
            composable(
                route = NavRoutes.EditEvent.route,
                arguments = listOf(
                    navArgument("eventId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                EditEventScreen(
                    navController = navController
                )
            }
            
            composable(NavRoutes.Login.route) {
                LoginScreen(
                    onLoginSuccess = onLoginSuccess,
                    onRegisterRequest = {
                        navController.navigate(NavRoutes.Register.route)
                    }
                )
            }
            
            composable(NavRoutes.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = onLoginSuccess,
                    onLoginRequest = {
                        navController.navigate(NavRoutes.Login.route)
                    }
                )
            }
            
            composable(NavRoutes.EditProfile.route) {
                if (isAuthenticated) {
                    EditProfileScreen(
                        navController = navController
                    )
                } else {
                    // Show login prompt instead of redirecting
                    LoginPromptScreen(
                        onLoginRequest = {
                            navController.navigate(NavRoutes.Login.route)
                        },
                        onRegisterRequest = {
                            navController.navigate(NavRoutes.Register.route)
                        }
                    )
                }
            }
            
            composable(
                route = NavRoutes.EventDetail.route,
                arguments = listOf(
                    navArgument("eventId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId")
                EventDetailScreen(
                    navController = navController,
                    eventId = eventId ?: "",
                    isAuthenticated = isAuthenticated,
                    currentUserId = currentUser?.id ?: "",
                    onLoginRequest = {
                        navController.navigate(NavRoutes.Login.route)
                    }
                )
            }
            
            // Additional routes would be added here
        }
    }
}