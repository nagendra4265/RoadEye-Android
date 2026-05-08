package com.roadeye.ui.screens.complaint

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.roadeye.domain.model.Complaint
import com.roadeye.domain.model.ComplaintStatus
import com.roadeye.domain.repository.AuthRepository
import com.roadeye.domain.repository.ComplaintRepository
import com.roadeye.ui.components.ComplaintCard
import com.roadeye.ui.navigation.Screen
import com.roadeye.ui.theme.RoadEyeBlue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintTrackingScreen(
    navController: NavController,
    viewModel: ComplaintTrackingViewModel = hiltViewModel()
) {
    val complaints by viewModel.filteredComplaints.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Track Complaints", fontWeight = FontWeight.Bold)
                        Text("ఫిర్యాదులను ట్రాక్ చేయండి", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onPrimary.copy(0.8f)))
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Filter chips
            ScrollableTabRow(
                selectedTabIndex = when (selectedFilter) {
                    null -> 0
                    ComplaintStatus.SUBMITTED -> 1
                    ComplaintStatus.IN_PROGRESS -> 2
                    ComplaintStatus.RESOLVED -> 3
                },
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                divider = {}
            ) {
                Tab(selected = selectedFilter == null, onClick = { viewModel.setFilter(null) }, text = { Text("All") })
                Tab(selected = selectedFilter == ComplaintStatus.SUBMITTED, onClick = { viewModel.setFilter(ComplaintStatus.SUBMITTED) }, text = { Text("Submitted") })
                Tab(selected = selectedFilter == ComplaintStatus.IN_PROGRESS, onClick = { viewModel.setFilter(ComplaintStatus.IN_PROGRESS) }, text = { Text("In Progress") })
                Tab(selected = selectedFilter == ComplaintStatus.RESOLVED, onClick = { viewModel.setFilter(ComplaintStatus.RESOLVED) }, text = { Text("Resolved") })
            }

            if (complaints.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", style = MaterialTheme.typography.displayMedium)
                        Spacer(Modifier.height(12.dp))
                        Text("No complaints found", style = MaterialTheme.typography.titleMedium)
                        Text("for selected filter", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline))
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(complaints, key = { it.id }) { complaint ->
                        ComplaintCard(
                            complaint = complaint,
                            onClick = { navController.navigate(Screen.ComplaintDetail.createRoute(complaint.id)) }
                        )
                    }
                }
            }
        }
    }
}

@HiltViewModel
class ComplaintTrackingViewModel @Inject constructor(
    private val complaintRepository: ComplaintRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow<ComplaintStatus?>(null)
    val selectedFilter: StateFlow<ComplaintStatus?> = _selectedFilter.asStateFlow()

    private val _allComplaints = MutableStateFlow<List<Complaint>>(emptyList())

    val filteredComplaints: StateFlow<List<Complaint>> = combine(_allComplaints, _selectedFilter) { list, filter ->
        if (filter == null) list else list.filter { it.status == filter }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            complaintRepository.getUserComplaints(authRepository.currentUserId ?: "demo_user")
                .collect { _allComplaints.value = it }
        }
    }

    fun setFilter(filter: ComplaintStatus?) { _selectedFilter.value = filter }
}
