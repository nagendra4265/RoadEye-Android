package com.roadeye.ui.screens.officer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.roadeye.domain.model.*
import com.roadeye.domain.repository.ComplaintRepository
import com.roadeye.domain.repository.UserRepository
import com.roadeye.ui.components.ComplaintCard
import com.roadeye.ui.components.StatCard
import com.roadeye.ui.navigation.Screen
import com.roadeye.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficerDashboardScreen(
    navController: NavController,
    viewModel: OfficerDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val complaints by viewModel.filteredComplaints.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.Dashboard, null) }, label = { Text("Dashboard") })
                NavigationBarItem(selected = false, onClick = { navController.navigate(Screen.MapView.createRoute("all")) }, icon = { Icon(Icons.Default.Map, null) }, label = { Text("Map") })
                NavigationBarItem(selected = false, onClick = { navController.navigate(Screen.Notifications.route) }, icon = { Icon(Icons.Default.Notifications, null) }, label = { Text("Alerts") })
                NavigationBarItem(selected = false, onClick = { navController.navigate(Screen.Profile.route) }, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 80.dp)) {
            // Officer Header
            item {
                Box(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color(0xFF1A237E), RoadEyeBlue))).padding(20.dp)) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Officer Dashboard", style = MaterialTheme.typography.headlineSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                                Text("అధికారి డాష్‌బోర్డ్", style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(0.7f)))
                                Spacer(Modifier.height(4.dp))
                                Surface(color = RoadEyeSaffron.copy(0.2f), shape = RoundedCornerShape(20.dp)) {
                                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF4CAF50), androidx.compose.foundation.shape.CircleShape))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Active Officer", style = MaterialTheme.typography.labelSmall.copy(color = RoadEyeSaffron, fontWeight = FontWeight.SemiBold))
                                    }
                                }
                            }
                            IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Stats
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OfficerStatChip(modifier = Modifier.weight(1f), value = uiState.stats.totalComplaints.toString(), label = "Total", color = RoadEyeBlueLight)
                            OfficerStatChip(modifier = Modifier.weight(1f), value = uiState.stats.submittedCount.toString(), label = "Pending", color = StatusSubmitted)
                            OfficerStatChip(modifier = Modifier.weight(1f), value = uiState.stats.inProgressCount.toString(), label = "Active", color = StatusInProgress)
                            OfficerStatChip(modifier = Modifier.weight(1f), value = uiState.stats.resolvedCount.toString(), label = "Done", color = StatusResolved)
                        }
                    }
                }
            }

            // Priority Alerts
            if (uiState.stats.highSeverityCount > 0) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = SeverityHigh.copy(0.08f)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, SeverityHigh.copy(0.3f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🚨", fontSize = 24.sp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("${uiState.stats.highSeverityCount} HIGH SEVERITY complaints require immediate attention!", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = SeverityHigh))
                                Text("అత్యవసర ఫిర్యాదులు ఉన్నాయి", style = MaterialTheme.typography.bodySmall.copy(color = SeverityHigh.copy(0.7f)))
                            }
                        }
                    }
                }
            }

            // Filter tabs
            item {
                Spacer(Modifier.height(16.dp))
                Text("Complaints", modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(8.dp))
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    divider = {}
                ) {
                    listOf("All", "Pending", "In Progress", "Resolved").forEachIndexed { index, label ->
                        Tab(selected = selectedTab == index, onClick = { viewModel.selectTab(index) }, text = { Text(label) })
                    }
                }
            }

            // Complaint cards
            items(complaints, key = { it.id }) { complaint ->
                ComplaintCard(
                    complaint = complaint,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    onClick = { navController.navigate(Screen.OfficerComplaintDetail.createRoute(complaint.id)) }
                )
            }

            if (complaints.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("✅", fontSize = 48.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("No complaints in this category", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.outline))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OfficerStatChip(modifier: Modifier = Modifier, value: String, label: String, color: Color) {
    Surface(modifier = modifier, color = color.copy(0.15f), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge.copy(color = color, fontWeight = FontWeight.ExtraBold))
            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(0.8f)))
        }
    }
}

data class OfficerDashboardUiState(
    val isLoading: Boolean = false,
    val stats: DashboardStats = DashboardStats()
)

@HiltViewModel
class OfficerDashboardViewModel @Inject constructor(
    private val complaintRepository: ComplaintRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OfficerDashboardUiState())
    val uiState: StateFlow<OfficerDashboardUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _allComplaints = MutableStateFlow<List<Complaint>>(emptyList())

    val filteredComplaints: StateFlow<List<Complaint>> = combine(_allComplaints, _selectedTab) { list, tab ->
        when (tab) {
            1 -> list.filter { it.status == ComplaintStatus.SUBMITTED }
            2 -> list.filter { it.status == ComplaintStatus.IN_PROGRESS }
            3 -> list.filter { it.status == ComplaintStatus.RESOLVED }
            else -> list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            complaintRepository.getComplaints().collect { list ->
                _allComplaints.value = list
                val stats = DashboardStats(
                    totalComplaints = list.size,
                    submittedCount = list.count { it.status == ComplaintStatus.SUBMITTED },
                    inProgressCount = list.count { it.status == ComplaintStatus.IN_PROGRESS },
                    resolvedCount = list.count { it.status == ComplaintStatus.RESOLVED },
                    highSeverityCount = list.count { it.severity == ComplaintSeverity.HIGH }
                )
                _uiState.update { it.copy(stats = stats) }
            }
        }
    }

    fun selectTab(tab: Int) { _selectedTab.value = tab }
}
