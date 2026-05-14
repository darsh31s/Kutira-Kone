package com.kutirakone.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kutirakone.app.model.Listing
import com.kutirakone.app.model.enums.ListingStatus
import com.kutirakone.app.model.enums.MaterialType
import com.kutirakone.app.repository.ListingRepository
import com.kutirakone.app.repository.StorageRepository
import com.kutirakone.app.ui.common.utils.UiState
import com.kutirakone.app.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ListingViewModel(
    private val listingRepository: ListingRepository = ListingRepository(),
    private val storageRepository: StorageRepository = StorageRepository()
) : ViewModel() {

    private val _nearbyListings = MutableStateFlow<UiState<List<Listing>>>(UiState.Idle)
    val nearbyListings: StateFlow<UiState<List<Listing>>> = _nearbyListings.asStateFlow()

    private val _myListings = MutableStateFlow<UiState<List<Listing>>>(UiState.Idle)
    val myListings: StateFlow<UiState<List<Listing>>> = _myListings.asStateFlow()

    private val _selectedListing = MutableStateFlow<UiState<Listing>>(UiState.Idle)
    val selectedListing: StateFlow<UiState<Listing>> = _selectedListing.asStateFlow()

    private val _uploadState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val uploadState: StateFlow<UiState<String>> = _uploadState.asStateFlow()

    val selectedMaterialFilter = MutableStateFlow<MaterialType?>(null)
    val selectedRadiusKm = MutableStateFlow(Constants.DEFAULT_RADIUS_KM)
    val selectedImages = MutableStateFlow<List<Uri>>(emptyList())

    fun loadNearbyListings(lat: Double, lng: Double) {
        viewModelScope.launch {
            listingRepository.getListingsNearby(
                lat, lng, selectedRadiusKm.value, selectedMaterialFilter.value
            ).collect {
                _nearbyListings.value = it
            }
        }
    }

    fun loadMyListings(userId: String) {
        viewModelScope.launch {
            listingRepository.getMyListings(userId).collect {
                _myListings.value = it
            }
        }
    }

    fun loadListing(listingId: String) {
        viewModelScope.launch {
            listingRepository.getListing(listingId).collect {
                _selectedListing.value = it
            }
        }
    }

    fun uploadListing(context: android.content.Context, listing: Listing, imageUris: List<Uri>) {
        if (imageUris.isEmpty()) {
            _uploadState.value = UiState.Error("At least 1 photo required")
            return
        }

        viewModelScope.launch {
            _uploadState.value = UiState.Loading
            try {
                val finalListingId = listing.id.ifBlank {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection(Constants.LISTINGS_COLLECTION).document().id
                }
                
                val uploadedUrls = mutableListOf<String>()
                var uploadFailed = false
                var errorMessage = ""
                
                for (uri in imageUris) {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    if (inputStream == null) {
                        uploadFailed = true
                        errorMessage = "Could not open image file"
                        break
                    }
                    storageRepository.uploadListingImage(context, listing.userId, inputStream, finalListingId).collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                uploadedUrls.add(state.data)
                            }
                            is UiState.Error -> {
                                uploadFailed = true
                                errorMessage = state.message
                            }
                            else -> {}
                        }
                    }
                    if (uploadFailed) break
                }
                
                if (uploadFailed) {
                    _uploadState.value = UiState.Error(errorMessage.ifBlank { "Image upload failed" })
                    return@launch
                }
                
                val finalListing = listing.copy(id = finalListingId, photoURLs = uploadedUrls)
                
                listingRepository.createListing(finalListing).collect { state ->
                    if (state is UiState.Success) {
                        _uploadState.value = UiState.Success(state.data)
                    } else if (state is UiState.Error) {
                        _uploadState.value = state
                    }
                }
            } catch (e: Exception) {
                _uploadState.value = UiState.Error(e.message ?: "Upload failed")
            }
        }
    }

    fun updateRadius(km: Double) {
        selectedRadiusKm.value = km
    }

    fun updateMaterialFilter(material: MaterialType?) {
        selectedMaterialFilter.value = material
    }

    fun updateListingStatus(listingId: String, status: ListingStatus) {
        viewModelScope.launch {
            listingRepository.updateListingStatus(listingId, status).collect {
                // If success, refresh listings internally or trust Firestore listener
            }
        }
    }

    fun deleteListing(listingId: String) {
        viewModelScope.launch {
            listingRepository.deleteListing(listingId).collect {
                // handeled by listeners typically
            }
        }
    }

    fun renewListing(listingId: String) {
        viewModelScope.launch {
            listingRepository.renewListing(listingId).collect {}
        }
    }

    fun addImage(uri: Uri) {
        if (selectedImages.value.size < Constants.MAX_LISTING_IMAGES) {
            selectedImages.value = selectedImages.value + uri
        }
    }

    fun removeImage(uri: Uri) {
        selectedImages.value = selectedImages.value - uri
    }
    
    fun resetUploadState() {
        _uploadState.value = UiState.Idle
        selectedImages.value = emptyList()
    }

    fun clearUploadError() {
        _uploadState.value = UiState.Idle
    }
}
