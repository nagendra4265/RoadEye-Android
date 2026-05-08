package com.roadeye.ui.screens.citizen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roadeye.domain.model.*
import com.roadeye.domain.repository.ComplaintRepository
import com.roadeye.domain.repository.UserRepository
import com.roadeye.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CitizenDashboardUiState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val district: String = "Vijayawada",
    val stats: DashboardStats = DashboardStats(),
    val roadHealthScore: Int = 72,
    val error: String? = null
)

@HiltViewModel
class CitizenDashboardViewModel @Inject constructor(
    private val complaintRepository: ComplaintRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CitizenDashboardUiState())
    val uiState: StateFlow<CitizenDashboardUiState> = _uiState.asStateFlow()

    val complaints: StateFlow<List<Complaint>> = complaintRepository
        .getUserComplaints(authRepository.currentUserId ?: "demo_user")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val user = authRepository.getCurrentUser()
                val stats = userRepository.getDashboardStats(
                    authRepository.currentUserId,
                    UserRole.CITIZEN
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userName = user?.name ?: "Citizen",
                        stats = stats,
                        roadHealthScore = calculateHealthScore(stats)
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun calculateHealthScore(stats: DashboardStats): Int {
        if (stats.totalComplaints == 0) return 85
        val resolvedRatio = stats.resolvedCount.toFloat() / stats.totalComplaints
        val highSeverityPenalty = stats.highSeverityCount * 5
        return (100 * resolvedRatio - highSeverityPenalty + 30).toInt().coerceIn(0, 100)
    }
}
