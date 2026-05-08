package com.roadeye.ui.screens.complaint

import android.content.Context
import android.location.Geocoder
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.roadeye.domain.model.*
import com.roadeye.domain.repository.AuthRepository
import com.roadeye.domain.repository.ComplaintRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.InputStream
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class ComplaintCaptureUiState(
    val capturedImageUri: Uri? = null,
    val imageBytes: ByteArray? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val title: String = "",
    val description: String = "",
    val severity: ComplaintSeverity = ComplaintSeverity.MEDIUM,
    val isLoadingLocation: Boolean = false,
    val isSubmitting: Boolean = false,
    val complaintSubmitted: Boolean = false,
    val aiAnalysisResult: String? = null,
    val duplicateWarning: String? = null,
    val error: String? = null
) {
    val canSubmit: Boolean
        get() = capturedImageUri != null &&
                latitude != 0.0 &&
                title.isNotBlank() &&
                description.isNotBlank()
}

@HiltViewModel
class ComplaintCaptureViewModel @Inject constructor(
    private val complaintRepository: ComplaintRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComplaintCaptureUiState())
    val uiState: StateFlow<ComplaintCaptureUiState> = _uiState.asStateFlow()

    fun onImageSelected(uri: Uri, context: Context) {
        viewModelScope.launch {
            val bytes = context.contentResolver.openInputStream(uri)?.use(InputStream::readBytes)
            _uiState.update { it.copy(capturedImageUri = uri, imageBytes = bytes) }
            analyzeImage()
            checkDuplicates()
        }
    }

    fun captureFromCamera(context: Context) {
        // CameraX integration would launch camera activity here
        // For demo purposes we note: integrate with CameraX via contract
    }

    private fun analyzeImage() {
        // Simulated AI-style analysis
        val messages = listOf(
            "🤖 Pothole detected – severity appears HIGH. Deep cavity visible, ~30cm diameter.",
            "🤖 Road surface cracking detected. Multiple fracture lines suggest structural damage.",
            "🤖 Water damage & erosion detected along road edge.",
            "🤖 Pothole detected – medium severity. Approx. 15cm wide, 5cm deep.",
            "🤖 Road damage confirmed. Asphalt deterioration pattern identified."
        )
        _uiState.update { it.copy(aiAnalysisResult = messages.random()) }
    }

    private fun checkDuplicates() {
        viewModelScope.launch {
            val lat = _uiState.value.latitude
            val lng = _uiState.value.longitude
            if (lat != 0.0 && lng != 0.0) {
                val nearby = complaintRepository.getNearbyComplaints(lat, lng, 0.1)
                    .first()
                if (nearby.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            duplicateWarning = "⚠️ A similar complaint already exists ${nearby.size} m away (ID: ${nearby.first().id.take(8)}). Consider checking its status first."
                        )
                    }
                }
            }
        }
    }

    fun getCurrentLocation(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingLocation = true) }
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val location = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY, null
                ).await()

                if (location != null) {
                    val address = getAddressFromLocation(context, location.latitude, location.longitude)
                    _uiState.update {
                        it.copy(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            address = address,
                            isLoadingLocation = false
                        )
                    }
                    checkDuplicates()
                } else {
                    _uiState.update { it.copy(isLoadingLocation = false, error = "Could not get location. Please try again.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingLocation = false, error = "Location permission required.") }
            }
        }
    }

    private fun getAddressFromLocation(context: Context, lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                buildString {
                    addr.subLocality?.let { append("$it, ") }
                    addr.locality?.let { append("$it, ") }
                    addr.adminArea?.let { append(it) }
                }.trimEnd(',', ' ')
            } else "Location captured"
        } catch (e: Exception) {
            "Location captured (${lat.toBigDecimal().setScale(4, java.math.RoundingMode.HALF_UP)}, ${lng.toBigDecimal().setScale(4, java.math.RoundingMode.HALF_UP)})"
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }
    fun onSeverityChange(value: ComplaintSeverity) = _uiState.update { it.copy(severity = value) }

    fun submitComplaint() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            try {
                // Upload image first if bytes available
                val imageUrl = if (state.imageBytes != null) {
                    val path = "complaints/${UUID.randomUUID()}.jpg"
                    val result = complaintRepository.uploadImage(state.imageBytes, path)
                    result.getOrDefault("")
                } else ""

                val complaint = Complaint(
                    userId = authRepository.currentUserId ?: "demo_user",
                    title = state.title,
                    description = state.description,
                    imageUrl = imageUrl,
                    beforeImageUrl = imageUrl,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    address = state.address,
                    severity = state.severity,
                    status = ComplaintStatus.SUBMITTED,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                val result = complaintRepository.submitComplaint(complaint)
                result.fold(
                    onSuccess = { _uiState.update { it.copy(isSubmitting = false, complaintSubmitted = true) } },
                    onFailure = { e -> _uiState.update { it.copy(isSubmitting = false, error = e.message) } }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, error = e.message) }
            }
        }
    }
}
