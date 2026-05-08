package com.roadeye.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object RoleSelection : Screen("role_selection")
    object Login : Screen("login/{role}") {
        fun createRoute(role: String) = "login/$role"
    }
    object OtpVerification : Screen("otp/{phone}/{verificationId}") {
        fun createRoute(phone: String, verificationId: String) = "otp/$phone/$verificationId"
    }
    object CitizenDashboard : Screen("citizen_dashboard")
    object OfficerDashboard : Screen("officer_dashboard")
    object ComplaintCapture : Screen("complaint_capture")
    object ComplaintDetail : Screen("complaint_detail/{complaintId}") {
        fun createRoute(id: String) = "complaint_detail/$id"
    }
    object ComplaintTracking : Screen("complaint_tracking")
    object MapView : Screen("map_view/{complaintId}") {
        fun createRoute(id: String = "all") = "map_view/$id"
    }
    object Notifications : Screen("notifications")
    object Profile : Screen("profile")
    object OfficerComplaintDetail : Screen("officer_complaint_detail/{complaintId}") {
        fun createRoute(id: String) = "officer_complaint_detail/$id"
    }
}
