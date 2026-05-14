package com.kutirakone.app.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kutirakone.app.navigation.Screen
import com.kutirakone.app.ui.common.utils.UiState
import com.kutirakone.app.ui.theme.KutiraAmber
import com.kutirakone.app.viewmodel.AuthViewModel
import com.kutirakone.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentUser = authViewModel.getCurrentUser()
    val profileState by profileViewModel.userProfileState.collectAsState()
    val updateState by profileViewModel.updateProfileState.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var location by remember { mutableStateOf<com.google.firebase.firestore.GeoPoint?>(null) }
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            profileViewModel.loadUserProfile(currentUser.uid)
        }
    }

    LaunchedEffect(profileState, currentUser) {
        if (profileState is UiState.Success) {
            val user = (profileState as UiState.Success).data
            name = user.name
            village = user.village
            location = user.location
            phone = user.phone
            username = currentUser?.email?.substringBefore("@") ?: ""
        }
    }

    LaunchedEffect(updateState) {
        if (updateState is UiState.Success) {
            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            profileViewModel.resetUpdateState()
        } else if (updateState is UiState.Error) {
            Toast.makeText(context, (updateState as UiState.Error).message, Toast.LENGTH_SHORT).show()
            profileViewModel.resetUpdateState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (profileState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Success -> {
                val user = (profileState as UiState.Success).data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar Placeholder
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatColumn("Role", user.role.name)
                        StatColumn("Listings", user.listingCount.toString())
                        if (user.role != com.kutirakone.app.model.enums.UserRole.ARTISAN) {
                            StatColumn("Rating", "⭐ " + String.format(java.util.Locale.US, "%.1f", user.avgRating))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider()
                    
                    if (user.role == com.kutirakone.app.model.enums.UserRole.BOTH) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Dual Role Workspace",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Card(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "You are registered as both Artisan and Tailor. Switch your active dashboard view below:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            navController.navigate(Screen.CustomerDashboard.route) {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = KutiraAmber),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Artisan View 🎨", style = MaterialTheme.typography.labelLarge)
                                    }
                                    Button(
                                        onClick = {
                                            navController.navigate(Screen.VendorDashboard.route) {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Tailor View 🧵", style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = village,
                        onValueChange = { village = it },
                        label = { Text("Village / Location") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val loc = com.kutirakone.app.utils.LocationUtils.getCurrentLocation(context)
                                if (loc != null) {
                                    location = com.google.firebase.firestore.GeoPoint(loc.first, loc.second)
                                    val address = com.kutirakone.app.utils.LocationUtils.getAddressFromCoordinates(context, loc.first, loc.second)
                                    village = address
                                } else {
                                    Toast.makeText(context, "Could not fetch GPS location", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (location != null) "📍 Coords: %.4f, %.4f".format(location!!.latitude, location!!.longitude) else "📍 Pin Exact Shop/Home Location")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("New Password (leave blank to keep unchanged)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            currentUser?.uid?.let {
                                profileViewModel.updateProfileAndCredentials(
                                    uid = it,
                                    name = name,
                                    village = village,
                                    location = location,
                                    newUsername = username,
                                    newPhone = phone,
                                    newPassword = password.takeIf { p -> p.isNotBlank() },
                                    newRole = user.role
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = updateState !is UiState.Loading
                    ) {
                        if (updateState is UiState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Save Changes")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            authViewModel.signOut()
                            navController.navigate(Screen.Auth.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Filled.Logout, contentDescription = "Log Out")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Out", fontWeight = FontWeight.Bold)
                    }
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text((profileState as UiState.Error).message)
                }
            }
            else -> {}
        }
    }
}

@Composable
fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}
