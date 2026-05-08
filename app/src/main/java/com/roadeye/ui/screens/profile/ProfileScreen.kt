package com.roadeye.ui.screens.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.roadeye.domain.repository.AuthRepository
import com.roadeye.ui.navigation.Screen
import com.roadeye.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(RoadEyeBlueDark, RoadEyeBlue))).padding(24.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { navController.navigateUp() }, modifier = Modifier.align(Alignment.Start)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier.size(90.dp).clip(CircleShape).background(RoadEyeSaffron),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👤", fontSize = 40.sp)
                }

                Spacer(Modifier.height(12.dp))
                Text("Ravi Kumar", style = MaterialTheme.typography.headlineSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                Text("+91 98765 43210", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(0.7f)))
                Spacer(Modifier.height(8.dp))
                Surface(color = RoadEyeSaffron.copy(0.2f), shape = RoundedCornerShape(20.dp)) {
                    Text("Citizen • Vijayawada", modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(color = RoadEyeSaffron, fontWeight = FontWeight.SemiBold))
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Stats
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileStatCard(modifier = Modifier.weight(1f), value = "12", label = "Reported", color = RoadEyeBlue)
                ProfileStatCard(modifier = Modifier.weight(1f), value = "8", label = "Resolved", color = StatusResolved)
                ProfileStatCard(modifier = Modifier.weight(1f), value = "2", label = "Pending", color = StatusInProgress)
            }

            Spacer(Modifier.height(8.dp))

            // Menu items
            Text("Account", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline))

            ProfileMenuItem(icon = Icons.Default.Person, title = "Edit Profile", subtitle = "Update your name and details") {}
            ProfileMenuItem(icon = Icons.Default.Notifications, title = "Notification Settings", subtitle = "Manage your alerts") {}
            ProfileMenuItem(icon = Icons.Default.Language, title = "Language", subtitle = "English / తెలుగు") {}
            ProfileMenuItem(icon = Icons.Default.DarkMode, title = "Dark Mode", subtitle = "Toggle appearance") {}

            Spacer(Modifier.height(8.dp))
            Text("Support", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline))

            ProfileMenuItem(icon = Icons.Default.Help, title = "Help & FAQ", subtitle = "How to use RoadEye") {}
            ProfileMenuItem(icon = Icons.Default.Policy, title = "Privacy Policy", subtitle = "Data protection details") {}
            ProfileMenuItem(icon = Icons.Default.Info, title = "About", subtitle = "RoadEye v1.0.0 • AP Government") {}

            Spacer(Modifier.height(8.dp))

            // Logout
            Button(
                onClick = {
                    viewModel.logout()
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(8.dp))
                Text("Logout", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "RoadEye v1.0.0 • Government of Andhra Pradesh\n© 2024 R&B Department",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ProfileStatCard(modifier: Modifier = Modifier, value: String, label: String, color: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = color.copy(0.1f))) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineMedium.copy(color = color, fontWeight = FontWeight.ExtraBold))
            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline))
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = RoadEyeBlue.copy(0.1f), shape = RoundedCornerShape(10.dp), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = RoadEyeBlue, modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium))
                Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    fun logout() { viewModelScope.launch { authRepository.logout() } }
}
