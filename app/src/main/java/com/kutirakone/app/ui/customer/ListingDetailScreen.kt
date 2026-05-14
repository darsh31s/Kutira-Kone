package com.kutirakone.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.kutirakone.app.model.enums.ListingType
import com.kutirakone.app.ui.common.components.AIIdeaCard
import com.kutirakone.app.ui.common.components.ListingDetailSkeleton
import com.kutirakone.app.ui.common.utils.UiState
import com.kutirakone.app.ui.theme.KutiraGreen
import com.kutirakone.app.viewmodel.AIViewModel
import com.kutirakone.app.viewmodel.AuthViewModel
import com.kutirakone.app.viewmodel.ListingViewModel
import com.kutirakone.app.viewmodel.RequestViewModel
import com.kutirakone.app.viewmodel.ReviewViewModel
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(
    navController: NavController,
    listingId: String,
    listingViewModel: ListingViewModel = viewModel(),
    aiViewModel: AIViewModel = viewModel(),
    requestViewModel: RequestViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    reviewViewModel: ReviewViewModel = viewModel()
) {
    val listingState by listingViewModel.selectedListing.collectAsState()
    val aiIdeasState by aiViewModel.designIdeas.collectAsState()
    val createRequestState by requestViewModel.createRequestState.collectAsState()
    val user = authViewModel.getCurrentUser()
    val userProfile by authViewModel.currentUserProfile.collectAsState()
    val myRequestsState by requestViewModel.myRequests.collectAsState()
    val hasReviewed by reviewViewModel.hasReviewed.collectAsState()

    val hasPendingRequest = remember(myRequestsState, listingId) {
        val state = myRequestsState
        if (state is UiState.Success) {
            state.data.any { it.listingId == listingId && it.status == com.kutirakone.app.model.enums.RequestStatus.PENDING }
        } else {
            false
        }
    }

    var showSwapDialog by remember { mutableStateOf(false) }
    var swapOfferText by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    var showPaymentSuccessDialog by remember { mutableStateOf(false) }
    var showPaymentSelectionDialog by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf("") }
    var isPaying by remember { mutableStateOf(false) }

    LaunchedEffect(listingId) {
        listingViewModel.loadListing(listingId)
    }

    LaunchedEffect(user, listingId) {
        if (user != null) {
            requestViewModel.loadMyRequests(user.uid)
            reviewViewModel.checkIfReviewed(user.uid, listingId)
        }
    }

    LaunchedEffect(listingState) {
        if (listingState is UiState.Success) {
            val listing = (listingState as UiState.Success).data
            aiViewModel.generateIdeasForListing(listing.id, listing.material.name, listing.sizeMetres)
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(createRequestState) {
        if (createRequestState is UiState.Success) {
            android.widget.Toast.makeText(context, "Request sent successfully!", android.widget.Toast.LENGTH_SHORT).show()
            if (user != null) {
                requestViewModel.loadMyRequests(user.uid)
            }
            requestViewModel.resetCreateState()
        } else if (createRequestState is UiState.Error) {
            android.widget.Toast.makeText(context, (createRequestState as UiState.Error).message, android.widget.Toast.LENGTH_SHORT).show()
            requestViewModel.resetCreateState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        when (listingState) {
            is UiState.Loading -> ListingDetailSkeleton()
            is UiState.Success -> {
                val listing = (listingState as UiState.Success).data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        if (listing.safePhotoURLs.isNotEmpty()) {
                            AsyncImage(
                                model = listing.safePhotoURLs.first(),
                                contentDescription = "Fabric",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(Color.LightGray))
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${listing.sizeMetres}m • ${listing.material.displayName} • ${listing.colour}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val priceText = when (listing.type) {
                            ListingType.SELL -> "₹${listing.price}"
                            ListingType.SWAP -> "SWAP ONLY"
                            ListingType.FREE -> "FREE"
                        }
                        Text(
                            text = priceText,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Seller: ${listing.userName} (⭐ ${listing.userAvgRating})", style = MaterialTheme.typography.bodyLarge)
                        
                        Spacer(modifier = Modifier.height(24.dp))

                        val isReservedForMe = listing.status == com.kutirakone.app.model.enums.ListingStatus.RESERVED &&
                                listing.reservedFor == user?.uid
                        val isCompletedForMe = listing.status == com.kutirakone.app.model.enums.ListingStatus.COMPLETED &&
                                listing.reservedFor == user?.uid
                        val isDeliveredForMe = listing.status == com.kutirakone.app.model.enums.ListingStatus.DELIVERED &&
                                listing.reservedFor == user?.uid

                        if (isDeliveredForMe) {
                            if (!hasReviewed) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    colors = CardDefaults.cardColors(containerColor = com.kutirakone.app.ui.theme.KutiraAmber.copy(alpha = 0.1f)),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, com.kutirakone.app.ui.theme.KutiraAmber)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            "📦 Order Delivered!",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFC59B27) // Golden
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Your fabric order has been successfully delivered and received. Please rate your experience with the tailor below!",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Button(
                                            onClick = {
                                                navController.navigate(com.kutirakone.app.navigation.Screen.Review.createRoute(listing.userId, listing.id))
                                            },
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = com.kutirakone.app.ui.theme.KutiraAmber)
                                        ) {
                                            Text("⭐ Rate & Review Tailor", fontWeight = FontWeight.Bold, color = Color.Black)
                                        }
                                    }
                                }
                            } else {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    colors = CardDefaults.cardColors(containerColor = KutiraGreen.copy(alpha = 0.1f)),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, KutiraGreen)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            "🎉 Order Completed!",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = KutiraGreen
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "You have successfully purchased this fabric listing and left your review. You can coordinate collection with the tailor directly via Chat!",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Button(
                                            onClick = {
                                                navController.navigate(com.kutirakone.app.navigation.Screen.RequestMgmt.route)
                                            },
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = KutiraGreen)
                                        ) {
                                            Text("💬 Open Orders / Chat", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        } else if (isCompletedForMe) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = KutiraGreen.copy(alpha = 0.1f)),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, KutiraGreen)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "🎉 Order Completed!",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = KutiraGreen
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "You have successfully purchased this fabric listing. You can coordinate collection with the tailor directly via Chat!",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Button(
                                        onClick = {
                                            navController.navigate(com.kutirakone.app.navigation.Screen.RequestMgmt.route)
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = KutiraGreen)
                                    ) {
                                        Text("💬 Open Orders / Chat", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else if (isReservedForMe) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "🎉 Reserved For You",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (listing.type == ListingType.SWAP) {
                                        Text(
                                            "The tailor has accepted your swap offer! Click below to confirm your exchange and initiate delivery tracking.",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = {
                                                selectedPaymentMethod = "SWAP_EXCHANGE"
                                                showPaymentSuccessDialog = true
                                            },
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = KutiraGreen)
                                        ) {
                                            Text("🤝 Confirm Swap Exchange", fontWeight = FontWeight.Bold)
                                        }
                                    } else if (listing.type == ListingType.FREE) {
                                        Text(
                                            "The tailor has accepted your free collection request! Click below to confirm your pickup and initiate delivery tracking.",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = {
                                                selectedPaymentMethod = "FREE_COLLECTION"
                                                showPaymentSuccessDialog = true
                                            },
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = KutiraGreen)
                                        ) {
                                            Text("🎁 Confirm Free Collection", fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Text(
                                            "The tailor has accepted your buy request! Choose a checkout payment option below to finalize.",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = {
                                                selectedPaymentMethod = "COD"
                                                showPaymentSuccessDialog = true
                                            },
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = KutiraGreen)
                                        ) {
                                            Text("💵 Cash on Delivery (COD)", fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                showPaymentSelectionDialog = true
                                            },
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = com.kutirakone.app.ui.theme.KutiraAmber)
                                        ) {
                                            Text("💳 Pay Online (UPI / Card)", fontWeight = FontWeight.Bold, color = Color.Black)
                                        }
                                    }
                                }
                            }
                        } else if (hasPendingRequest) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "⏳ Request Sent (Pending Approval)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "You have already sent a request for this fabric scrap. The tailor will review and respond to it shortly!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        } else if (user?.uid != listing.userId) {
                            Button(
                                onClick = {
                                    if (listing.type == ListingType.SWAP) {
                                        showSwapDialog = true
                                    } else {
                                        requestViewModel.createBuyRequest(listing, user?.uid ?: "", userProfile?.name ?: "Artisan")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = KutiraGreen),
                                enabled = createRequestState !is UiState.Loading
                            ) {
                                if (createRequestState is UiState.Loading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text(when (listing.type) {
                                        ListingType.SWAP -> "Request to Swap"
                                        ListingType.FREE -> "Request Free Collection"
                                        else -> "Request to Buy"
                                    }, color = Color.White)
                                }
                            }
                        } else {
                            if (listing.status == com.kutirakone.app.model.enums.ListingStatus.DELIVERED) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    colors = CardDefaults.cardColors(containerColor = KutiraGreen.copy(alpha = 0.1f)),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, KutiraGreen)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            "🎉 Sold & Delivered!",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = KutiraGreen
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "This fabric scrap has been successfully sold, paid for, and delivered to the artisan buyer! Past sales details are recorded in your Delivered sections.",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            } else {
                                var showDeleteConfirm by remember { mutableStateOf(false) }
                                
                                Button(
                                    onClick = { showDeleteConfirm = true },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("❌ Delete Fabric Listing", color = Color.White)
                                }
                                
                                if (showDeleteConfirm) {
                                    AlertDialog(
                                        onDismissRequest = { showDeleteConfirm = false },
                                        title = { Text("Delete Listing?") },
                                        text = { Text("Are you sure you want to permanently delete this fabric listing from your shop?") },
                                        confirmButton = {
                                            Button(
                                                onClick = {
                                                    listingViewModel.deleteListing(listing.id)
                                                    android.widget.Toast.makeText(context, "Listing deleted successfully", android.widget.Toast.LENGTH_SHORT).show()
                                                    showDeleteConfirm = false
                                                    navController.popBackStack()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Text("Delete")
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showDeleteConfirm = false }) {
                                                Text("Cancel")
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text("✨ AI Design Ideas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("AI-generated ideas · based on this fabric", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))

                        when (aiIdeasState) {
                            is UiState.Loading -> CircularProgressIndicator()
                            is UiState.Success -> {
                                val ideas = (aiIdeasState as UiState.Success).data
                                ideas.forEach { idea ->
                                    AIIdeaCard(idea)
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                            is UiState.Error -> Text((aiIdeasState as UiState.Error).message)
                            else -> {}
                        }
                    }
                }
                
                if (showSwapDialog) {
                    AlertDialog(
                        onDismissRequest = { showSwapDialog = false },
                        title = { Text("Swap Offer") },
                        text = {
                            OutlinedTextField(
                                value = swapOfferText,
                                onValueChange = { swapOfferText = it },
                                label = { Text("Describe what you offer") }
                            )
                        },
                        confirmButton = {
                            Button(onClick = {
                                requestViewModel.createSwapRequest(listing, swapOfferText, user?.uid ?: "", userProfile?.name ?: "Artisan")
                                showSwapDialog = false
                            }) {
                                Text("Send")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showSwapDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                if (showPaymentSelectionDialog) {
                    var payMode by remember { mutableStateOf("UPI") }
                    var upiId by remember { mutableStateOf("") }
                    var cardNumber by remember { mutableStateOf("") }
                    var expiry by remember { mutableStateOf("") }
                    var cvv by remember { mutableStateOf("") }

                    AlertDialog(
                        onDismissRequest = { if (!isPaying) showPaymentSelectionDialog = false },
                        title = { Text("Secure Online Payment", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    FilterChip(
                                        selected = payMode == "UPI",
                                        onClick = { payMode = "UPI" },
                                        label = { Text("UPI (GPay/PhonePe)") }
                                    )
                                    FilterChip(
                                        selected = payMode == "CARD",
                                        onClick = { payMode = "CARD" },
                                        label = { Text("Card") }
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                if (payMode == "UPI") {
                                    OutlinedTextField(
                                        value = upiId,
                                        onValueChange = { upiId = it },
                                        label = { Text("Enter UPI ID (e.g. name@okhdfcbank)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    OutlinedTextField(
                                        value = cardNumber,
                                        onValueChange = { cardNumber = it },
                                        label = { Text("Card Number") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = expiry,
                                            onValueChange = { expiry = it },
                                            label = { Text("MM/YY") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedTextField(
                                            value = cvv,
                                            onValueChange = { cvv = it },
                                            label = { Text("CVV") },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                
                                if (isPaying) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("Processing Secure Payment...")
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isPaying = true
                                        kotlinx.coroutines.delay(2000)
                                        isPaying = false
                                        showPaymentSelectionDialog = false
                                        selectedPaymentMethod = "ONLINE"
                                        showPaymentSuccessDialog = true
                                    }
                                },
                                enabled = !isPaying && (if (payMode == "UPI") upiId.isNotBlank() else cardNumber.isNotBlank() && expiry.isNotBlank() && cvv.isNotBlank())
                            ) {
                                Text("Pay ₹${listing.price ?: 0.0}")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showPaymentSelectionDialog = false }, enabled = !isPaying) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                if (showPaymentSuccessDialog) {
                    AlertDialog(
                        onDismissRequest = { },
                        title = { Text(when (selectedPaymentMethod) {
                            "SWAP_EXCHANGE" -> "🤝 Swap Confirmed!"
                            "FREE_COLLECTION" -> "🎁 Collection Confirmed!"
                            else -> "🎉 Order Confirmed!"
                        }, fontWeight = FontWeight.Bold, color = KutiraGreen) },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text(when (selectedPaymentMethod) {
                                    "SWAP_EXCHANGE" -> "✨ Thank you for swapping! ✨"
                                    "FREE_COLLECTION" -> "✨ Free Collection Reserved! ✨"
                                    else -> "✨ Thank you for your purchase! ✨"
                                }, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                if (selectedPaymentMethod == "SWAP_EXCHANGE") {
                                    Text("Your swap exchange has been mutually confirmed! You can now exchange your materials with the tailor and track delivery.")
                                } else if (selectedPaymentMethod == "FREE_COLLECTION") {
                                    Text("Your free scrap collection has been confirmed! You can now pick up your materials from the tailor's shop.")
                                } else if (selectedPaymentMethod == "COD") {
                                    Text("Your order has been placed via Cash on Delivery. Please pay the tailor when collecting your fabric scrap pieces.")
                                } else {
                                    Text("Your online payment has been securely processed and transferred to the tailor. You can collect your fabric from the shop!")
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    listingViewModel.updateListingStatus(listing.id, com.kutirakone.app.model.enums.ListingStatus.COMPLETED)
                                    showPaymentSuccessDialog = false
                                    navController.popBackStack()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = KutiraGreen)
                            ) {
                                Text("Got it!")
                            }
                        }
                    )
                }
            }
            is UiState.Error -> Text((listingState as UiState.Error).message)
            else -> {}
        }
    }
}
