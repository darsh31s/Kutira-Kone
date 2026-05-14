package com.kutirakone.app.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kutirakone.app.model.Review
import com.kutirakone.app.ui.common.utils.UiState
import com.kutirakone.app.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class ReviewRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    fun submitReview(review: Review): Flow<UiState<Unit>> = flow {
        emit(UiState.Loading)
        try {
            val docRef = firestore.collection(Constants.REVIEWS_COLLECTION).document()
            val reviewWithId = review.copy(id = docRef.id)
            docRef.set(reviewWithId.toMap()).await()
            emit(UiState.Success(Unit))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to submit review"))
        }
    }

    fun getReviewsForVendor(vendorId: String): Flow<UiState<List<Review>>> = flow {
        emit(UiState.Loading)
        try {
            val snapshot = firestore.collection(Constants.REVIEWS_COLLECTION)
                .whereEqualTo("vendorId", vendorId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
                
            val reviews = snapshot.documents.mapNotNull { Review.fromDocument(it) }
            emit(UiState.Success(reviews))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to get reviews"))
        }
    }

    fun hasUserReviewed(customerId: String, listingId: String): Flow<UiState<Boolean>> = flow {
        emit(UiState.Loading)
        try {
            val snapshot = firestore.collection(Constants.REVIEWS_COLLECTION)
                .whereEqualTo("customerId", customerId)
                .whereEqualTo("listingId", listingId)
                .get()
                .await()
                
            emit(UiState.Success(!snapshot.isEmpty))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to check review status"))
        }
    }
}
