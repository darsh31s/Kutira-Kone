package com.kutirakone.app.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.kutirakone.app.navigation.Screen
import com.kutirakone.app.ui.common.utils.UiState
import com.kutirakone.app.utils.LocationUtils
import com.kutirakone.app.viewmodel.ListingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapViewScreen(
    navController: NavController,
    listingViewModel: ListingViewModel = viewModel()
) {
    val nearbyListings by listingViewModel.nearbyListings.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var currentPos by remember { mutableStateOf(LatLng(20.5937, 78.9629)) } // Default India center
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentPos, 5f)
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            // Safe fallback: If emulator GPS is off, query starting from emulator's default California location
            val loc = LocationUtils.getCurrentLocation(context) ?: Pair(37.421998, -122.084)
            currentPos = LatLng(loc.first, loc.second)
            cameraPositionState.position = CameraPosition.fromLatLngZoom(currentPos, 14f)
            listingViewModel.loadNearbyListings(loc.first, loc.second)
        }
    }

    // Auto-center camera over the listings if they are loaded successfully
    LaunchedEffect(nearbyListings) {
        if (nearbyListings is UiState.Success) {
            val listings = (nearbyListings as UiState.Success).data
            if (listings.isNotEmpty()) {
                val firstListing = listings.first()
                val loc = firstListing.location
                if (loc != null) {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                        LatLng(loc.latitude, loc.longitude),
                        14f
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Map View") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                // Azure blue marker for the user's current location!
                Marker(
                    state = MarkerState(position = currentPos),
                    title = "My Location",
                    icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                        com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE
                    )
                )

                if (nearbyListings is UiState.Success) {
                    val listings = (nearbyListings as UiState.Success).data
                    
                    // Track coordinates count to prevent completely overlapping markers
                    val coordUsage = mutableMapOf<String, Int>()

                    listings.forEach { listing ->
                        val loc = listing.location
                        if (loc != null) {
                            val coordKey = "${loc.latitude},${loc.longitude}"
                            val count = coordUsage.getOrDefault(coordKey, 0)
                            coordUsage[coordKey] = count + 1

                            // Apply a tiny spiral/jitter offset if multiple markers occupy the exact same coordinate
                            val offset = 0.00008 * count
                            val angle = count * 0.9 // golden spiral-ish distribution
                            val finalLat = loc.latitude + (offset * kotlin.math.cos(angle))
                            val finalLng = loc.longitude + (offset * kotlin.math.sin(angle))
                            
                            Marker(
                                state = MarkerState(position = LatLng(finalLat, finalLng)),
                                title = "${listing.sizeMetres}m ${listing.material.displayName}",
                                snippet = "Tap to view details",
                                onClick = {
                                    navController.navigate(Screen.ListingDetail.createRoute(listing.id))
                                    false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
