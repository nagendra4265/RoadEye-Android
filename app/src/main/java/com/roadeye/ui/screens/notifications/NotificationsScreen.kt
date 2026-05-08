package com.roadeye.ui.screens.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.roadeye.domain.model.Notification
import com.roadeye.domain.model.NotificationType
import com.roadeye.domain.repository.AuthRepository
import com.roadeye.domain.repository.NotificationRepository
import com.roadeye.ui.theme.RoadEyeBlue
import com.roadeye.ui.theme.RoadEyeSaffron
import com.roadeye.ui.theme.StatusResolved
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                actions = {
                    TextButton(onClick = viewModel::markAllRead) { Text("Mark All Read", color = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RoadEyeBlue, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔔", fontSize = 56.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("No notifications yet", style = MaterialTheme.typography.titleMedium)
                    Text("నోటిఫికేషన్‌లు లేవు", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline))
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(notifications, key = { it.id }) { notification ->
                    NotificationCard(notification = notification)
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: Notification) {
    val (emoji, color) = when (notification.type) {
        NotificationType.STATUS_UPDATE -> Pair("🔄", RoadEyeBlue)
        NotificationType.RESOLVED -> Pair("✅", StatusResolved)
        NotificationType.OFFICER_ASSIGNED -> Pair("👷", RoadEyeSaffron)
        NotificationType.NEW_COMPLAINT -> Pair("📋", MaterialTheme.colorScheme.primary)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) MaterialTheme.colorScheme.surface
            else color.copy(0.05f)
        ),
        border = if (!notification.isRead) androidx.compose.foundation.BorderStroke(1.dp, color.copy(0.3f)) else null,
        elevation = CardDefaults.cardElevation(if (notification.isRead) 1.dp else 3.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Surface(color = color.copy(0.15f), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = 20.sp) }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(notification.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold))
                Spacer(Modifier.height(4.dp))
                Text(notification.body, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(0.7f)))
            }
            if (!notification.isRead) {
                Box(modifier = Modifier.size(8.dp).offset(y = 4.dp).align(Alignment.Top), contentAlignment = Alignment.Center) {
                    Surface(color = color, shape = RoundedCornerShape(50), modifier = Modifier.size(8.dp)) {}
                }
            }
        }
    }
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // Demo notifications for presentation
    val notifications: StateFlow<List<Notification>> = MutableStateFlow(
        listOf(
            Notification(id = "1", title = "Complaint Update", body = "Your complaint #A1B2C3 has been assigned to an officer.", type = NotificationType.OFFICER_ASSIGNED, isRead = false),
            Notification(id = "2", title = "Work in Progress", body = "Repair work has started on the pothole at MG Road.", type = NotificationType.STATUS_UPDATE, isRead = false),
            Notification(id = "3", title = "Complaint Resolved! ✅", body = "The road damage at Benz Circle has been fixed. Thank you for reporting!", type = NotificationType.RESOLVED, isRead = true)
        )
    ).asStateFlow()

    fun markAllRead() {
        viewModelScope.launch {
            authRepository.currentUserId?.let {
                notificationRepository.markAllNotificationsRead(it)
            }
        }
    }
}
