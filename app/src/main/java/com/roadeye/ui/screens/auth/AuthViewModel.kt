package com.roadeye.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roadeye.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun sendOtp(phoneNumber: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.sendOtp(phoneNumber)
            result.fold(
                onSuccess = { verificationId ->
                    _uiState.value = AuthUiState(isLoading = false)
                    onSuccess(verificationId)
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun verifyOtp(verificationId: String, otp: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.verifyOtp(verificationId, otp)
            result.fold(
                onSuccess = {
                    _uiState.value = AuthUiState(success = true)
                    onSuccess()
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState(error = e.message)
                }
            )
        }
    }

    fun loginOfficer(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.loginOfficer(email, password)
            result.fold(
                onSuccess = {
                    _uiState.value = AuthUiState(success = true)
                    onSuccess()
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState(error = e.message)
                }
            )
        }
    }
}
