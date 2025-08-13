package com.domicoder.miunieventos.ui.navigation

sealed class NavRoutes(val route: String) {
    object Discover : NavRoutes("discover")
    object MyEvents : NavRoutes("my_events")
    object ScanQR : NavRoutes("scan_qr")
    object Maps : NavRoutes("maps")
    object EventDetail : NavRoutes("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
    object Profile : NavRoutes("profile")
    object EditProfile : NavRoutes("edit_profile")
    object Login : NavRoutes("login")
    object Register : NavRoutes("register")
    object CreateEvent : NavRoutes("create_event")
    object EditEvent : NavRoutes("edit_event/{eventId}") {
        fun createRoute(eventId: String) = "edit_event/$eventId"
    }
    object OrganizedEvents : NavRoutes("organized_events/{organizerId}") {
        fun createRoute(organizerId: String) = "organized_events/$organizerId"
    }
} 