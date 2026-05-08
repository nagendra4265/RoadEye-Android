package com.roadeye.ui.screens.auth

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(RoadEyeBlueDark, RoadEyeBlue)
                    )
                )
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
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = if (isCitizen) "👤" else "🏛️",
                    fontSize = 48.sp
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = if (isCitizen) "Citizen Login" else "Officer Login",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = if (isCitizen) "పౌరుడు లాగిన్" else "అధికారి లాగిన్",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(0.7f)
                    )
                )
            }

            // Login Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    if (isCitizen) {
                        CitizenLoginForm(
                            viewModel = viewModel,
                            uiState = uiState,
                            navController = navController
                        )
                    } else {
                        OfficerLoginForm(
                            viewModel = viewModel,
                            uiState = uiState,
                            navController = navController
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Info section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = RoadEyeBlueContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = RoadEyeBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Your data is secure and encrypted. This is an official Government of AP application.",
                        style = MaterialTheme.typography.bodySmall.copy(color = RoadEyeBlueDark),
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // Error Snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar
        }
    }
}

@Composable
fun CitizenLoginForm(
    viewModel: AuthViewModel,
    uiState: AuthUiState,
    navController: NavController
) {
    var phone by remember { mutableStateOf("") }

    Text(
        text = "Mobile Number",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
    )
    Text(
        text = "మొబైల్ నంబర్",
        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
    )

    Spacer(Modifier.height(16.dp))

    OutlinedTextField(
        value = phone,
        onValueChange = { if (it.length <= 10) phone = it.filter { c -> c.isDigit() } },
        label = { Text("Phone Number") },
        placeholder = { Text("+91 XXXXX XXXXX") },
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
        singleLine = true
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = "OTP will be sent to this number for verification",
        style = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.outline
        )
    )

    Spacer(Modifier.height(24.dp))

    Button(
        onClick = {
            viewModel.sendOtp("+91$phone") { verificationId ->
                navController.navigate(
                    Screen.OtpVerification.createRoute("+91$phone", verificationId)
                )
            }
        },
        modifier = Modifier.fillMaxWidth().height(52.dp),
        enabled = phone.length == 10 && !uiState.isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RoadEyeBlue)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                "Send OTP",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }

    Spacer(Modifier.height(16.dp))

    // Demo bypass for testing
    TextButton(
        onClick = { navController.navigate(Screen.CitizenDashboard.route) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Demo: Skip Login (Testing Only)",
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Composable
fun OfficerLoginForm(
    viewModel: AuthViewModel,
    uiState: AuthUiState,
    navController: NavController
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Text(
        text = "Officer Credentials",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
    )
    Text(
        text = "అధికారి ఆధారాలు",
        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
    )

    Spacer(Modifier.height(16.dp))

    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Government Email ID") },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
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
        singleLine = true
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
        enabled = email.isNotEmpty() && password.isNotEmpty() && !uiState.isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RoadEyeSaffron)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text("Login as Officer", style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
        }
    }

    Spacer(Modifier.height(16.dp))

    // Demo bypass
    TextButton(
        onClick = { navController.navigate(Screen.OfficerDashboard.route) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Demo: Skip Login (Testing Only)",
            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.outline)
        )
    }
}
