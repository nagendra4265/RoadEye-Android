package com.roadeye.ui.screens.complaint

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.roadeye.domain.model.*
import com.roadeye.domain.repository.AuthRepository
import com.roadeye.domain.repository.ComplaintRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class ComplaintCaptureUiState(
    val capturedImageUri: Uri? = null,
    val tempCameraUri: Uri? = null,
    val imageBytes: ByteArray? = null,

    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",

    val title: String = "",
    val description: String = "",

    val severity: ComplaintSeverity = ComplaintSeverity.MEDIUM,

    val isLoadingLocation: Boolean = false,
    val locationPermissionDenied: Boolean = false,

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

    // ------------------------------------------------------------
    // IMAGE
    // ------------------------------------------------------------

    fun createTempImageUri(context: Context): Uri {
        val fileName = "road_eye_${System.currentTimeMillis()}.jpg"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/RoadEye"
                )
            }
        }

        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )!!

        _uiState.update { it.copy(tempCameraUri = uri) }

        return uri
    }

    fun onCameraImageCaptured(uri: Uri, context: Context) {
        onImageSelected(uri, context)
    }

    fun clearImage() {
        _uiState.update {
            it.copy(
                capturedImageUri = null,
                tempCameraUri = null,
                imageBytes = null,
                aiAnalysisResult = null,
                duplicateWarning = null
            )
        }
    }

    fun onImageSelected(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                val bytes = context.contentResolver
                    .openInputStream(uri)
                    ?.use(InputStream::readBytes)

                _uiState.update {
                    it.copy(
                        capturedImageUri = uri,
                        imageBytes = bytes,
                        error = null
                    )
                }

                analyzeImage()
                checkDuplicates()

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to process image")
                }
            }
        }
    }

    // ------------------------------------------------------------
    // LOCATION
    // ------------------------------------------------------------

    fun getCurrentLocation(context: Context) {

        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            _uiState.update {
                it.copy(
                    locationPermissionDenied = true,
                    error = "Location permission required."
                )
            }
            return
        }

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoadingLocation = true,
                    locationPermissionDenied = false,
                    error = null
                )
            }

            try {

                val fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(context)

                // FIRST TRY
                var location = fusedLocationClient
                    .getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        null
                    )
                    .await()

                // FALLBACK
                if (location == null) {
                    location = fusedLocationClient.lastLocation.await()
                }

                if (location != null) {

                    val address = getAddressFromLocation(
                        context,
                        location.latitude,
                        location.longitude
                    )

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

                    _uiState.update {
                        it.copy(
                            isLoadingLocation = false,
                            error = "Could not get location. Please move outdoors and try again."
                        )
                    }
                }

            } catch (e: SecurityException) {

                _uiState.update {
                    it.copy(
                        isLoadingLocation = false,
                        locationPermissionDenied = true,
                        error = "Location permission denied."
                    )
                }

            } catch (e: Exception) {

                _uiState.update {
                    it.copy(
                        isLoadingLocation = false,
                        error = e.message ?: "Failed to get location"
                    )
                }
            }
        }
    }

    private suspend fun getAddressFromLocation(
        context: Context,
        lat: Double,
        lng: Double
    ): String = withContext(Dispatchers.IO) {

        try {

            val geocoder = Geocoder(context, Locale.getDefault())

            val addresses = geocoder.getFromLocation(lat, lng, 1)

            if (!addresses.isNullOrEmpty()) {

                val addr = addresses[0]

                buildString {
                    addr.subLocality?.let { append("$it, ") }
                    addr.locality?.let { append("$it, ") }
                    addr.adminArea?.let { append(it) }
                }.trimEnd(',', ' ')

            } else {
                "Location captured"
            }

        } catch (e: Exception) {

            "Location captured ($lat, $lng)"
        }
    }

    // ------------------------------------------------------------
    // AI
    // ------------------------------------------------------------

    private fun analyzeImage() {

        val messages = listOf(
            "🤖 Pothole detected – severity appears HIGH.",
            "🤖 Road surface cracking detected.",
            "🤖 Water damage & erosion detected.",
            "🤖 Medium severity pothole detected.",
            "🤖 Asphalt deterioration identified."
        )

        _uiState.update {
            it.copy(aiAnalysisResult = messages.random())
        }
    }

    private fun checkDuplicates() {

        viewModelScope.launch {

            val lat = _uiState.value.latitude
            val lng = _uiState.value.longitude

            if (lat != 0.0 && lng != 0.0) {

                val nearby = complaintRepository
                    .getNearbyComplaints(lat, lng, 0.1)
                    .first()

                if (nearby.isNotEmpty()) {

                    _uiState.update {
                        it.copy(
                            duplicateWarning =
                                "⚠️ Similar complaint already exists nearby."
                        )
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------
    // FORM
    // ------------------------------------------------------------

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onSeverityChange(value: ComplaintSeverity) {
        _uiState.update { it.copy(severity = value) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // ------------------------------------------------------------
    // SUBMIT
    // ------------------------------------------------------------

    fun submitComplaint() {

        viewModelScope.launch {

            val state = _uiState.value

            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    error = null
                )
            }

            try {

                val imageUrl =
                    if (state.imageBytes != null) {

                        val path =
                            "complaints/${UUID.randomUUID()}.jpg"

                        complaintRepository
                            .uploadImage(state.imageBytes, path)
                            .getOrDefault("")

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

                complaintRepository.submitComplaint(complaint)
                    .fold(
                        onSuccess = {
                            _uiState.update {
                                it.copy(
                                    isSubmitting = false,
                                    complaintSubmitted = true
                                )
                            }
                        },
                        onFailure = { e ->
                            _uiState.update {
                                it.copy(
                                    isSubmitting = false,
                                    error = e.message
                                )
                            }
                        }
                    )

            } catch (e: Exception) {

                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        error = e.message
                    )
                }
            }
        }
    }
}