package com.roadeye.ui.screens.splash

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.roadeye.domain.repository.AuthRepository
import com.roadeye.domain.repository.UserRepository
import com.roadeye.domain.model.UserRole
import com.roadeye.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    fun getStartDestination(): String {
        return if (authRepository.isLoggedIn) {
            // Check role and navigate to appropriate dashboard
            Screen.CitizenDashboard.route // Will be determined by stored role
        } else {
            Screen.RoleSelection.route
        }
    }
}
