package com.kutirakone.app.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kutirakone.app.navigation.Screen
import com.kutirakone.app.ui.common.components.EmptyStateView
import com.kutirakone.app.ui.common.components.FabricCard
import com.kutirakone.app.ui.common.components.FabricCardSkeleton
import com.kutirakone.app.ui.common.components.MaterialFilterRow
import com.kutirakone.app.ui.common.components.RadiusFilterChips
import com.kutirakone.app.ui.common.utils.UiState
import com.kutirakone.app.ui.theme.KutiraAmber
import com.kutirakone.app.utils.LocationUtils
import com.kutirakone.app.viewmodel.ListingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDashboardScreen(
    navController: NavController,
    listingViewModel: ListingViewModel = viewModel(),
    authViewModel: com.kutirakone.app.viewmodel.AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val nearbyListings by listingViewModel.nearbyListings.collectAsState()
    val selectedRadius by listingViewModel.selectedRadiusKm.collectAsState()
    val selectedMaterial by listingViewModel.selectedMaterialFilter.collectAsState()
    val isUserLoggedIn by authViewModel.isUserLoggedIn.collectAsState()
    val userProfile by authViewModel.currentUserProfile.collectAsState()
    var currentLocationName by remember { mutableStateOf("Locating...") }

    LaunchedEffect(isUserLoggedIn) {
        if (!isUserLoggedIn) {
            navController.navigate(Screen.Auth.route) {
                popUpTo(0)
            }
        }
    }

    LaunchedEffect(selectedRadius, selectedMaterial, userProfile) {
        coroutineScope.launch {
            val liveLoc = LocationUtils.getCurrentLocation(context)
            if (liveLoc != null) {
                // Priority 1: Use active live device GPS location!
                val address = LocationUtils.getAddressFromCoordinates(context, liveLoc.first, liveLoc.second)
                currentLocationName = if (address.isNotBlank() && address != "Unknown Area") address else (userProfile?.village?.ifBlank { "Active GPS Location" } ?: "Active GPS Location")
                listingViewModel.loadNearbyListings(liveLoc.first, liveLoc.second)
            } else {
                // Priority 2: Fallback to saved user profile coordinates
                val profileLoc = userProfile?.location
                if (profileLoc != null) {
                    currentLocationName = if (userProfile?.village?.isNotBlank() == true) userProfile!!.village else "Saved Shop Location"
                    listingViewModel.loadNearbyListings(profileLoc.latitude, profileLoc.longitude)
                } else {
                    currentLocationName = "Location Required"
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Discover Fabrics")
                        Text(currentLocationName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.MapView.route) }) {
                        Icon(Icons.Filled.Map, contentDescription = "Map View")
                    }
                    IconButton(onClick = { navController.navigate(Screen.RequestMgmt.createRoute(1)) }) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = "My Orders / Cart")
                    }
                    IconButton(onClick = { navController.navigate(Screen.PreviousOrders.route) }) {
                        Icon(Icons.Filled.History, contentDescription = "Previous Orders")
                    }
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(Icons.Filled.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.Inspire.route) },
                containerColor = KutiraAmber
            ) {
                Text("✨") // Sparkle icon for inspire
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            MaterialFilterRow(
                selectedMaterial = selectedMaterial,
                onMaterialSelected = { listingViewModel.updateMaterialFilter(it) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            when (nearbyListings) {
                is UiState.Loading -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(4) { FabricCardSkeleton() }
                    }
                }
                is UiState.Success -> {
                    val listings = (nearbyListings as UiState.Success).data
                    if (listings.isEmpty()) {
                        EmptyStateView(
                            title = "No scraps found nearby",
                            subtitle = "Try selecting a larger radius or a different material.",
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(listings) { listing ->
                                FabricCard(
                                    listing = listing,
                                    onClick = { navController.navigate(Screen.ListingDetail.createRoute(listing.id)) }
                                )
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    Text(
                        text = (nearbyListings as UiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                else -> {}
            }
        }
    }
}
