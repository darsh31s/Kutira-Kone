package com.kutirakone.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kutirakone.app.model.Review
import com.kutirakone.app.repository.ReviewRepository
import com.kutirakone.app.repository.UserRepository
import com.kutirakone.app.ui.common.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val reviewRepository: ReviewRepository = ReviewRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _reviews = MutableStateFlow<UiState<List<Review>>>(UiState.Idle)
    val reviews: StateFlow<UiState<List<Review>>> = _reviews.asStateFlow()

    private val _submitState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val submitState: StateFlow<UiState<Unit>> = _submitState.asStateFlow()

    val selectedRating = MutableStateFlow(5)
    val reviewComment = MutableStateFlow("")

    private val _hasReviewed = MutableStateFlow(false)
    val hasReviewed: StateFlow<Boolean> = _hasReviewed.asStateFlow()

    fun loadReviewsForVendor(vendorId: String) {
        viewModelScope.launch {
            reviewRepository.getReviewsForVendor(vendorId).collect {
                _reviews.value = it
            }
        }
    }

    fun submitReview(vendorId: String, customerId: String, customerName: String, listingId: String) {
        if (selectedRating.value < 1) {
            _submitState.value = UiState.Error("Rating must be at least 1 star")
            return
        }

        val review = Review(
            vendorId = vendorId,
            customerId = customerId,
            customerName = customerName,
            listingId = listingId,
            rating = selectedRating.value,
            comment = reviewComment.value
        )

        viewModelScope.launch {
            reviewRepository.submitReview(review).collect { state ->
                _submitState.value = state
                if (state is UiState.Success) {
                    // Trigger rating recalculation
                    userRepository.updateAvgRating(vendorId).collect {}
                }
            }
        }
    }

    fun setRating(rating: Int) {
        selectedRating.value = rating
    }

    fun checkIfReviewed(customerId: String, listingId: String) {
        viewModelScope.launch {
            reviewRepository.hasUserReviewed(customerId, listingId).collect { state ->
                if (state is UiState.Success) {
                    _hasReviewed.value = state.data
                }
            }
        }
    }
}
