package com.roadeye.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import kotlinx.coroutines.launch

@Composable
fun OtpVerificationScreen(
    navController: NavController,
    phone: String,
    verificationId: String,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var otpValue by remember { mutableStateOf("") }
    var countdown by remember { mutableIntStateOf(30) }
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000)
            countdown--
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(RoadEyeBlueDark, RoadEyeBlue)))
                .padding(24.dp)
        ) {
            Column {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(Modifier.height(12.dp))
                Text("Verify OTP", style = MaterialTheme.typography.headlineSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                Text("OTP పరిశీలన", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(0.7f)))
                Spacer(Modifier.height(8.dp))
                Text("OTP sent to $phone", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(0.8f)))
            }
        }

        Spacer(Modifier.height(48.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Enter 6-digit OTP",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            Spacer(Modifier.height(32.dp))

            // OTP Input Fields
            OtpInputField(
                otpValue = otpValue,
                onOtpChange = { if (it.length <= 6) otpValue = it.filter { c -> c.isDigit() } }
            )

            Spacer(Modifier.height(32.dp))

            // Resend OTP
            if (countdown > 0) {
                Text(
                    "Resend OTP in ${countdown}s",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.outline)
                )
            } else {
                TextButton(onClick = {
                    viewModel.sendOtp(phone) {}
                    countdown = 30
                    scope.launch { while (countdown > 0) { delay(1000); countdown-- } }
                }) {
                    Text("Resend OTP", color = RoadEyeBlue, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.verifyOtp(verificationId, otpValue) {
                        navController.navigate(Screen.CitizenDashboard.route) {
                            popUpTo(Screen.RoleSelection.route) { inclusive = false }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = otpValue.length == 6 && !uiState.isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoadEyeBlue)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Verify & Login", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // Demo bypass
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = {
                navController.navigate(Screen.CitizenDashboard.route)
            }) {
                Text("Demo: Skip OTP", style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.outline))
            }
        }
    }
}

@Composable
fun OtpInputField(
    otpValue: String,
    onOtpChange: (String) -> Unit
) {
    BasicTextField(
        value = otpValue,
        onValueChange = onOtpChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(6) { index ->
                    val char = when {
                        index >= otpValue.length -> ""
                        else -> otpValue[index].toString()
                    }
                    val isFocused = index == otpValue.length

                    Box(
                        modifier = Modifier
                            .size(50.dp, 56.dp)
                            .background(
                                if (char.isNotEmpty()) RoadEyeBlue.copy(0.1f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = if (isFocused) 2.dp else 1.dp,
                                color = if (isFocused) RoadEyeBlue
                                else if (char.isNotEmpty()) RoadEyeBlue.copy(0.5f)
                                else MaterialTheme.colorScheme.outline.copy(0.4f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = RoadEyeBlue,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        }
    )
}
