package com.roadeye.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.roadeye.ui.navigation.Screen
import com.roadeye.ui.theme.RoadEyeBlue
import com.roadeye.ui.theme.RoadEyeBlueDark
import com.roadeye.ui.theme.RoadEyeSaffron
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.5f) }
    val taglineAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Logo animation
        alphaAnim.animateTo(1f, animationSpec = tween(800))
        scaleAnim.animateTo(1f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ))
        delay(300)
        taglineAlpha.animateTo(1f, animationSpec = tween(600))
        delay(1500)

        // Navigate based on auth state
        val destination = viewModel.getStartDestination()
        navController.navigate(destination) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(RoadEyeBlueDark, RoadEyeBlue, Color(0xFF2B5797))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon - Road with Eye symbol
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scaleAnim.value)
                    .alpha(alphaAnim.value)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(RoadEyeSaffron, Color(0xFFE65100))
                        ),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🛣️",
                    fontSize = 52.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "RoadEye",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 2.sp
                ),
                modifier = Modifier.alpha(alphaAnim.value)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "రోడ్ ఐ • Road Safety Reporter",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White.copy(alpha = 0.8f)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(taglineAlpha.value)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Government of Andhra Pradesh",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = RoadEyeSaffron.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.alpha(taglineAlpha.value)
            )
        }

        // Bottom branding
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(taglineAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoadingDots()
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Empowering Citizens • Improving Roads",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
fun LoadingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { index ->
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 200)
                ),
                label = "dot_$index"
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(alpha)
                    .background(Color.White, shape = androidx.compose.foundation.shape.CircleShape)
            )
        }
    }
}
