package com.kutirakone.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kutirakone.app.R
import com.kutirakone.app.navigation.Screen
import com.kutirakone.app.model.enums.UserRole
import com.kutirakone.app.ui.common.utils.UiState
import com.kutirakone.app.ui.theme.KutiraAmber
import com.kutirakone.app.ui.theme.KutiraGreen
import com.kutirakone.app.viewmodel.AuthViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    val username by viewModel.username.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val name by viewModel.name.collectAsState()
    val village by viewModel.village.collectAsState()
    val selectedRole by viewModel.selectedRole.collectAsState()
    
    val isLoginMode by viewModel.isLoginMode.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()
    val userProfileState by viewModel.userProfileState.collectAsState()
    
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    LaunchedEffect(isUserLoggedIn, userProfileState) {
        if (isUserLoggedIn) {
            when (userProfileState) {
                is UiState.Success -> {
                    val role = (userProfileState as UiState.Success).data
                    if (role == UserRole.TAILOR || role == UserRole.BOTH) {
                        navController.navigate(Screen.VendorDashboard.route) { popUpTo(0) }
                    } else {
                        navController.navigate(Screen.CustomerDashboard.route) { popUpTo(0) }
                    }
                }
                is UiState.Error -> {
                    // Since role selection is deleted, if there's no profile, just do nothing
                }
                else -> { /* Loading or Idle, do nothing and wait for fetch to complete */ }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KutiraGreen),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            // Logo placeholder
            Text(
                text = "✂️ 🧵",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.app_name),
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isLoginMode) "Welcome Back" else "Create an Account",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            OutlinedTextField(
                value = username,
                onValueChange = { viewModel.updateUsername(it) },
                label = { Text("Username", color = Color.White) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KutiraAmber,
                    unfocusedBorderColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.updatePassword(it) },
                label = { Text("Password", color = Color.White) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KutiraAmber,
                    unfocusedBorderColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (!isLoginMode) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { viewModel.updateConfirmPassword(it) },
                    label = { Text("Confirm Password", color = Color.White) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KutiraAmber,
                        unfocusedBorderColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { viewModel.updatePhone(it) },
                    label = { Text("Phone Number", color = Color.White) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KutiraAmber,
                        unfocusedBorderColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text("Full Name", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KutiraAmber,
                        unfocusedBorderColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = village,
                    onValueChange = { viewModel.updateVillage(it) },
                    label = { Text("Village / Locality", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KutiraAmber,
                        unfocusedBorderColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val loc = com.kutirakone.app.utils.LocationUtils.getCurrentLocation(context)
                            if (loc != null) {
                                viewModel.updateSelectedLocation(com.google.firebase.firestore.GeoPoint(loc.first, loc.second))
                                val address = com.kutirakone.app.utils.LocationUtils.getAddressFromCoordinates(context, loc.first, loc.second)
                                viewModel.updateVillage(address)
                            } else {
                                android.widget.Toast.makeText(context, "Could not fetch GPS location. Make sure GPS is enabled and permissions are granted.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KutiraAmber),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📍 Pin Exact Shop/Home Location", color = Color.Black)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Select your role",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                RoleCard(
                    title = stringResource(R.string.label_tailor),
                    subtitle = "I have fabric scraps to sell or swap",
                    icon = "✂️",
                    isSelected = selectedRole == UserRole.TAILOR,
                    onClick = { viewModel.updateRole(UserRole.TAILOR) }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                RoleCard(
                    title = stringResource(R.string.label_artisan),
                    subtitle = "I need fabric pieces for my crafts",
                    icon = "🎨",
                    isSelected = selectedRole == UserRole.ARTISAN,
                    onClick = { viewModel.updateRole(UserRole.ARTISAN) }
                )

                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(onClick = { viewModel.updateRole(UserRole.BOTH) }) {
                    Text(
                        text = stringResource(R.string.label_both),
                        color = if (selectedRole == UserRole.BOTH) KutiraAmber else Color.White.copy(alpha = 0.7f),
                        fontWeight = if (selectedRole == UserRole.BOTH) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    if (isLoginMode) viewModel.login() else viewModel.register()
                },
                colors = ButtonDefaults.buttonColors(containerColor = KutiraAmber),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = authState !is UiState.Loading
            ) {
                if (authState is UiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(if (isLoginMode) "LOG IN" else "SIGN UP", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            
            if (authState is UiState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (authState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isLoginMode) "Don't have an account? Sign Up" else "Already have an account? Log In",
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { 
                    viewModel.toggleAuthMode()
                    viewModel.resetAuthState()
                }.padding(8.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun RoleCard(
    title: String,
    subtitle: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) BorderStroke(2.dp, KutiraAmber) else BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) KutiraAmber.copy(alpha = 0.2f) else Color.Transparent
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, style = MaterialTheme.typography.displaySmall)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}
