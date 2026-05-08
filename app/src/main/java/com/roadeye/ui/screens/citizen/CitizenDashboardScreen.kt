package com.roadeye.ui.screens.citizen

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.roadeye.domain.model.*
import com.roadeye.ui.components.ComplaintCard
import com.roadeye.ui.components.RoadHealthMeter
import com.roadeye.ui.components.StatCard
import com.roadeye.ui.navigation.Screen
import com.roadeye.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenDashboardScreen(
    navController: NavController,
    viewModel: CitizenDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val complaints by viewModel.complaints.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.ComplaintCapture.route) },
                containerColor = RoadEyeSaffron,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.AddAPhoto, contentDescription = null) },
                text = { Text("Report Damage", fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            CitizenBottomNav(navController = navController, currentRoute = Screen.CitizenDashboard.route)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header
            item {
                CitizenDashboardHeader(
                    navController = navController,
                    userName = uiState.userName
                )
            }

            // Stats Row
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Total",
                        value = uiState.stats.totalComplaints.toString(),
                        icon = Icons.Default.Report,
                        color = RoadEyeBlue
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "In Progress",
                        value = uiState.stats.inProgressCount.toString(),
                        icon = Icons.Default.Engineering,
                        color = StatusInProgress
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Resolved",
                        value = uiState.stats.resolvedCount.toString(),
                        icon = Icons.Default.CheckCircle,
                        color = StatusResolved
                    )
                }
            }

            // Road Health Meter
            item {
                Spacer(Modifier.height(16.dp))
                RoadHealthMeter(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    score = uiState.roadHealthScore,
                    district = uiState.district
                )
            }

            // Quick Actions
            item {
                Spacer(Modifier.height(16.dp))
                QuickActions(navController = navController)
            }

            // Recent Complaints Header
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "My Complaints",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "నా ఫిర్యాదులు",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                        )
                    }
                    TextButton(onClick = { navController.navigate(Screen.ComplaintTracking.route) }) {
                        Text("View All", color = RoadEyeBlue)
                    }
                }
            }

            // Complaints List
            if (complaints.isEmpty()) {
                item {
                    EmptyComplaintsPlaceholder(
                        onReportClick = { navController.navigate(Screen.ComplaintCapture.route) }
                    )
                }
            } else {
                items(complaints.take(5)) { complaint ->
                    ComplaintCard(
                        complaint = complaint,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        onClick = {
                            navController.navigate(Screen.ComplaintDetail.createRoute(complaint.id))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CitizenDashboardHeader(
    navController: NavController,
    userName: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(RoadEyeBlueDark, RoadEyeBlue)
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good Morning,",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(0.7f))
                    )
                    Text(
                        text = if (userName.isEmpty()) "Citizen" else userName,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "నమస్కారం • Namaskaram",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(0.6f))
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                        Box {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                            // Badge
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(RoadEyeSaffron, CircleShape)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // AP Government Banner
            Surface(
                color = Color.White.copy(0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🏛️", fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "Andhra Pradesh Road Maintenance",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            "R&B Department • ఆర్ & బి విభాగం",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(0.7f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActions(navController: NavController) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            "Quick Actions",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                icon = "📷",
                label = "Report\nDamage",
                color = RoadEyeBlue
            ) { navController.navigate(Screen.ComplaintCapture.route) }

            QuickActionButton(
                modifier = Modifier.weight(1f),
                icon = "📋",
                label = "Track\nStatus",
                color = StatusInProgress
            ) { navController.navigate(Screen.ComplaintTracking.route) }

            QuickActionButton(
                modifier = Modifier.weight(1f),
                icon = "🗺️",
                label = "Road\nMap",
                color = RoadEyeGreen
            ) { navController.navigate(Screen.MapView.createRoute("all")) }

            QuickActionButton(
                modifier = Modifier.weight(1f),
                icon = "🔔",
                label = "Notifi-\ncations",
                color = RoadEyeSaffron
            ) { navController.navigate(Screen.Notifications.route) }
        }
    }
}

@Composable
fun QuickActionButton(
    modifier: Modifier = Modifier,
    icon: String,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(0.1f)),
        border = BorderStroke(1.dp, color.copy(0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = color,
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun EmptyComplaintsPlaceholder(onReportClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🛣️", fontSize = 48.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                "No complaints yet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                "ఇంకా ఫిర్యాదులు లేవు",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "See a pothole or damaged road? Report it and help your community!",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onReportClick,
                colors = ButtonDefaults.buttonColors(containerColor = RoadEyeBlue)
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Report a Damage", color = Color.White)
            }
        }
    }
}

@Composable
fun CitizenBottomNav(navController: NavController, currentRoute: String) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == Screen.CitizenDashboard.route,
            onClick = { navController.navigate(Screen.CitizenDashboard.route) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.ComplaintTracking.route,
            onClick = { navController.navigate(Screen.ComplaintTracking.route) },
            icon = { Icon(Icons.Default.List, contentDescription = null) },
            label = { Text("Track") }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.MapView.route,
            onClick = { navController.navigate(Screen.MapView.createRoute("all")) },
            icon = { Icon(Icons.Default.Map, contentDescription = null) },
            label = { Text("Map") }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Profile.route,
            onClick = { navController.navigate(Screen.Profile.route) },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("Profile") }
        )
    }
}
