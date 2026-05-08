package com.roadeye.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.roadeye.ui.screens.auth.LoginScreen
import com.roadeye.ui.screens.auth.OtpVerificationScreen
import com.roadeye.ui.screens.citizen.CitizenDashboardScreen
import com.roadeye.ui.screens.complaint.*
import com.roadeye.ui.screens.map.MapScreen
import com.roadeye.ui.screens.notifications.NotificationsScreen
import com.roadeye.ui.screens.officer.OfficerComplaintDetailScreen
import com.roadeye.ui.screens.officer.OfficerDashboardScreen
import com.roadeye.ui.screens.profile.ProfileScreen
import com.roadeye.ui.screens.roleselection.RoleSelectionScreen
import com.roadeye.ui.screens.splash.SplashScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RoadEyeNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { 300 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -300 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -300 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { 300 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(navController = navController)
        }

        composable(
            route = Screen.Login.route,
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "CITIZEN"
            LoginScreen(navController = navController, role = role)
        }

        composable(
            route = Screen.OtpVerification.route,
            arguments = listOf(
                navArgument("phone") { type = NavType.StringType },
                navArgument("verificationId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            val verificationId = backStackEntry.arguments?.getString("verificationId") ?: ""
            OtpVerificationScreen(
                navController = navController,
                phone = phone,
                verificationId = verificationId
            )
        }

        composable(Screen.CitizenDashboard.route) {
            CitizenDashboardScreen(navController = navController)
        }

        composable(Screen.OfficerDashboard.route) {
            OfficerDashboardScreen(navController = navController)
        }

        composable(Screen.ComplaintCapture.route) {
            ComplaintCaptureScreen(navController = navController)
        }

        composable(
            route = Screen.ComplaintDetail.route,
            arguments = listOf(navArgument("complaintId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("complaintId") ?: ""
            ComplaintDetailScreen(navController = navController, complaintId = id)
        }

        composable(Screen.ComplaintTracking.route) {
            ComplaintTrackingScreen(navController = navController)
        }

        composable(
            route = Screen.MapView.route,
            arguments = listOf(navArgument("complaintId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("complaintId") ?: "all"
            MapScreen(navController = navController, complaintId = id)
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(navController = navController)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }

        composable(
            route = Screen.OfficerComplaintDetail.route,
            arguments = listOf(navArgument("complaintId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("complaintId") ?: ""
            OfficerComplaintDetailScreen(navController = navController, complaintId = id)
        }
    }
}
