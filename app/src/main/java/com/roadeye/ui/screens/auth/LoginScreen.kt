package com.roadeye.ui.screens.auth

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.roadeye.ui.navigation.Screen
import com.roadeye.ui.theme.*

@Composable
fun LoginScreen(
    navController: NavController,
    role: String,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val isCitizen = role == "CITIZEN"
    val uiState by viewModel.uiState.collectAsState()

    // Clear old errors when screen opens
    LaunchedEffect(Unit) { viewModel.clearError() }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Top gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(Brush.verticalGradient(listOf(RoadEyeBlueDark, RoadEyeBlue)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = { navController.navigateUp() }, modifier = Modifier.align(Alignment.Start)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(Modifier.height(8.dp))
                Text(if (isCitizen) "👤" else "🏛️", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    if (isCitizen) "Citizen Login" else "Officer Login",
                    style = MaterialTheme.typography.headlineSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
                Text(
                    if (isCitizen) "పౌరుడు లాగిన్" else "అధికారి లాగిన్",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(0.7f))
                )
            }

            // Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    if (isCitizen) {
                        CitizenLoginForm(viewModel = viewModel, uiState = uiState, navController = navController)
                    } else {
                        OfficerLoginForm(viewModel = viewModel, uiState = uiState, navController = navController)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Error snackbar
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(10.dp))
                        Text(error, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Security badge
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = RoadEyeBlueContainer)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = RoadEyeBlue, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Secure OTP login via Firebase. Your data is encrypted and protected.",
                        style = MaterialTheme.typography.bodySmall.copy(color = RoadEyeBlueDark)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Citizen OTP Form
// ─────────────────────────────────────────────────────────────
@Composable
fun CitizenLoginForm(
    viewModel: AuthViewModel,
    uiState: AuthUiState,
    navController: NavController
) {
    var phone by remember { mutableStateOf("") }
    val context = LocalContext.current

    Text("Mobile Number", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
    Text("మొబైల్ నంబర్", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline))
    Spacer(Modifier.height(16.dp))

    OutlinedTextField(
        value = phone,
        onValueChange = { if (it.length <= 10) phone = it.filter { c -> c.isDigit() } },
        label = { Text("10-digit Mobile Number") },
        placeholder = { Text("9876543210") },
        leadingIcon = {
            Row(
                modifier = Modifier.padding(start = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🇮🇳", fontSize = 18.sp)
                Spacer(Modifier.width(4.dp))
                Text("+91", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                Spacer(Modifier.width(8.dp))
                Divider(modifier = Modifier.height(24.dp).width(1.dp))
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        isError = uiState.error != null,
        supportingText = {
            if (phone.isNotEmpty() && phone.length < 10) {
                Text("Enter 10 digits", color = MaterialTheme.colorScheme.error)
            }
        }
    )

    Spacer(Modifier.height(6.dp))
    Text(
        "Firebase will send a 6-digit OTP to +91$phone",
        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
    )

    Spacer(Modifier.height(24.dp))

    Button(
        onClick = {
            val fullPhone = "+91$phone"
            val activity = context as Activity
            viewModel.sendOtp(
                phoneNumber = fullPhone,
                activity = activity,
                onCodeSent = { verificationId ->
                    navController.navigate(
                        Screen.OtpVerification.createRoute(fullPhone, verificationId)
                    )
                },
                onAutoVerified = {
                    // Device auto-verified (test numbers / instant verification)
                    navController.navigate(Screen.CitizenDashboard.route) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = false }
                    }
                }
            )
        },
        modifier = Modifier.fillMaxWidth().height(52.dp),
        enabled = phone.length == 10 && !uiState.isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RoadEyeBlue)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            Spacer(Modifier.width(10.dp))
            Text("Sending OTP...", color = Color.White)
        } else {
            Icon(Icons.Default.Sms, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Send OTP", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }

    Spacer(Modifier.height(12.dp))
    Text(
        "⚠️ Make sure Phone Authentication is enabled in your Firebase Console → Authentication → Sign-in method",
        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline),
    )
}

// ─────────────────────────────────────────────────────────────
// Officer Email/Password Form
// ─────────────────────────────────────────────────────────────
@Composable
fun OfficerLoginForm(
    viewModel: AuthViewModel,
    uiState: AuthUiState,
    navController: NavController
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Text("Officer Credentials", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
    Text("అధికారి ఆధారాలు", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline))
    Spacer(Modifier.height(16.dp))

    OutlinedTextField(
        value = email,
        onValueChange = { email = it.trim() },
        label = { Text("Government Email ID") },
        placeholder = { Text("officer@ap.gov.in") },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        isError = uiState.error != null
    )

    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Password") },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = { showPassword = !showPassword }) {
                Icon(
                    if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null
                )
            }
        },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        isError = uiState.error != null
    )

    Spacer(Modifier.height(24.dp))

    Button(
        onClick = {
            viewModel.loginOfficer(email, password) {
                navController.navigate(Screen.OfficerDashboard.route) {
                    popUpTo(Screen.RoleSelection.route) { inclusive = false }
                }
            }
        },
        modifier = Modifier.fillMaxWidth().height(52.dp),
        enabled = email.isNotBlank() && password.isNotBlank() && !uiState.isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RoadEyeSaffron)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            Spacer(Modifier.width(10.dp))
            Text("Signing in...", color = Color.White)
        } else {
            Icon(Icons.Default.Badge, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Login as Officer", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
