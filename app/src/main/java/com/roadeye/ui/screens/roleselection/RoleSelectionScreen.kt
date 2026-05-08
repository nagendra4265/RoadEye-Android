package com.roadeye.ui.screens.roleselection

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.roadeye.ui.navigation.Screen
import com.roadeye.ui.theme.*

@Composable
fun RoleSelectionScreen(
    navController: NavController,
    viewModel: RoleSelectionViewModel = hiltViewModel()
) {
    var selectedRole by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        RoadEyeBlueDark,
                        RoadEyeBlue,
                        Color(0xFF1E4DB7)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(72.dp))

            // Header
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                RoadEyeSaffron.copy(alpha = 0.2f),
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                            .border(
                                2.dp,
                                RoadEyeSaffron,
                                shape = androidx.compose.foundation.shape.CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🛣️", fontSize = 38.sp)
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "Welcome to RoadEye",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "రోడ్ ఐకి స్వాగతం",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White.copy(alpha = 0.7f)
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(12.dp))

                    Surface(
                        color = RoadEyeSaffron.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "Government of Andhra Pradesh",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = RoadEyeSaffron,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(56.dp))

            Text(
                text = "Select Your Role",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.SemiBold
                )
            )

            Text(
                text = "మీ పాత్రను ఎంచుకోండి",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.6f)
                )
            )

            Spacer(Modifier.height(32.dp))

            // Role Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RoleCard(
                    modifier = Modifier.weight(1f),
                    title = "Citizen",
                    teluguTitle = "పౌరుడు",
                    description = "Report road damage & track complaints",
                    icon = Icons.Default.Person,
                    isSelected = selectedRole == "CITIZEN",
                    color = Color(0xFF42A5F5),
                    emoji = "👤"
                ) {
                    selectedRole = "CITIZEN"
                }

                RoleCard(
                    modifier = Modifier.weight(1f),
                    title = "Officer",
                    teluguTitle = "అధికారి",
                    description = "Manage & resolve road complaints",
                    icon = Icons.Default.Badge,
                    isSelected = selectedRole == "OFFICER",
                    color = Color(0xFFFFB300),
                    emoji = "🏛️"
                ) {
                    selectedRole = "OFFICER"
                }
            }

            Spacer(Modifier.height(40.dp))

            // Proceed Button
            AnimatedVisibility(
                visible = selectedRole != null,
                enter = fadeIn() + expandVertically()
            ) {
                Button(
                    onClick = {
                        viewModel.saveRole(selectedRole!!)
                        navController.navigate(Screen.Login.createRoute(selectedRole!!))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoadEyeSaffron
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Continue as ${if (selectedRole == "CITIZEN") "Citizen" else "Officer"}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                }
            }
        }

        // Bottom watermark
        Text(
            text = "v1.0.0 • Secure Government App",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun RoleCard(
    modifier: Modifier = Modifier,
    title: String,
    teluguTitle: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    color: Color,
    emoji: String,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Box(
        modifier = modifier
            .aspectRatio(0.85f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isSelected) 16.dp else 4.dp,
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected)
                    Brush.verticalGradient(listOf(color.copy(alpha = 0.25f), color.copy(alpha = 0.1f)))
                else
                    Brush.verticalGradient(listOf(Color.White.copy(0.1f), Color.White.copy(0.05f)))
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) color else Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 40.sp)

            Spacer(Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = if (isSelected) color else Color.White,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = teluguTitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (isSelected) color.copy(0.8f) else Color.White.copy(0.6f)
                )
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                ),
                textAlign = TextAlign.Center
            )

            if (isSelected) {
                Spacer(Modifier.height(12.dp))
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

