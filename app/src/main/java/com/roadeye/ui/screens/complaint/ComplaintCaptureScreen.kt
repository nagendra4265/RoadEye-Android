package com.roadeye.ui.screens.complaint

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.permissions.*
import com.roadeye.domain.model.ComplaintSeverity
import com.roadeye.ui.navigation.Screen
import com.roadeye.ui.theme.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ComplaintCaptureScreen(
    navController: NavController,
    viewModel: ComplaintCaptureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val multiplePermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it, context) }
    }

    // Handle success navigation
    LaunchedEffect(uiState.complaintSubmitted) {
        if (uiState.complaintSubmitted) {
            navController.navigate(Screen.CitizenDashboard.route) {
                popUpTo(Screen.ComplaintCapture.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Report Road Damage", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("రోడ్ నష్టాన్ని నివేదించండి", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onPrimary.copy(0.8f)))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RoadEyeBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image Capture Section
            ImageCaptureSection(
                imageUri = uiState.capturedImageUri,
                aiAnalysisResult = uiState.aiAnalysisResult,
                duplicateWarning = uiState.duplicateWarning,
                onCameraClick = {
                    if (multiplePermissionsState.allPermissionsGranted) {
                        viewModel.captureFromCamera(context)
                    } else {
                        multiplePermissionsState.launchMultiplePermissionRequest()
                    }
                },
                onGalleryClick = { imagePickerLauncher.launch("image/*") }
            )

            // Location Section
            LocationSection(
                address = uiState.address,
                latitude = uiState.latitude,
                longitude = uiState.longitude,
                isLoadingLocation = uiState.isLoadingLocation,
                onRefreshLocation = { viewModel.getCurrentLocation(context) }
            )

            // Complaint Details
            ComplaintDetailsSection(
                title = uiState.title,
                description = uiState.description,
                severity = uiState.severity,
                onTitleChange = viewModel::onTitleChange,
                onDescriptionChange = viewModel::onDescriptionChange,
                onSeverityChange = viewModel::onSeverityChange
            )

            // Submit Button
            Button(
                onClick = { viewModel.submitComplaint() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = uiState.canSubmit && !uiState.isSubmitting,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoadEyeBlue)
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(12.dp))
                    Text("Submitting...", color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Submit Complaint", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            uiState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(error, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        }
    }
}

@Composable
fun ImageCaptureSection(
    imageUri: Uri?,
    aiAnalysisResult: String?,
    duplicateWarning: String?,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "📷 Photo Evidence",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                "ఫోటో సాక్ష్యం",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline)
            )

            Spacer(Modifier.height(12.dp))

            if (imageUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Captured damage",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Re-capture overlay button
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp),
                        color = Color.Black.copy(0.6f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        IconButton(onClick = onCameraClick, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "Retake", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // AI Analysis Result
                aiAnalysisResult?.let {
                    Spacer(Modifier.height(12.dp))
                    AiAnalysisCard(result = it)
                }

                // Duplicate warning
                duplicateWarning?.let {
                    Spacer(Modifier.height(8.dp))
                    DuplicateWarningCard(message = it)
                }
            } else {
                // Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface)
                            )
                        )
                        .border(
                            2.dp,
                            RoadEyeBlue.copy(0.3f),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🛣️", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Take a photo of the road damage",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.outline
                            )
                        )
                        Text(
                            "నష్టానికి ఫోటో తీయండి",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.outline.copy(0.7f)
                            )
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCameraClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoadEyeBlue),
                        border = BorderStroke(1.5.dp, RoadEyeBlue)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Camera")
                    }
                    OutlinedButton(
                        onClick = onGalleryClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoadEyeBlue),
                        border = BorderStroke(1.5.dp, RoadEyeBlue)
                    ) {
                        Icon(Icons.Default.Photo, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Gallery")
                    }
                }
            }
        }
    }
}

@Composable
fun AiAnalysisCard(result: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🤖", fontSize = 20.sp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    "AI Analysis",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7B1FA2)
                    )
                )
                Text(
                    result,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF4A148C))
                )
            }
        }
    }
}

@Composable
fun DuplicateWarningCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFFFC107))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF57C00), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Duplicate Warning", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFFF57C00)))
                Text(message, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF795548)))
            }
        }
    }
}

@Composable
fun LocationSection(
    address: String,
    latitude: Double,
    longitude: Double,
    isLoadingLocation: Boolean,
    onRefreshLocation: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = SeverityHigh, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Location", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text("స్థానం", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline))
                    }
                }
                if (isLoadingLocation) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = RoadEyeBlue)
                } else {
                    IconButton(onClick = onRefreshLocation, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Refresh location", tint = RoadEyeBlue, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (address.isNotEmpty()) {
                Surface(
                    color = RoadEyeGreenContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RoadEyeGreen, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(address, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                            Text("GPS: ${"%.6f".format(latitude)}, ${"%.6f".format(longitude)}", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline))
                        }
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onRefreshLocation,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Detect My Location")
                }
            }
        }
    }
}

@Composable
fun ComplaintDetailsSection(
    title: String,
    description: String,
    severity: ComplaintSeverity,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSeverityChange: (ComplaintSeverity) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📝 Complaint Details", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Text("ఫిర్యాదు వివరాలు", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline))

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Title (e.g. Large Pothole on Main Road)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Description") },
                placeholder = { Text("Describe the road damage in detail...") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 5
            )

            Spacer(Modifier.height(16.dp))

            Text("Severity Level • తీవ్రత స్థాయి", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium))
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ComplaintSeverity.values().forEach { s ->
                    val isSelected = severity == s
                    val color = when (s) {
                        ComplaintSeverity.HIGH -> SeverityHigh
                        ComplaintSeverity.MEDIUM -> SeverityMedium
                        ComplaintSeverity.LOW -> SeverityLow
                    }
                    FilterChip(
                        modifier = Modifier.weight(1f),
                        selected = isSelected,
                        onClick = { onSeverityChange(s) },
                        label = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    s.displayName,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(0.2f),
                            selectedLabelColor = color
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            selectedBorderColor = color,
                            borderColor = MaterialTheme.colorScheme.outline.copy(0.3f)
                        )
                    )
                }
            }
        }
    }
}
