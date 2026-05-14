package com.kutirakone.app.ui.vendor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kutirakone.app.navigation.Screen
import com.kutirakone.app.ui.common.components.EmptyStateView
import com.kutirakone.app.ui.common.components.FabricCard
import com.kutirakone.app.ui.common.components.FabricCardSkeleton
import com.kutirakone.app.ui.common.utils.UiState
import com.kutirakone.app.ui.theme.KutiraAmber
import com.kutirakone.app.viewmodel.AuthViewModel
import com.kutirakone.app.viewmodel.ListingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorDashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    listingViewModel: ListingViewModel = viewModel()
) {
    val user = authViewModel.getCurrentUser()
    val userProfile by authViewModel.currentUserProfile.collectAsState()
    val myListings by listingViewModel.myListings.collectAsState()
    val isUserLoggedIn by authViewModel.isUserLoggedIn.collectAsState()
    var selectedShopTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(isUserLoggedIn) {
        if (!isUserLoggedIn) {
            navController.navigate(Screen.Auth.route) {
                popUpTo(0)
            }
        }
    }

    LaunchedEffect(user) {
        user?.uid?.let { listingViewModel.loadMyListings(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Shop") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.RequestMgmt.route) }) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Requests")
                    }
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(Icons.Filled.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.Upload.route) },
                containerColor = KutiraAmber
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Upload Scrap")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (myListings is UiState.Success) {
                TabRow(
                    selectedTabIndex = selectedShopTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedShopTab == 0,
                        onClick = { selectedShopTab = 0 },
                        text = { Text("🟢 Available Scraps", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedShopTab == 1,
                        onClick = { selectedShopTab = 1 },
                        text = { Text("📦 Delivered Orders", fontWeight = FontWeight.Bold) }
                    )
                }
            }

            when (myListings) {
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
                    val allListings = (myListings as UiState.Success).data
                    val filteredListings = if (selectedShopTab == 0) {
                        allListings.filter { it.status == com.kutirakone.app.model.enums.ListingStatus.AVAILABLE }
                    } else {
                        allListings.filter { it.status == com.kutirakone.app.model.enums.ListingStatus.DELIVERED }
                    }

                    if (filteredListings.isEmpty()) {
                        EmptyStateView(
                            title = if (selectedShopTab == 0) "No active scraps" else "No delivered scraps",
                            subtitle = if (selectedShopTab == 0) "Tap the + button to add your first fabric scrap" else "Delivered orders will appear here!",
                            actionLabel = if (selectedShopTab == 0) "Upload Now" else null,
                            onAction = { if (selectedShopTab == 0) navController.navigate(Screen.Upload.route) }
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredListings) { listing ->
                                FabricCard(
                                    listing = listing,
                                    onClick = { navController.navigate(Screen.ListingDetail.createRoute(listing.id)) },
                                    showDistanceAndRating = selectedShopTab == 0
                                )
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    Text(
                        text = (myListings as UiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                else -> {}
            }
        }
    }
}
