package com.roadeye.ui.screens.complaint

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.roadeye.domain.model.ComplaintStatus
import com.roadeye.ui.components.*
import com.roadeye.ui.navigation.Screen
import com.roadeye.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintDetailScreen(
    navController: NavController,
    complaintId: String,
    viewModel: ComplaintDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(complaintId) { viewModel.loadComplaint(complaintId) }
    val complaint by viewModel.complaint.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complaint Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    complaint?.let {
                        IconButton(onClick = {
                            navController.navigate(Screen.MapView.createRoute(it.id))
                        }) {
                            Icon(Icons.Default.Map, contentDescription = "View on map")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RoadEyeBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        complaint?.let { c ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero image
                if (c.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = c.imageUrl,
                        contentDescription = "Road damage",
                        modifier = Modifier.fillMaxWidth().height(250.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(180.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🛣️", fontSize = 64.sp)
                    }
                }

                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Status & Severity row
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusBadge(status = c.status)
                        SeverityBadge(severity = c.severity)
                        if (!c.isSynced) {
                            Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(4.dp)) {
                                Text("Offline", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.error))
                            }
                        }
                    }

                    Text(c.title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                    Text(c.description, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(0.7f)))

                    // Timeline Progress
                    ComplaintTimeline(status = c.status)

                    // Info Cards Row
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        InfoChip(modifier = Modifier.weight(1f), icon = "📅", label = "Reported", value = formatDate(c.createdAt))
                        InfoChip(modifier = Modifier.weight(1f), icon = "🆔", label = "ID", value = c.id.take(8).uppercase())
                    }

                    // Location card
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = SeverityHigh, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Location", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                                Text(c.address.ifEmpty { "GPS: ${c.latitude}, ${c.longitude}" },
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(0.7f)))
                            }
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = { navController.navigate(Screen.MapView.createRoute(c.id)) }) {
                                Icon(Icons.Default.OpenInNew, contentDescription = "Open map", tint = RoadEyeBlue, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    // Officer notes (if assigned)
                    if (c.officerNotes.isNotEmpty()) {
                        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = RoadEyeSaffronContainer)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🏛️", fontSize = 18.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Officer Update", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF4A2800)))
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(c.officerNotes, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF795548)))
                                if (c.assignedOfficerName.isNotEmpty()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text("— ${c.assignedOfficerName}", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline))
                                }
                            }
                        }
                    }

                    // After image (if resolved)
                    if (c.afterImageUrl.isNotEmpty()) {
                        Column {
                            Text("✅ After Repair", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = StatusResolved))
                            Spacer(Modifier.height(8.dp))
                            AsyncImage(
                                model = c.afterImageUrl,
                                contentDescription = "After repair",
                                modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = RoadEyeBlue)
        }
    }
}

@Composable
fun ComplaintTimeline(status: ComplaintStatus) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Progress", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TimelineStep("Submitted", ComplaintStatus.SUBMITTED, status, "📤")
                TimelineLine(active = status != ComplaintStatus.SUBMITTED)
                TimelineStep("In Progress", ComplaintStatus.IN_PROGRESS, status, "🔧")
                TimelineLine(active = status == ComplaintStatus.RESOLVED)
                TimelineStep("Resolved", ComplaintStatus.RESOLVED, status, "✅")
            }
        }
    }
}

@Composable
fun RowScope.TimelineStep(label: String, step: ComplaintStatus, current: ComplaintStatus, emoji: String) {
    val isActive = current.ordinal >= step.ordinal
    val isCurrentStep = current == step
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
        Box(
            modifier = Modifier.size(40.dp).background(
                if (isActive) when (step) {
                    ComplaintStatus.SUBMITTED -> StatusSubmitted
                    ComplaintStatus.IN_PROGRESS -> StatusInProgress
                    ComplaintStatus.RESOLVED -> StatusResolved
                }.copy(if (isCurrentStep) 1f else 0.4f)
                else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(50)
            ),
            contentAlignment = Alignment.Center
        ) { Text(emoji, fontSize = 18.sp) }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
        ), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
fun RowScope.TimelineLine(active: Boolean) {
    Divider(
        modifier = Modifier.weight(0.5f).padding(bottom = 20.dp),
        color = if (active) StatusResolved else MaterialTheme.colorScheme.outline.copy(0.3f),
        thickness = 2.dp
    )
}

@Composable
fun InfoChip(modifier: Modifier = Modifier, icon: String, label: String, value: String) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("$icon $label", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        }
    }
}
