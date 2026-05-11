package com.roadeye.ui.screens.auth

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.roadeye.ui.navigation.Screen
import com.roadeye.ui.theme.RoadEyeBlue
import com.roadeye.ui.theme.RoadEyeBlueDark
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
    var countdown by remember { mutableIntStateOf(60) }
    var currentVerificationId by remember { mutableStateOf(verificationId) }
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Start countdown timer
    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000L)
            countdown--
        }
    }

    // Auto-navigate on success (handles auto-verification too)
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            navController.navigate(Screen.CitizenDashboard.route) {
                popUpTo(Screen.RoleSelection.route) { inclusive = false }
            }
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
                Spacer(Modifier.height(8.dp))
                Text(
                    "Verify OTP",
                    style = MaterialTheme.typography.headlineSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
                Text(
                    "OTP పరిశీలన",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(0.7f))
                )
                Spacer(Modifier.height(8.dp))
                Surface(color = Color.White.copy(0.15f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        "OTP sent to $phone",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Enter 6-digit OTP",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Check your SMS inbox for the verification code",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(36.dp))

            // OTP boxes
            OtpInputField(
                otpValue = otpValue,
                onOtpChange = {
                    if (it.length <= 6) {
                        otpValue = it.filter { c -> c.isDigit() }
                    }
                },
                hasError = uiState.error != null
            )

            Spacer(Modifier.height(12.dp))

            // Error message
            uiState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(error, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Resend timer / button
            if (countdown > 0) {
                Text(
                    "Resend OTP in ${countdown}s",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.outline)
                )
            } else {
                TextButton(
                    onClick = {
                        countdown = 60
                        otpValue = ""
                        // Reset countdown
                        scope.launch {
                            while (countdown > 0) { delay(1000L); countdown-- }
                        }
                        // Re-trigger OTP
                        viewModel.sendOtp(
                            phoneNumber = phone,
                            activity = context as Activity,
                            onCodeSent = { newVerificationId ->
                                currentVerificationId = newVerificationId
                            },
                            onAutoVerified = {
                                navController.navigate(Screen.CitizenDashboard.route) {
                                    popUpTo(Screen.RoleSelection.route) { inclusive = false }
                                }
                            }
                        )
                    }
                ) {
                    Text("Resend OTP", color = RoadEyeBlue, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(32.dp))

            // Verify button
            Button(
                onClick = {
                    viewModel.verifyOtp(
                        verificationId = currentVerificationId,
                        otp = otpValue,
                        onSuccess = {
                            navController.navigate(Screen.CitizenDashboard.route) {
                                popUpTo(Screen.RoleSelection.route) { inclusive = false }
                            }
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = otpValue.length == 6 && !uiState.isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoadEyeBlue)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Verifying...", color = Color.White)
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Verify & Login", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Help card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("💡 Not receiving OTP?", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    Text("• Check if Phone Auth is enabled in Firebase Console", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline))
                    Text("• Add your number to Firebase test numbers for testing", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline))
                    Text("• Ensure SHA-1 fingerprint is added to Firebase project", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline))
                    Text("• Make sure google-services.json is the latest version", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline))
                }
            }
        }
    }
}

@Composable
fun OtpInputField(
    otpValue: String,
    onOtpChange: (String) -> Unit,
    hasError: Boolean = false
) {
    BasicTextField(
        value = otpValue,
        onValueChange = onOtpChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(6) { index ->
                    val char = if (index < otpValue.length) otpValue[index].toString() else ""
                    val isCurrent = index == otpValue.length

                    Box(
                        modifier = Modifier
                            .size(48.dp, 56.dp)
                            .background(
                                when {
                                    hasError && char.isNotEmpty() -> MaterialTheme.colorScheme.errorContainer
                                    char.isNotEmpty() -> RoadEyeBlue.copy(0.08f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = if (isCurrent) 2.dp else 1.dp,
                                color = when {
                                    hasError -> MaterialTheme.colorScheme.error
                                    isCurrent -> RoadEyeBlue
                                    char.isNotEmpty() -> RoadEyeBlue.copy(0.5f)
                                    else -> MaterialTheme.colorScheme.outline.copy(0.3f)
                                },
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (hasError) MaterialTheme.colorScheme.error else RoadEyeBlue,
                                textAlign = TextAlign.Center
                            )
                        )
                        // Cursor blink indicator
                        if (isCurrent && char.isEmpty()) {
                            Box(modifier = Modifier.size(2.dp, 24.dp).background(RoadEyeBlue))
                        }
                    }
                }
            }
        }
    )
}
