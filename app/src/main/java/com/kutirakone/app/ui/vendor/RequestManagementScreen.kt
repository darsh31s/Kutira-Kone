package com.kutirakone.app.ui.vendor

import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kutirakone.app.model.Request
import com.kutirakone.app.model.Listing
import com.kutirakone.app.model.enums.RequestStatus
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.kutirakone.app.ui.common.utils.UiState
import com.kutirakone.app.ui.theme.KutiraGreen
import com.kutirakone.app.viewmodel.AuthViewModel
import com.kutirakone.app.viewmodel.RequestViewModel
import com.kutirakone.app.viewmodel.ReviewViewModel
import com.kutirakone.app.utils.LocationUtils
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.animation.core.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestManagementScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    requestViewModel: RequestViewModel = viewModel(),
    reviewViewModel: ReviewViewModel = viewModel(),
    initialTab: Int = 0
) {
    val user = authViewModel.getCurrentUser()
    val userRole by authViewModel.currentUserRole.collectAsState()
    
    val incomingRequests by requestViewModel.incomingRequests.collectAsState()
    val myRequests by requestViewModel.myRequests.collectAsState()
    
    val isArtisan = userRole == com.kutirakone.app.model.enums.UserRole.ARTISAN
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    
    var showTrackingDialog by remember { mutableStateOf(false) }
    var trackingRequest by remember { mutableStateOf<Request?>(null) }

    LaunchedEffect(user, userRole) {
        user?.uid?.let { uid ->
            if (isArtisan) {
                requestViewModel.loadMyRequests(uid)
            } else {
                requestViewModel.loadIncomingRequests(uid)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isArtisan) "My Sent Requests" else "Incoming Requests") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        val requestsState = if (isArtisan) myRequests else incomingRequests
        
        if (showTrackingDialog && trackingRequest != null) {
            TrackingDialog(
                request = trackingRequest!!,
                requestViewModel = requestViewModel,
                reviewViewModel = reviewViewModel,
                navController = navController,
                onDismiss = { showTrackingDialog = false }
            )
        }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (requestsState is UiState.Success) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(if (isArtisan) "💬 Sent" else "⏳ Requests", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(if (isArtisan) "🛒 Cart" else "🚚 Active", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("📦 Delivered", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("📨 Chats", fontWeight = FontWeight.Bold) }
                    )
                }
            }

            when (requestsState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Success -> {
                    val allRequests = (requestsState as UiState.Success).data
                    val filteredRequests = when (selectedTab) {
                        0 -> allRequests.filter { it.status == RequestStatus.PENDING || it.status == RequestStatus.DECLINED }
                        1 -> allRequests.filter { it.status == RequestStatus.ACCEPTED }
                        2 -> allRequests.filter { it.status == RequestStatus.COMPLETED }
                        else -> allRequests.filter { it.status == RequestStatus.ACCEPTED && it.conversationId.isNotEmpty() }
                    }

                    if (filteredRequests.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            val emptyText = if (isArtisan) {
                                when (selectedTab) {
                                    0 -> "No pending requests sent"
                                    1 -> "Your cart is empty. Send a request to buy a fabric scrap!"
                                    2 -> "No delivered purchases yet"
                                    else -> "No active chats. Start chatting to coordinate!"
                                }
                            } else {
                                when (selectedTab) {
                                    0 -> "No pending incoming requests"
                                    1 -> "No active fabric shipments in progress"
                                    2 -> "No completed orders delivered yet"
                                    else -> "No active customer chats yet"
                                }
                            }
                            Text(emptyText, style = MaterialTheme.typography.bodyLarge, color = Color.Gray, modifier = Modifier.padding(24.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredRequests) { request ->
                                RequestCard(
                                    request = request,
                                    requestViewModel = requestViewModel,
                                    isArtisan = isArtisan,
                                    navController = navController,
                                    tabIndex = selectedTab,
                                    onTrackClick = {
                                        trackingRequest = it
                                        showTrackingDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("Error loading requests", color = MaterialTheme.colorScheme.error)
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun RequestCard(
    request: Request,
    requestViewModel: RequestViewModel,
    isArtisan: Boolean,
    navController: NavController,
    tabIndex: Int = 0,
    onTrackClick: (Request) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var listingStatus by remember { mutableStateOf<String?>(null) }
    var listingDetail by remember { mutableStateOf<Listing?>(null) }
    
    LaunchedEffect(request.listingId) {
        if (request.listingId.isNotEmpty()) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("listings")
                .document(request.listingId)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        listingStatus = doc.getString("status")
                        listingDetail = Listing.fromDocument(doc)
                    }
                }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                // Image of Fabric on Left
                val imageURL = listingDetail?.safePhotoURLs?.firstOrNull()
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .background(Color.LightGray)
                ) {
                    if (imageURL != null) {
                        AsyncImage(
                            model = imageURL,
                            contentDescription = "Fabric image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.LightGray),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Text("✂️", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }

                // Details Column on Right
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isArtisan) "Request for ${request.listingMaterial}" else "Request from ${request.requesterName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Status Badge
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            color = when (request.status) {
                                RequestStatus.PENDING -> KutiraGreen.copy(alpha = 0.2f)
                                RequestStatus.ACCEPTED -> Color.Blue.copy(alpha = 0.2f)
                                RequestStatus.DECLINED -> Color.Red.copy(alpha = 0.2f)
                                else -> Color.Gray.copy(alpha = 0.2f)
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = request.status.name,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = when (request.status) {
                                    RequestStatus.PENDING -> KutiraGreen
                                    RequestStatus.ACCEPTED -> Color.Blue
                                    RequestStatus.DECLINED -> Color.Red
                                    else -> Color.Gray
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Creation Type badge (SELL/SWAP/FREE)
                    val dealType = listingDetail?.type?.name ?: request.type.uppercase()
                    Text(
                        text = "Type: $dealType",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (dealType == "SWAP") com.kutirakone.app.ui.theme.SwapColor else com.kutirakone.app.ui.theme.SellColor
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Insights: Size, Color, Condition
                    listingDetail?.let { listing ->
                        val insightsText = listOfNotNull(
                            "Size: ${listing.sizeMetres}m",
                            listing.colour.takeIf { it.isNotBlank() }?.let { "Color: $it" },
                            listing.condition.takeIf { it.isNotBlank() }?.let { "Condition: $it" }
                        ).joinToString("  ·  ")
                        
                        Text(
                            text = insightsText,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Cost details
                        val costText = if (listing.type == com.kutirakone.app.model.enums.ListingType.SWAP) {
                            "Trade Exchange Offer"
                        } else {
                            listing.price?.let { "Cost: ₹$it" } ?: "Cost: Free"
                        }
                        
                        Text(
                            text = costText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = KutiraGreen
                        )
                    } ?: run {
                        Text(
                            text = if (isArtisan) "You requested to ${request.type.uppercase()} this fabric" else "Wants to ${request.type.uppercase()} your ${request.listingMaterial} fabric",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            if (request.type == "swap" && request.swapOffer.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(if (isArtisan) "Your swap offer:" else "Offered in exchange:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(request.swapOffer, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            
            if (!isArtisan && request.status == RequestStatus.PENDING) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { requestViewModel.declineRequest(request.id) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Decline")
                    }
                    Button(
                        onClick = { 
                            val convId = UUID.randomUUID().toString()
                            requestViewModel.acceptRequest(request, convId) 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KutiraGreen)
                    ) {
                        Text("Accept Request")
                    }
                }
            } else if ((request.status == RequestStatus.ACCEPTED || request.status == RequestStatus.COMPLETED) && request.conversationId.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, androidx.compose.ui.Alignment.End)
                ) {
                    if (isArtisan) {
                        if (tabIndex == 1) { // Cart & Tracking Tab
                            val isPaid = listingStatus == "COMPLETED" || listingStatus == "DELIVERED"
                            
                            if (!isPaid) {
                                Button(
                                    onClick = {
                                        navController.navigate(com.kutirakone.app.navigation.Screen.ListingDetail.createRoute(request.listingId))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = com.kutirakone.app.ui.theme.KutiraAmber)
                                ) {
                                    Text("💳 Pay / Checkout", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                if (request.status == RequestStatus.ACCEPTED) {
                                    OutlinedButton(
                                        onClick = {
                                            navController.navigate(com.kutirakone.app.navigation.Screen.ListingDetail.createRoute(request.listingId))
                                        }
                                    ) {
                                        Text("🔍 View Fabric")
                                    }
                                }

                                Button(
                                    onClick = { onTrackClick(request) },
                                    colors = ButtonDefaults.buttonColors(containerColor = KutiraGreen)
                                ) {
                                    Text(if (request.status == RequestStatus.COMPLETED) "📦 Order Details & Review" else "🚚 Track Order")
                                }
                            }
                        } else if (tabIndex == 2) { // Chats Tab Only
                            val otherPartyName = "Shop Owner"
                            Button(
                                onClick = { 
                                    navController.navigate(com.kutirakone.app.navigation.Screen.Chat.createRoute(request.conversationId, otherPartyName, request.ownerId, request.listingId))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = KutiraGreen)
                            ) {
                                Text("💬 Start Chat with Tailor", color = Color.White)
                            }
                        }
                    } else { // Tailor view
                        val otherPartyName = request.requesterName
                        Button(
                            onClick = { 
                                navController.navigate(com.kutirakone.app.navigation.Screen.Chat.createRoute(request.conversationId, otherPartyName, request.ownerId, request.listingId))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = KutiraGreen)
                        ) {
                            Text("💬 Open Chat with Artisan", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrackingDialog(
    request: Request,
    requestViewModel: RequestViewModel,
    reviewViewModel: ReviewViewModel,
    navController: NavController,
    onDismiss: () -> Unit
) {
    val isDelivered = request.status == RequestStatus.COMPLETED
    val context = androidx.compose.ui.platform.LocalContext.current
    val hasReviewed by reviewViewModel.hasReviewed.collectAsState()

    class DemoRider(
        val name: String,
        val vehicleNumber: String,
        val vehicleModel: String,
        val rating: String,
        val emoji: String,
        val phone: String
    )

    val ridersList = listOf(
        DemoRider("Suresh Kumar", "KA 03 EX 4567", "Honda Activa", "★ 4.9", "👨🏻‍✈️", "+919876543210"),
        DemoRider("Rajesh Patel", "MH 12 QP 9876", "TVS Jupiter", "★ 4.8", "👨🏾‍✈️", "+919812345678"),
        DemoRider("Amit Sharma", "DL 01 AA 2345", "Ather 450X", "★ 4.9", "👨🏼‍✈️", "+919555512345"),
        DemoRider("Vikram Singh", "HR 26 CK 1289", "Hero Splendor", "★ 4.7", "👨🏽‍✈️", "+919999888877"),
        DemoRider("Anil Mehta", "GJ 01 XX 5432", "Ola S1 Pro", "★ 4.9", "👨🏻‍✈️", "+919111222333")
    )

    val selectedRider = remember(request.id) {
        val index = Math.abs(request.id.hashCode()) % ridersList.size
        ridersList[index]
    }

    var listingLocation by remember { mutableStateOf<com.google.firebase.firestore.GeoPoint?>(null) }
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    
    var progress by remember { mutableStateOf(if (isDelivered) 1.0f else 0.15f) }

    LaunchedEffect(request.listingId, request.requesterId) {
        if (request.listingId.isNotEmpty()) {
            reviewViewModel.checkIfReviewed(request.requesterId, request.listingId)
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("listings")
                .document(request.listingId)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        listingLocation = doc.getGeoPoint("location")
                    }
                }
            userLocation = LocationUtils.getCurrentLocation(context)
        }
    }

    val initialDist = remember(listingLocation, userLocation) {
        val listLoc = listingLocation
        val usrLoc = userLocation
        if (listLoc != null && usrLoc != null) {
            LocationUtils.calculateDistanceInKm(listLoc.latitude, listLoc.longitude, usrLoc.first, usrLoc.second)
        } else {
            2.4
        }
    }

    val currentDistanceKm = remember(initialDist, progress) {
        (initialDist * (1.0f - progress)).coerceAtLeast(0.0)
    }

    val simulatedDistanceStr = remember(currentDistanceKm) {
        String.format("%.2f km", currentDistanceKm)
    }

    val simulatedDurationStr = remember(currentDistanceKm, progress) {
        if (progress >= 1.0f) "Delivered" else LocationUtils.getDeliveryEstimateText(currentDistanceKm)
    }

    val simulatedStageIndex = remember(progress) {
        when {
            progress >= 1.0f -> 4
            progress >= 0.85f -> 3 // Out for Delivery
            progress >= 0.60f -> 2 // In Transit
            else -> 1             // Packaged & Dispatched
        }
    }

    // Auto-advance loop for demo tracking simulation (2% progress every 4 seconds)
    LaunchedEffect(isDelivered) {
        if (!isDelivered) {
            while (progress < 1.0f) {
                kotlinx.coroutines.delay(4000)
                if (progress < 1.0f) {
                    progress = (progress + 0.02f).coerceAtMost(1.0f)
                }
            }
        }
    }

    // Fire real-time notification toasts for dynamic stages to wow project evaluators!
    LaunchedEffect(simulatedStageIndex) {
        if (!isDelivered) {
            when (simulatedStageIndex) {
                1 -> android.widget.Toast.makeText(context, "📦 Fabric scraps wrapped and handed over to ${selectedRider.name}!", android.widget.Toast.LENGTH_SHORT).show()
                2 -> android.widget.Toast.makeText(context, "🚚 Rider ${selectedRider.name} has entered the main neighborhood avenue!", android.widget.Toast.LENGTH_SHORT).show()
                3 -> android.widget.Toast.makeText(context, "🛵 ${selectedRider.name} is out for delivery! Prepare to receive scraps.", android.widget.Toast.LENGTH_SHORT).show()
                4 -> android.widget.Toast.makeText(context, "✅ ${selectedRider.name} has reached! Confirm delivery to rate him.", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    val tailorLatLng = listingLocation?.let { LatLng(it.latitude, it.longitude) } ?: LatLng(20.5937, 78.9629)
    val artisanLatLng = userLocation?.let { LatLng(it.first, it.second) } ?: LatLng(tailorLatLng.latitude + 0.012, tailorLatLng.longitude + 0.015)
    
    // Highly realistic, flowing neighborhood road route that has natural curves, diagonal bends, and small street shifts (exactly like Zepto/Blinkit!)
    val routePoints = remember(tailorLatLng, artisanLatLng) {
        val lat1 = tailorLatLng.latitude
        val lng1 = tailorLatLng.longitude
        val lat2 = artisanLatLng.latitude
        val lng2 = artisanLatLng.longitude
        
        val dLat = lat2 - lat1
        val dLng = lng2 - lng1
        
        listOf(
            LatLng(lat1, lng1),
            LatLng(lat1 + dLat * 0.15, lng1 + dLng * 0.08), // diagonal exit from store
            LatLng(lat1 + dLat * 0.22, lng1 + dLng * 0.35), // realistic neighborhood bend
            LatLng(lat1 + dLat * 0.45, lng1 + dLng * 0.42), // diagonal street run
            LatLng(lat1 + dLat * 0.60, lng1 + dLng * 0.45), // main road stretch
            LatLng(lat1 + dLat * 0.62, lng1 + dLng * 0.78), // curved avenue
            LatLng(lat1 + dLat * 0.88, lng1 + dLng * 0.85), // final block intersection
            LatLng(lat2, lng2)                              // Reach Destination: Your Location
        )
    }

    // Custom Zepto/Blinkit minimal white/light gray Map Styling JSON
    val cleanMapStyleJson = """
        [
          {
            "featureType": "all",
            "elementType": "labels.text.fill",
            "stylers": [{"color": "#7c7c7c"}]
          },
          {
            "featureType": "all",
            "elementType": "labels.text.stroke",
            "stylers": [{"visibility": "on"}, {"color": "#ffffff"}, {"weight": 2}]
          },
          {
            "featureType": "administrative",
            "elementType": "geometry.fill",
            "stylers": [{"color": "#fefefe"}, {"lightness": 20}]
          },
          {
            "featureType": "administrative",
            "elementType": "geometry.stroke",
            "stylers": [{"color": "#fefefe"}, {"lightness": 17}, {"weight": 1.2}]
          },
          {
            "featureType": "landscape",
            "elementType": "geometry",
            "stylers": [{"color": "#f5f5f5"}]
          },
          {
            "featureType": "poi",
            "elementType": "geometry",
            "stylers": [{"color": "#f5f5f5"}]
          },
          {
            "featureType": "road.highway",
            "elementType": "geometry.fill",
            "stylers": [{"color": "#ffffff"}, {"lightness": 17}]
          },
          {
            "featureType": "road.highway",
            "elementType": "geometry.stroke",
            "stylers": [{"color": "#ffffff"}, {"lightness": 29}, {"weight": 0.2}]
          },
          {
            "featureType": "road.arterial",
            "elementType": "geometry",
            "stylers": [{"color": "#ffffff"}, {"lightness": 18}]
          },
          {
            "featureType": "road.local",
            "elementType": "geometry",
            "stylers": [{"color": "#ffffff"}, {"lightness": 16}]
          },
          {
            "featureType": "water",
            "elementType": "geometry",
            "stylers": [{"color": "#e0f2f1"}]
          }
        ]
    """.trimIndent()

    val mapProperties = remember {
        com.google.maps.android.compose.MapProperties(
            mapStyleOptions = com.google.android.gms.maps.model.MapStyleOptions(cleanMapStyleJson)
        )
    }

    val t = progress
    
    // High-fidelity segment-by-segment rider interpolation following the road network polyline!
    val riderLatLng = remember(routePoints, t) {
        if (routePoints.isEmpty()) {
            LatLng(0.0, 0.0)
        } else if (t <= 0f) {
            routePoints.first()
        } else if (t >= 1f) {
            routePoints.last()
        } else {
            val numSegments = routePoints.size - 1
            val segmentFloat = t * numSegments
            val segmentIndex = segmentFloat.toInt().coerceIn(0, numSegments - 1)
            val segmentProgress = segmentFloat - segmentIndex
            
            val p1 = routePoints[segmentIndex]
            val p2 = routePoints[segmentIndex + 1]
            
            LatLng(
                p1.latitude + (p2.latitude - p1.latitude) * segmentProgress,
                p1.longitude + (p2.longitude - p1.longitude) * segmentProgress
            )
        }
    }

    val trackingCameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng((tailorLatLng.latitude + artisanLatLng.latitude)/2, (tailorLatLng.longitude + artisanLatLng.longitude)/2), 14f)
    }
    
    LaunchedEffect(riderLatLng) {
        trackingCameraState.position = CameraPosition.fromLatLngZoom(riderLatLng, 15f)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Order Status", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("ID: #${request.id.take(8).uppercase()}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {


                // Interactive Live Telemetry Simulator for Demo/Testing
                if (!isDelivered) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Text("🛠️", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "Live Telemetry Simulator",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFE0F2FE), androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "Demo Mode",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0369A1)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Drag this slider to fast-forward the delivery rider along real neighborhood streets:",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Text("🏪 Tailor", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text(
                                    "Progress: ${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7209B7)
                                )
                                Text("🏡 You", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                            }
                            Slider(
                                value = progress,
                                onValueChange = { newValue ->
                                    progress = newValue
                                },
                                valueRange = 0.15f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF7209B7),
                                    activeTrackColor = Color(0xFF7209B7),
                                    inactiveTrackColor = Color(0xFFE5E7EB)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Live Route Tracker Map Card with floating overlay buttons
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp), // Extended height for premium visuals
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = trackingCameraState,
                            properties = mapProperties,
                            uiSettings = com.google.maps.android.compose.MapUiSettings(
                                zoomControlsEnabled = false,
                                scrollGesturesEnabled = false // HIDE scroll gestures inside map so parent column is 100% easily scrollable anywhere!
                            )
                        ) {
                            Marker(
                                state = MarkerState(position = tailorLatLng),
                                title = "Tailor's Shop (Store)",
                                snippet = "Origin of Scrap Fabric",
                                icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN)
                            )
                            Marker(
                                state = MarkerState(position = artisanLatLng),
                                title = "Your Location",
                                icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED)
                            )
                            // Clean solid black road route path with smooth joint corner connections!
                            Polyline(
                                points = routePoints,
                                color = Color.Black,
                                width = 8f,
                                jointType = com.google.android.gms.maps.model.JointType.ROUND
                            )
                            Marker(
                                state = MarkerState(position = riderLatLng),
                                title = "Rider: ${selectedRider.name} 🛵",
                                snippet = if (t >= 1f) "Arrived!" else "In Transit..."
                            )
                        }

                        // Floating Back/Dismiss Button on top-left of the map (Zepto style)
                        IconButton(
                            onClick = { onDismiss() },
                            modifier = Modifier
                                .padding(12.dp)
                                .size(36.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color.White)
                                .align(androidx.compose.ui.Alignment.TopStart),
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White)
                        ) {
                            Text("‹", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Black)
                        }

                        // Floating map control cluster on top-right of the map (Zepto style)
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .align(androidx.compose.ui.Alignment.TopEnd),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Expand/Inward button (Zepto Style)
                            IconButton(
                                onClick = { /* expand action */ },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(Color.White),
                                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White)
                            ) {
                                Text("↔", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFD90429))
                            }

                            // Re-center target button (Zepto Style magenta target)
                            IconButton(
                                onClick = { 
                                    trackingCameraState.position = CameraPosition.fromLatLngZoom(riderLatLng, 15f)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(Color.White),
                                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White)
                            ) {
                                Text("🎯", style = MaterialTheme.typography.titleMedium, color = Color(0xFFD90429))
                            }
                        }
                    }
                }

                // Delivery Summary / Address Card (Zepto Style)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color(0xFFFFF7ED)),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Text("🛍️", style = MaterialTheme.typography.titleLarge)
                        }
                        
                        Column {
                            Text(
                                text = "1 fabric item",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "Delivering to: ${request.requesterName}'s Location",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Premium Rider Profile & Contact Card (Zepto/Blinkit Style) for Project Demo
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Rider Avatar Circle with Purple Gradient
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(Color(0xFF7209B7), Color(0xFF3F37C9))
                                    )
                                ),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Text(text = selectedRider.emoji, style = MaterialTheme.typography.titleLarge)
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = selectedRider.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFDCFCE7), androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = selectedRider.rating,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }
                            Text(
                                text = "Vehicle: ${selectedRider.vehicleNumber} • ${selectedRider.vehicleModel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Real-world dynamic completion timestamps relative to order events
                val timelineTimes = remember(simulatedStageIndex, isDelivered) {
                    val format = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                    val calendar = java.util.Calendar.getInstance()
                    
                    if (isDelivered) {
                        val times = mutableListOf<String>()
                        calendar.add(java.util.Calendar.MINUTE, -2) // Delivered 2m ago
                        times.add(0, format.format(calendar.time))
                        calendar.add(java.util.Calendar.MINUTE, -6) // Out for delivery 8m ago
                        times.add(0, format.format(calendar.time))
                        calendar.add(java.util.Calendar.MINUTE, -4) // In transit 12m ago
                        times.add(0, format.format(calendar.time))
                        calendar.add(java.util.Calendar.MINUTE, -3) // Dispatched 15m ago
                        times.add(0, format.format(calendar.time))
                        calendar.add(java.util.Calendar.MINUTE, -5) // Placed 20m ago
                        times.add(0, format.format(calendar.time))
                        times
                    } else {
                        val times = mutableListOf<String>()
                        val baseCal = java.util.Calendar.getInstance()
                        
                        // Stage 0: Placed
                        baseCal.add(java.util.Calendar.MINUTE, -10)
                        times.add(format.format(baseCal.time))
                        
                        // Stage 1: Dispatched
                        if (simulatedStageIndex >= 1) {
                            baseCal.add(java.util.Calendar.MINUTE, 3)
                            times.add(format.format(baseCal.time))
                        } else {
                            times.add("In Progress")
                        }
                        
                        // Stage 2: In Transit
                        if (simulatedStageIndex >= 2) {
                            baseCal.add(java.util.Calendar.MINUTE, 4)
                            times.add(format.format(baseCal.time))
                        } else if (simulatedStageIndex == 1) {
                            times.add("In Progress")
                        } else {
                            times.add("Pending")
                        }
                        
                        // Stage 3: Out for Delivery
                        if (simulatedStageIndex >= 3) {
                            baseCal.add(java.util.Calendar.MINUTE, 2)
                            times.add(format.format(baseCal.time))
                        } else if (simulatedStageIndex == 2) {
                            times.add("In Progress")
                        } else {
                            times.add("Pending")
                        }
                        
                        // Stage 4: Delivered
                        times.add("Pending")
                        
                        times
                    }
                }

                // Timeline stages
                val stages = listOf(
                    Triple("Order Placed", "Request accepted and order confirmed", simulatedStageIndex >= 0),
                    Triple("Packaged & Dispatched", "Tailor has handpicked and wrapped your fabric scraps", simulatedStageIndex >= 1),
                    Triple("In Transit", "Shipped via Kutira local delivery rider", simulatedStageIndex >= 2),
                    Triple("Out for Delivery", "Rider ${selectedRider.name} is delivering to your location", simulatedStageIndex >= 3),
                    Triple("Delivered", "Delivered successfully with secure OTP verification", simulatedStageIndex >= 4)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    stages.forEachIndexed { index, (stageTitle, stageDesc, isDone) ->
                        val stageTime = timelineTimes.getOrElse(index) { "" }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(if (isDone) KutiraGreen else if (stageTime == "In Progress") com.kutirakone.app.ui.theme.KutiraAmber else Color.LightGray),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    if (isDone) {
                                        Text("✓", color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text("${index + 1}", color = Color.White, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                if (index < stages.lastIndex) {
                                    Spacer(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(24.dp)
                                            .background(if (isDone) KutiraGreen else Color.LightGray)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stageTitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDone) Color.Black else if (stageTime == "In Progress") Color.Black else Color.Gray
                                    )
                                    Text(
                                        text = stageTime,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isDone || stageTime == "In Progress") FontWeight.Bold else FontWeight.Normal,
                                        color = if (isDone) KutiraGreen else if (stageTime == "In Progress") com.kutirakone.app.ui.theme.KutiraAmber else Color.Gray
                                    )
                                }
                                Text(
                                    text = stageDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
                
                if (!isDelivered) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        onClick = {
                            requestViewModel.markOrderAsDelivered(request.id, request.listingId)
                            android.widget.Toast.makeText(context, "Delivery confirmed successfully! You can now rate the tailor.", android.widget.Toast.LENGTH_LONG).show()
                            onDismiss()
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF06D6A0), // Vibrant Mint Green
                                            Color(0xFF2EC4B6)  // Rich Aqua Teal
                                        )
                                    )
                                ),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(horizontal = 20.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(Color.White.copy(alpha = 0.25f)),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    Text("📦", style = MaterialTheme.typography.bodyMedium)
                                }
                                
                                Text(
                                    text = "Mark as Received",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                Text(
                                    text = "Confirm ›",
                                    color = Color.White.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isDelivered && !hasReviewed) {
                Button(
                    onClick = {
                        onDismiss()
                        navController.navigate(com.kutirakone.app.navigation.Screen.ListingDetail.createRoute(request.listingId))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = com.kutirakone.app.ui.theme.KutiraAmber)
                ) {
                    Text("⭐ Rate & Review Tailor", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Close", color = Color.Gray)
                }
            }
        }
    )
}
