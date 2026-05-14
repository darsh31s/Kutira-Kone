package com.kutirakone.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kutirakone.app.model.Listing
import com.kutirakone.app.model.Request
import com.kutirakone.app.model.enums.RequestStatus
import com.kutirakone.app.repository.RequestRepository
import com.kutirakone.app.ui.common.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RequestViewModel(
    private val requestRepository: RequestRepository = RequestRepository()
) : ViewModel() {

    private val _incomingRequests = MutableStateFlow<UiState<List<Request>>>(UiState.Idle)
    val incomingRequests: StateFlow<UiState<List<Request>>> = _incomingRequests.asStateFlow()

    private val _myRequests = MutableStateFlow<UiState<List<Request>>>(UiState.Idle)
    val myRequests: StateFlow<UiState<List<Request>>> = _myRequests.asStateFlow()

    private val _createRequestState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val createRequestState: StateFlow<UiState<String>> = _createRequestState.asStateFlow()

    private val _updateState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val updateState: StateFlow<UiState<Unit>> = _updateState.asStateFlow()

    fun loadIncomingRequests(ownerId: String) {
        viewModelScope.launch {
            requestRepository.getRequestsForOwner(ownerId).collect {
                _incomingRequests.value = it
            }
        }
    }

    fun loadMyRequests(requesterId: String) {
        viewModelScope.launch {
            requestRepository.getRequestsForRequester(requesterId).collect {
                _myRequests.value = it
            }
        }
    }

    fun createBuyRequest(listing: Listing, requesterId: String, requesterName: String) {
        val request = Request(
            listingId = listing.id,
            listingMaterial = listing.material.name,
            requesterId = requesterId,
            requesterName = requesterName,
            ownerId = listing.userId,
            type = "buy"
        )
        viewModelScope.launch {
            requestRepository.createRequest(request).collect {
                _createRequestState.value = it
            }
        }
    }

    fun createSwapRequest(listing: Listing, offer: String, requesterId: String, requesterName: String) {
        val request = Request(
            listingId = listing.id,
            listingMaterial = listing.material.name,
            requesterId = requesterId,
            requesterName = requesterName,
            ownerId = listing.userId,
            type = "swap",
            swapOffer = offer
        )
        viewModelScope.launch {
            requestRepository.createRequest(request).collect {
                _createRequestState.value = it
            }
        }
    }

    fun acceptRequest(request: Request, conversationId: String) {
        viewModelScope.launch {
            requestRepository.updateRequestStatus(request.id, RequestStatus.ACCEPTED, conversationId).collect {
                _updateState.value = it
            }
        }
    }

    fun declineRequest(requestId: String) {
        viewModelScope.launch {
            requestRepository.updateRequestStatus(requestId, RequestStatus.DECLINED).collect {
                _updateState.value = it
            }
        }
    }

    fun counterRequest(requestId: String, counterOffer: String) {
        viewModelScope.launch {
            // Simplification: In a full implementation we'd also update the swapOffer or have a separate field
            requestRepository.updateRequestStatus(requestId, RequestStatus.COUNTERED).collect {
                _updateState.value = it
            }
        }
    }
    
    fun resetCreateState() {
        _createRequestState.value = UiState.Idle
    }

    fun addMockRequests(ownerId: String) {
        val indianNames = listOf("Aarav Patel", "Diya Sharma", "Vihaan Singh", "Ananya Gupta", "Advik Kumar", "Rohan Verma", "Kavya Iyer")
        val fabrics = listOf("Cotton", "Silk", "Linen", "Wool", "Chiffon")
        val types = listOf("buy", "swap", "buy", "swap", "buy")
        
        viewModelScope.launch {
            indianNames.forEachIndexed { index, name ->
                val type = types.random()
                val request = Request(
                    listingId = "mock_listing_$index",
                    listingMaterial = fabrics.random(),
                    requesterId = "mock_user_$index",
                    requesterName = name,
                    ownerId = ownerId,
                    type = type,
                    swapOffer = if (type == "swap") "I will trade 2 metres of premium ${fabrics.random()} for this." else ""
                )
                requestRepository.createRequest(request).collect {}
            }
        }
    }

    fun markOrderAsDelivered(requestId: String, listingId: String) {
        viewModelScope.launch {
            requestRepository.markOrderAsDelivered(requestId, listingId).collect {
                _updateState.value = it
            }
        }
    }
}
