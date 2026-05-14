package com.kutirakone.app.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.kutirakone.app.model.Listing
import com.kutirakone.app.model.enums.ListingStatus
import com.kutirakone.app.ui.common.components.FabricCard
import com.kutirakone.app.ui.common.utils.UiState
import com.kutirakone.app.ui.theme.KutiraGreen
import com.kutirakone.app.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviousOrdersScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val user = authViewModel.getCurrentUser()
    var deliveredListingsState by remember { mutableStateOf<UiState<List<Listing>>>(UiState.Loading) }

    LaunchedEffect(user) {
        if (user != null) {
            FirebaseFirestore.getInstance()
                .collection("listings")
                .whereEqualTo("status", ListingStatus.DELIVERED.name)
                .whereEqualTo("reservedFor", user.uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    val list = snapshot.documents.mapNotNull { Listing.fromDocument(it) }
                    deliveredListingsState = UiState.Success(list)
                }
                .addOnFailureListener { error ->
                    deliveredListingsState = UiState.Error(error.message ?: "Failed to load previous orders")
                }
        } else {
            deliveredListingsState = UiState.Success(emptyList())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Previous Orders 📦", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = deliveredListingsState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = KutiraGreen)
                    }
                }
                is UiState.Success -> {
                    val listings = state.data

                    if (listings.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "📦",
                                style = MaterialTheme.typography.displayLarge,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                "No Previous Orders",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "You haven't purchased or received any fabric scraps yet. Once your active orders are delivered, they will appear here!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(listings) { listing ->
                                FabricCard(
                                    listing = listing,
                                    onClick = { navController.navigate(com.kutirakone.app.navigation.Screen.ListingDetail.createRoute(listing.id)) },
                                    showDistanceAndRating = false
                                )
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Failed to load previous orders", color = MaterialTheme.colorScheme.error)
                    }
                }
                else -> {}
            }
        }
    }
}
