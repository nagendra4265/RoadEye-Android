package com.roadeye.ui.screens.officer

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.roadeye.domain.model.*
import com.roadeye.domain.repository.ComplaintRepository
import com.roadeye.ui.components.*
import com.roadeye.ui.navigation.Screen
import com.roadeye.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.InputStream
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficerComplaintDetailScreen(
    navController: NavController,
    complaintId: String,
    viewModel: OfficerComplaintDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(complaintId) { viewModel.loadComplaint(complaintId) }
    val complaint by viewModel.complaint.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.setAfterImage(it, context) }
    }

    LaunchedEffect(uiState.updated) {
        if (uiState.updated) navController.navigateUp()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complaint Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    complaint?.let {
                        IconButton(onClick = { navController.navigate(Screen.MapView.createRoute(it.id)) }) {
                            Icon(Icons.Default.Map, contentDescription = "Map")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A237E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        complaint?.let { c ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                // Damage image
                Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                    if (c.imageUrl.isNotEmpty()) {
                        AsyncImage(model = c.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                            Text("🛣️", fontSize = 56.sp)
                        }
                    }
                    // Severity overlay
                    Surface(modifier = Modifier.align(Alignment.TopEnd).padding(12.dp), color = Color.Black.copy(0.6f), shape = RoundedCornerShape(8.dp)) {
                        SeverityBadge(severity = c.severity)
                    }
                }

                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Status + title
                    StatusBadge(status = c.status)
                    Text(c.title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                    Text(c.description, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(0.7f)))

                    // Citizen info
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Citizen Info", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Row { Text("👤 ${c.userName.ifEmpty { "Anonymous" }}", style = MaterialTheme.typography.bodySmall) }
                            Row { Text("📞 ${c.userPhone.ifEmpty { "N/A" }}", style = MaterialTheme.typography.bodySmall) }
                            Row { Text("📅 ${formatDate(c.createdAt)}", style = MaterialTheme.typography.bodySmall) }
                            Row { Text("📍 ${c.address.ifEmpty { "GPS: ${c.latitude}, ${c.longitude}" }}", style = MaterialTheme.typography.bodySmall) }
                        }
                    }

                    // Update status section
                    if (c.status != ComplaintStatus.RESOLVED) {
                        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(3.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("🔧 Update Status", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text("స్థితిని అప్‌డేట్ చేయండి", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline))

                                Spacer(Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = uiState.officerNotes,
                                    onValueChange = viewModel::setOfficerNotes,
                                    label = { Text("Officer Notes / Remarks") },
                                    modifier = Modifier.fillMaxWidth().height(100.dp),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(Modifier.height(12.dp))

                                // After repair image
                                Text("After Repair Photo (optional)", style = MaterialTheme.typography.labelMedium)
                                Spacer(Modifier.height(8.dp))
                                if (uiState.afterImageUri != null) {
                                    AsyncImage(
                                        model = uiState.afterImageUri,
                                        contentDescription = "After repair",
                                        modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    OutlinedButton(onClick = { imagePickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Upload After Repair Photo")
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                // Action Buttons
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    if (c.status == ComplaintStatus.SUBMITTED) {
                                        OutlinedButton(
                                            onClick = { viewModel.updateStatus(c.id, ComplaintStatus.IN_PROGRESS) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusInProgress),
                                            border = BorderStroke(1.5.dp, StatusInProgress),
                                            enabled = !uiState.isUpdating
                                        ) {
                                            Text("Start Work", fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Button(
                                        onClick = { viewModel.updateStatus(c.id, ComplaintStatus.RESOLVED) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = StatusResolved),
                                        enabled = !uiState.isUpdating
                                    ) {
                                        if (uiState.isUpdating) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                        } else {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Mark Resolved", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Already resolved
                        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = RoadEyeGreenContainer)) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("✅", fontSize = 24.sp)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Complaint Resolved", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = RoadEyeGreen))
                                    c.resolvedAt?.let { Text("Resolved on ${formatDate(it)}", style = MaterialTheme.typography.bodySmall.copy(color = RoadEyeGreen.copy(0.7f))) }
                                }
                            }
                        }
                        if (c.afterImageUrl.isNotEmpty()) {
                            Text("After Repair", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            AsyncImage(model = c.afterImageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
                        }
                    }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = RoadEyeBlue)
        }
    }
}

data class OfficerDetailUiState(
    val isUpdating: Boolean = false,
    val officerNotes: String = "",
    val afterImageUri: Uri? = null,
    val afterImageBytes: ByteArray? = null,
    val updated: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class OfficerComplaintDetailViewModel @Inject constructor(
    private val complaintRepository: ComplaintRepository
) : ViewModel() {

    private val _complaint = MutableStateFlow<Complaint?>(null)
    val complaint: StateFlow<Complaint?> = _complaint.asStateFlow()

    private val _uiState = MutableStateFlow(OfficerDetailUiState())
    val uiState: StateFlow<OfficerDetailUiState> = _uiState.asStateFlow()

    fun loadComplaint(id: String) {
        viewModelScope.launch {
            complaintRepository.getComplaintById(id).collect { _complaint.value = it }
        }
    }

    fun setOfficerNotes(notes: String) = _uiState.update { it.copy(officerNotes = notes) }

    fun setAfterImage(uri: Uri, context: android.content.Context) {
        viewModelScope.launch {
            val bytes = context.contentResolver.openInputStream(uri)?.use(InputStream::readBytes)
            _uiState.update { it.copy(afterImageUri = uri, afterImageBytes = bytes) }
        }
    }

    fun updateStatus(complaintId: String, status: ComplaintStatus) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            val state = _uiState.value
            val afterImageUrl = if (state.afterImageBytes != null) {
                val path = "complaints/after/${java.util.UUID.randomUUID()}.jpg"
                complaintRepository.uploadImage(state.afterImageBytes, path).getOrDefault("")
            } else ""

            val result = complaintRepository.updateComplaintStatus(
                complaintId = complaintId,
                status = status,
                officerNotes = state.officerNotes,
                afterImageUrl = afterImageUrl
            )
            result.fold(
                onSuccess = { _uiState.update { it.copy(isUpdating = false, updated = true) } },
                onFailure = { e -> _uiState.update { it.copy(isUpdating = false, error = e.message) } }
            )
        }
    }
}
