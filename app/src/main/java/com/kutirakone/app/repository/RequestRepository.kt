package com.kutirakone.app.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kutirakone.app.model.Request
import com.kutirakone.app.model.enums.RequestStatus
import com.kutirakone.app.ui.common.utils.UiState
import com.kutirakone.app.utils.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class RequestRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    fun createRequest(request: Request): Flow<UiState<String>> = flow {
        emit(UiState.Loading)
        try {
            val docRef = firestore.collection(Constants.REQUESTS_COLLECTION).document()
            val requestWithId = request.copy(id = docRef.id)
            docRef.set(requestWithId.toMap()).await()
            emit(UiState.Success(docRef.id))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to create request"))
        }
    }

    fun getRequestsForOwner(ownerId: String): Flow<UiState<List<Request>>> = callbackFlow {
        trySend(UiState.Loading)
        val listener = firestore.collection(Constants.REQUESTS_COLLECTION)
            .whereEqualTo("ownerId", ownerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(UiState.Error(error.message ?: "Failed to get requests"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val requests = snapshot.documents
                        .mapNotNull { Request.fromDocument(it) }
                        .sortedByDescending { it.createdAt }
                    trySend(UiState.Success(requests))
                }
            }
        awaitClose { listener.remove() }
    }

    fun getRequestsForRequester(requesterId: String): Flow<UiState<List<Request>>> = callbackFlow {
        trySend(UiState.Loading)
        val listener = firestore.collection(Constants.REQUESTS_COLLECTION)
            .whereEqualTo("requesterId", requesterId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(UiState.Error(error.message ?: "Failed to get requests"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val requests = snapshot.documents
                        .mapNotNull { Request.fromDocument(it) }
                        .sortedByDescending { it.createdAt }
                    trySend(UiState.Success(requests))
                }
            }
        awaitClose { listener.remove() }
    }

    fun updateRequestStatus(
        requestId: String,
        status: RequestStatus,
        conversationId: String = ""
    ): Flow<UiState<Unit>> = flow {
        emit(UiState.Loading)
        try {
            val updates = mutableMapOf<String, Any>("status" to status.name)
            if (conversationId.isNotEmpty()) {
                updates["conversationId"] = conversationId
            }
            firestore.collection(Constants.REQUESTS_COLLECTION).document(requestId).update(updates).await()
            
            if (status == RequestStatus.ACCEPTED) {
                val doc = firestore.collection(Constants.REQUESTS_COLLECTION).document(requestId).get().await()
                val req = Request.fromDocument(doc)
                if (req != null) {
                    val listingUpdates = mapOf(
                        "status" to com.kutirakone.app.model.enums.ListingStatus.RESERVED.name,
                        "reservedFor" to req.requesterId
                    )
                    firestore.collection(Constants.LISTINGS_COLLECTION).document(req.listingId).update(listingUpdates).await()
                }
            }
            
            emit(UiState.Success(Unit))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to update request status"))
        }
    }

    fun getRequest(requestId: String): Flow<UiState<Request>> = flow {
        emit(UiState.Loading)
        try {
            val doc = firestore.collection(Constants.REQUESTS_COLLECTION).document(requestId).get().await()
            val request = Request.fromDocument(doc)
            if (request != null) {
                emit(UiState.Success(request))
            } else {
                emit(UiState.Error("Request not found"))
            }
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to get request"))
        }
    }

    fun markOrderAsDelivered(requestId: String, listingId: String): Flow<UiState<Unit>> = flow {
        emit(UiState.Loading)
        try {
            firestore.collection(Constants.REQUESTS_COLLECTION).document(requestId).update("status", RequestStatus.COMPLETED.name).await()
            firestore.collection(Constants.LISTINGS_COLLECTION).document(listingId).update("status", com.kutirakone.app.model.enums.ListingStatus.DELIVERED.name).await()
            emit(UiState.Success(Unit))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to mark order as delivered"))
        }
    }
}
