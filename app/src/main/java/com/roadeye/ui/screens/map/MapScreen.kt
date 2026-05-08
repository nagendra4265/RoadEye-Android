package com.roadeye.ui.screens.map

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.roadeye.domain.model.*
import com.roadeye.domain.repository.ComplaintRepository
import com.roadeye.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    complaintId: String,
    viewModel: MapViewModel = hiltViewModel()
) {
    LaunchedEffect(complaintId) { viewModel.loadComplaints(complaintId) }
    val complaints by viewModel.complaints.collectAsState()
    val focusedComplaint by viewModel.focusedComplaint.collectAsState()

    val defaultLocation = LatLng(16.5062, 80.6480) // Vijayawada, AP
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            focusedComplaint?.let { LatLng(it.latitude, it.longitude) } ?: defaultLocation,
            if (focusedComplaint != null) 16f else 12f
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Complaint Map", fontWeight = FontWeight.Bold)
                        Text("${complaints.size} complaints", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onPrimary.copy(0.8f)))
                    }
                },
                navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RoadEyeBlue, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = false),
                uiSettings = MapUiSettings(zoomControlsEnabled = true)
            ) {
                complaints.forEach { complaint ->
                    val color = when (complaint.severity) {
                        ComplaintSeverity.HIGH -> com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                            com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED)
                        ComplaintSeverity.MEDIUM -> com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                            com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_ORANGE)
                        ComplaintSeverity.LOW -> com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                            com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN)
                    }
                    Marker(
                        state = MarkerState(position = LatLng(complaint.latitude, complaint.longitude)),
                        title = complaint.title,
                        snippet = "${complaint.severity.displayName} • ${complaint.status.displayName}",
                        icon = color
                    )
                }
            }

            // Legend
            Card(
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(0.95f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Legend", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Text("🔴 High Severity", style = MaterialTheme.typography.labelSmall)
                    Text("🟠 Medium Severity", style = MaterialTheme.typography.labelSmall)
                    Text("🟢 Low Severity", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@HiltViewModel
class MapViewModel @Inject constructor(
    private val complaintRepository: ComplaintRepository
) : ViewModel() {

    private val _complaints = MutableStateFlow<List<Complaint>>(emptyList())
    val complaints: StateFlow<List<Complaint>> = _complaints.asStateFlow()

    private val _focusedComplaint = MutableStateFlow<Complaint?>(null)
    val focusedComplaint: StateFlow<Complaint?> = _focusedComplaint.asStateFlow()

    fun loadComplaints(complaintId: String) {
        viewModelScope.launch {
            if (complaintId == "all") {
                complaintRepository.getComplaints().collect { list ->
                    _complaints.value = list.filter { it.latitude != 0.0 }
                }
            } else {
                complaintRepository.getComplaintById(complaintId).collect { c ->
                    c?.let {
                        _focusedComplaint.value = it
                        _complaints.value = listOf(it)
                    }
                }
            }
        }
    }
}
