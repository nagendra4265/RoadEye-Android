package com.roadeye.ui.screens.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
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
    val success: Boolean = false,
    // Held so the OTP screen can use resendToken
    val resendToken: PhoneAuthProvider.ForceResendingToken? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // ─────────────────────────────────────────────────────────────
    // Send OTP via real Firebase PhoneAuthProvider
    // ─────────────────────────────────────────────────────────────
    fun sendOtp(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: (verificationId: String) -> Unit,
        onAutoVerified: () -> Unit          // device auto-retrieved the OTP
    ) {
        _uiState.value = AuthUiState(isLoading = true, error = null)

        authRepository.sendOtp(
            phoneNumber = phoneNumber,
            activity = activity,

            onCodeSent = { verificationId, resendToken ->
                _uiState.value = AuthUiState(isLoading = false, resendToken = resendToken)
                onCodeSent(verificationId)
            },

            onVerificationCompleted = { credential ->
                // Auto-retrieval or instant verification (e.g. test numbers)
                viewModelScope.launch {
                    _uiState.value = AuthUiState(isLoading = true)
                    val result = authRepository.signInWithCredential(credential)
                    result.fold(
                        onSuccess = {
                            _uiState.value = AuthUiState(success = true)
                            onAutoVerified()
                        },
                        onFailure = { e ->
                            _uiState.value = AuthUiState(error = e.message)
                        }
                    )
                }
            },

            onError = { errorMsg ->
                _uiState.value = AuthUiState(isLoading = false, error = errorMsg)
            }
        )
    }

    // ─────────────────────────────────────────────────────────────
    // User typed OTP manually
    // ─────────────────────────────────────────────────────────────
    fun verifyOtp(
        verificationId: String,
        otp: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true, error = null)
            val result = authRepository.verifyOtp(verificationId, otp)
            result.fold(
                onSuccess = {
                    _uiState.value = AuthUiState(success = true)
                    onSuccess()
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState(isLoading = false, error = e.message)
                }
            )
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Officer email login
    // ─────────────────────────────────────────────────────────────
    fun loginOfficer(
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true, error = null)
            val result = authRepository.loginOfficer(email, password)
            result.fold(
                onSuccess = {
                    _uiState.value = AuthUiState(success = true)
                    onSuccess()
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
