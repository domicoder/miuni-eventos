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
import com.domicoder.miunieventos.ui.maps.MapsScreen
import com.domicoder.miunieventos.ui.myevents.MyEventsScreen
import com.domicoder.miunieventos.ui.profile.ProfileScreen
import com.domicoder.miunieventos.ui.profile.EditProfileScreen
import com.domicoder.miunieventos.ui.scanner.ScannerScreen
import com.domicoder.miunieventos.util.DeepLinkManager
import com.domicoder.miunieventos.util.RSVPStateManager
import com.domicoder.miunieventos.data.model.UserProfileData

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    // Authentication state
    var isAuthenticated by remember { mutableStateOf(false) }
    var currentUserId by remember { mutableStateOf<String?>(null) }
    var isUserOrganizer by remember { mutableStateOf(false) }
    
    // User profile information state
    var userProfileInfo by remember { mutableStateOf<Map<String, UserProfileData>>(emptyMap()) }
    
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
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            // Initialize RSVP states for the current user
            // This would typically come from a repository
            // For now, we'll initialize with empty state
            RSVPStateManager.initializeFromDatabase(emptyList())
        }
    }
    
    // Initialize user profile data when user ID changes
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            val initialProfileData = when (currentUserId) {
                "user1" -> UserProfileData(
                    name = "Juanito Alimaña",
                    department = "Ingeniería Software"
                )
                "user2" -> UserProfileData(
                    name = "María González",
                    department = "Ciencias Sociales"
                )
                "user3" -> UserProfileData(
                    name = "Carlos Rodríguez",
                    department = "Medicina"
                )
                else -> UserProfileData(
                    name = "Usuario",
                    department = "Departamento"
                )
            }
            userProfileInfo = userProfileInfo + (currentUserId!! to initialProfileData)
        }
    }
    
    // Handle profile updates
    val onProfileUpdated = { userId: String, name: String, department: String ->
        val updatedProfile = UserProfileData(name = name, department = department)
        userProfileInfo = userProfileInfo + (userId to updatedProfile)
    }
    
    // Handle login success
    val onLoginSuccess = { userId: String ->
        currentUserId = userId
        isAuthenticated = true
        // Set organizer status based on hardcoded user IDs (for MVP testing)
        isUserOrganizer = userId in listOf("user1", "user2")
        navController.navigate(NavRoutes.Discover.route) {
            popUpTo(NavRoutes.Login.route) { inclusive = true }
        }
    }
    
    // Handle logout
    val onLogout = {
        isAuthenticated = false
        currentUserId = null
        isUserOrganizer = false
        userProfileInfo = emptyMap()
        navController.navigate(NavRoutes.Login.route) {
            popUpTo(0) { inclusive = true }
        }
    }
    
    // Show main app - login is optional
    Scaffold(
        bottomBar = {
            BottomNavigation(
                navController = navController,
                isOrganizer = isUserOrganizer
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
                    currentUserId = currentUserId ?: "",
                    onLoginRequest = {
                        navController.navigate(NavRoutes.Login.route)
                    }
                )
            }
            
            composable(NavRoutes.MyEvents.route) {
                if (isAuthenticated) {
                    MyEventsScreen(
                        navController = navController,
                        currentUserId = currentUserId ?: ""
                    )
                } else {
                    // Show login prompt instead of redirecting
                    LoginPromptScreen(
                        onLoginRequest = {
                            navController.navigate(NavRoutes.Login.route)
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
                        }
                    )
                }
            }
            
            composable(NavRoutes.Maps.route) {
                MapsScreen(navController = navController)
            }
            
            composable(NavRoutes.Profile.route) {
                if (isAuthenticated) {
                    ProfileScreen(
                        navController = navController,
                        userId = currentUserId ?: "",
                        isOrganizer = isUserOrganizer,
                        userProfileInfo = userProfileInfo[currentUserId] ?: UserProfileData("Usuario", "Departamento"),
                        onLogout = onLogout
                    )
                } else {
                    // Show login prompt instead of redirecting
                    LoginPromptScreen(
                        onLoginRequest = {
                            navController.navigate(NavRoutes.Login.route)
                        }
                    )
                }
            }
            
            composable(NavRoutes.EditProfile.route) {
                if (isAuthenticated) {
                    EditProfileScreen(
                        navController = navController,
                        currentName = when (currentUserId) {
                            "user1" -> "Juanito Alimaña"
                            "user2" -> "María González"
                            "user3" -> "Carlos Rodríguez"
                            else -> "Usuario"
                        },
                        currentDepartment = when (currentUserId) {
                            "user1" -> "Ingeniería Software"
                            "user2" -> "Ciencias Sociales"
                            "user3" -> "Medicina"
                            else -> "Departamento"
                        },
                        currentPhotoUrl = when (currentUserId) {
                            "user1" -> "https://i.pravatar.cc/300?u=user1"
                            "user2" -> "https://i.pravatar.cc/300?u=user2"
                            "user3" -> "https://i.pravatar.cc/300?u=user3"
                            else -> "https://i.pravatar.cc/300?u=default"
                        },
                        onProfileUpdated = { name, department ->
                            // Update the user profile information
                            onProfileUpdated(currentUserId ?: "", name, department)
                            navController.popBackStack()
                        }
                    )
                } else {
                    // Show login prompt or redirect to login
                    navController.navigate(NavRoutes.Login.route)
                }
            }
            
            composable(NavRoutes.Login.route) {
                LoginScreen(
                    onLoginSuccess = onLoginSuccess
                )
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
                    currentUserId = currentUserId ?: "",
                    onLoginRequest = {
                        navController.navigate(NavRoutes.Login.route)
                    }
                )
            }
            
            // Additional routes would be added here
        }
    }
}