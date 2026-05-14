package com.kutirakone.app.ui.vendor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.firebase.firestore.GeoPoint
import com.kutirakone.app.model.Listing
import com.kutirakone.app.model.enums.ListingType
import com.kutirakone.app.model.enums.MaterialType
import com.kutirakone.app.ui.common.components.ImagePickerButton
import com.kutirakone.app.ui.common.utils.UiState
import com.kutirakone.app.ui.common.utils.showToast
import com.kutirakone.app.ui.theme.KutiraGreen
import com.kutirakone.app.utils.GeoHashUtils
import com.kutirakone.app.utils.LocationUtils
import com.kutirakone.app.viewmodel.AuthViewModel
import com.kutirakone.app.viewmodel.ListingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    navController: NavController,
    listingViewModel: ListingViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val user = authViewModel.getCurrentUser()
    val uploadState by listingViewModel.uploadState.collectAsState()
    val selectedImages by listingViewModel.selectedImages.collectAsState()
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var material by remember { mutableStateOf<MaterialType?>(null) }
    var size by remember { mutableStateOf("") }
    var colour by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("New") }
    var listingType by remember { mutableStateOf(ListingType.SELL) }
    var price by remember { mutableStateOf("") }
    var swapOffer by remember { mutableStateOf("") }
    var location by remember { mutableStateOf<GeoPoint?>(null) }
    var locationName by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var addressSuggestions by remember { mutableStateOf<List<Pair<String, Pair<Double, Double>>>>(emptyList()) }
    var isSearchingAddress by remember { mutableStateOf(false) }
    var showMapPickerDialog by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 2 && searchQuery != locationName) {
            isSearchingAddress = true
            kotlinx.coroutines.delay(500)
            addressSuggestions = LocationUtils.getCoordinatesFromAddress(context, searchQuery)
            isSearchingAddress = false
        } else {
            addressSuggestions = emptyList()
        }
    }

    LaunchedEffect(uploadState) {
        if (uploadState is UiState.Success) {
            context.showToast("Listing published successfully!")
            listingViewModel.resetUploadState()
            navController.popBackStack()
        } else if (uploadState is UiState.Error) {
            context.showToast((uploadState as UiState.Error).message)
            listingViewModel.clearUploadError() // reset error state so they can retry, but keep selected images!
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("List a Fabric Scrap") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Photos Section
            ImagePickerButton(
                onImagePicked = { uri -> uri?.let { listingViewModel.addImage(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            
            Text("${selectedImages.size} photos selected", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))

            // Material
            Text("Material", style = MaterialTheme.typography.labelLarge, modifier = Modifier.fillMaxWidth())
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                MaterialType.values().forEach { m ->
                    FilterChip(
                        selected = material == m,
                        onClick = { material = m },
                        label = { Text(m.displayName) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Size & Colour
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = size,
                    onValueChange = { size = it },
                    label = { Text("Size (metres)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = colour,
                    onValueChange = { colour = it },
                    label = { Text("Colour") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Type
            Text("Listing Type", style = MaterialTheme.typography.labelLarge, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ListingType.values().forEach { t ->
                    FilterChip(
                        selected = listingType == t,
                        onClick = { listingType = t },
                        label = { Text(t.name) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (listingType == ListingType.SELL) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (listingType == ListingType.SWAP) {
                OutlinedTextField(
                    value = swapOffer,
                    onValueChange = { swapOffer = it },
                    label = { Text("What do you want in exchange?") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Listing Location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search address...") },
                            placeholder = { Text("e.g. MG Road, Bengaluru") },
                            modifier = Modifier.weight(1f),
                            trailingIcon = {
                                if (isSearchingAddress) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    IconButton(onClick = {
                                        coroutineScope.launch {
                                            val loc = LocationUtils.getCurrentLocation(context)
                                            if (loc != null) {
                                                location = GeoPoint(loc.first, loc.second)
                                                locationName = LocationUtils.getAddressFromCoordinates(context, loc.first, loc.second)
                                                searchQuery = locationName
                                                context.showToast("Current location loaded!")
                                            } else {
                                                context.showToast("Could not retrieve GPS location")
                                            }
                                        }
                                    }) {
                                        Text("📍", style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }
                        )
                        
                        Button(
                            onClick = { showMapPickerDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = KutiraGreen),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("🗺️ Map", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    
                    if (addressSuggestions.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column {
                                addressSuggestions.forEach { (name, coords) ->
                                    DropdownMenuItem(
                                        text = { Text(name) },
                                        onClick = {
                                            location = GeoPoint(coords.first, coords.second)
                                            locationName = name
                                            searchQuery = name
                                            addressSuggestions = emptyList()
                                        }
                                    )
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                    
                    if (location != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = KutiraGreen.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("📍", style = MaterialTheme.typography.titleMedium)
                                Column {
                                    Text("Selected Location:", style = MaterialTheme.typography.labelMedium, color = KutiraGreen, fontWeight = FontWeight.Bold)
                                    Text(locationName, modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (user != null && material != null && size.isNotEmpty() && colour.isNotEmpty() && location != null) {
                        val sizeDbl = size.toDoubleOrNull() ?: 0.0
                        val priceDbl = if (listingType == ListingType.SELL) price.toDoubleOrNull() else null
                        
                        val listingId = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection(com.kutirakone.app.utils.Constants.LISTINGS_COLLECTION).document().id
                        
                        val newListing = Listing(
                            id = listingId,
                            userId = user.uid,
                            userName = user.displayName ?: "Vendor",
                            material = material!!,
                            sizeMetres = sizeDbl,
                            colour = colour,
                            condition = condition,
                            type = listingType,
                            price = priceDbl,
                            swapOffer = swapOffer,
                            location = location,
                            geoHash = GeoHashUtils.encodeGeoHash(location!!.latitude, location!!.longitude),
                            photoURLs = listOf("mock_url") // Mock until actual upload logic is fully implemented with real context
                        )
                        listingViewModel.uploadListing(context, newListing, selectedImages)
                    } else {
                        context.showToast("Please fill all fields and add photo/location")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = KutiraGreen),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                if (uploadState is UiState.Loading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text("Publish Listing", color = Color.White)
                }
            }
        }
    }

    if (showMapPickerDialog) {
        MapPickerDialog(
            initialLocation = location,
            onLocationSelected = { loc, address ->
                location = loc
                locationName = address
                searchQuery = address
            },
            onDismiss = { showMapPickerDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerDialog(
    initialLocation: GeoPoint?,
    onLocationSelected: (GeoPoint, String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var selectedLatLng by remember { mutableStateOf(initialLocation?.let { LatLng(it.latitude, it.longitude) } ?: LatLng(20.5937, 78.9629)) }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLatLng, if (initialLocation != null) 15f else 5f)
    }

    LaunchedEffect(Unit) {
        if (initialLocation == null) {
            val loc = LocationUtils.getCurrentLocation(context)
            if (loc != null) {
                selectedLatLng = LatLng(loc.first, loc.second)
                cameraPositionState.position = CameraPosition.fromLatLngZoom(selectedLatLng, 15f)
            }
        }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text("Select Location on Map", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
                
                Box(modifier = Modifier.weight(1f)) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { latLng ->
                            selectedLatLng = latLng
                        }
                    ) {
                        Marker(
                            state = MarkerState(position = selectedLatLng),
                            title = "Selected Listing Position",
                            draggable = true
                        )
                    }
                    
                    Text(
                        "Tap anywhere on the map to place your shop/listing pin 📍",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        modifier = Modifier
                            .align(androidx.compose.ui.Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                            .background(Color.Black.copy(alpha = 0.7f), androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val loc = LocationUtils.getCurrentLocation(context)
                                if (loc != null) {
                                    selectedLatLng = LatLng(loc.first, loc.second)
                                    cameraPositionState.position = CameraPosition.fromLatLngZoom(selectedLatLng, 15f)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("📍 GPS Loc", color = Color.Black)
                    }
                    
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val addressName = LocationUtils.getAddressFromCoordinates(context, selectedLatLng.latitude, selectedLatLng.longitude)
                                onLocationSelected(GeoPoint(selectedLatLng.latitude, selectedLatLng.longitude), addressName)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = com.kutirakone.app.ui.theme.KutiraGreen),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text("Confirm Location", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
