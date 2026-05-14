package com.kutirakone.app.repository

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kutirakone.app.model.Listing
import com.kutirakone.app.model.enums.ListingStatus
import com.kutirakone.app.model.enums.MaterialType
import com.kutirakone.app.ui.common.utils.UiState
import com.kutirakone.app.utils.Constants
import com.kutirakone.app.utils.GeoHashUtils
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class ListingRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    fun createListing(listing: Listing): Flow<UiState<String>> = flow {
        emit(UiState.Loading)
        try {
            val docRef = if (listing.id.isNotBlank()) {
                firestore.collection(Constants.LISTINGS_COLLECTION).document(listing.id)
            } else {
                firestore.collection(Constants.LISTINGS_COLLECTION).document()
            }
            val listingWithId = listing.copy(id = docRef.id)
            docRef.set(listingWithId.toMap()).await()
            
            // Increment user listing count
            val userRef = firestore.collection(Constants.USERS_COLLECTION).document(listing.userId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val newCount = (snapshot.getLong("listingCount") ?: 0) + 1
                transaction.update(userRef, "listingCount", newCount)
            }.await()
            
            emit(UiState.Success(docRef.id))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to create listing"))
        }
    }

    fun getListingsNearby(
        lat: Double, lng: Double, radiusKm: Double,
        materialFilter: MaterialType?
    ): Flow<UiState<List<Listing>>> = flow {
        emit(UiState.Loading)
        try {
            val bounds = GeoHashUtils.getGeoHashBounds(lat, lng, radiusKm)
            val tasks: MutableList<Task<*>> = ArrayList()
            val listingsCollection = firestore.collection(Constants.LISTINGS_COLLECTION)

            for (b in bounds) {
                val query = listingsCollection
                    .orderBy("geoHash")
                    .startAt(b.startHash)
                    .endAt(b.endHash)
                
                tasks.add(query.get())
            }

            Tasks.whenAllComplete(tasks).await()

            val matchingDocs = mutableListOf<DocumentSnapshot>()
            for (task in tasks) {
                val snap = task.result as? com.google.firebase.firestore.QuerySnapshot
                if (snap != null) {
                    for (doc in snap.documents) {
                        val docLat = doc.getGeoPoint("location")?.latitude
                        val docLng = doc.getGeoPoint("location")?.longitude
                        val status = doc.getString("status")
                        val reservedFor = doc.getString("reservedFor") ?: ""
                        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        
                        val isVisible = status == ListingStatus.AVAILABLE.name || 
                                (status == ListingStatus.RESERVED.name && reservedFor == currentUserId)
                        
                        if (docLat != null && docLng != null && isVisible) {
                            val distanceInKm = GeoHashUtils.distanceBetween(lat, lng, docLat, docLng)
                            if (distanceInKm <= radiusKm) {
                                // Add distance temporarily, then we can map it
                                matchingDocs.add(doc)
                            }
                        }
                    }
                }
            }

            var listings = matchingDocs.mapNotNull { doc ->
                Listing.fromDocument(doc)?.let { listing ->
                    val distance = GeoHashUtils.distanceBetween(
                        lat, lng,
                        listing.location?.latitude ?: 0.0,
                        listing.location?.longitude ?: 0.0
                    )
                    listing.copy(distanceKm = distance)
                }
            }.filter { it.expiresAt.seconds > Timestamp.now().seconds }

            if (materialFilter != null) {
                listings = listings.filter { it.material == materialFilter }
            }
            
            // Sort by distance
            listings = listings.sortedBy { it.distanceKm }

            emit(UiState.Success(listings))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to get nearby listings"))
        }
    }

    fun getMyListings(userId: String): Flow<UiState<List<Listing>>> = callbackFlow {
        trySend(UiState.Loading)
        val listener = firestore.collection(Constants.LISTINGS_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(UiState.Error(error.message ?: "Failed to get listings"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val listings = snapshot.documents
                        .mapNotNull { Listing.fromDocument(it) }
                        .sortedByDescending { it.createdAt.seconds }
                    trySend(UiState.Success(listings))
                }
            }
        awaitClose { listener.remove() }
    }

    fun getListing(listingId: String): Flow<UiState<Listing>> = flow {
        emit(UiState.Loading)
        try {
            val doc = firestore.collection(Constants.LISTINGS_COLLECTION).document(listingId).get().await()
            val listing = Listing.fromDocument(doc)
            if (listing != null) {
                emit(UiState.Success(listing))
            } else {
                emit(UiState.Error("Listing not found"))
            }
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to get listing"))
        }
    }

    fun updateListingStatus(listingId: String, status: ListingStatus): Flow<UiState<Unit>> = flow {
        emit(UiState.Loading)
        try {
            firestore.collection(Constants.LISTINGS_COLLECTION).document(listingId)
                .update("status", status.name).await()
            emit(UiState.Success(Unit))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to update status"))
        }
    }

    fun deleteListing(listingId: String): Flow<UiState<Unit>> = flow {
        emit(UiState.Loading)
        try {
            // Should decrement user listing count but keeping simple for now
            firestore.collection(Constants.LISTINGS_COLLECTION).document(listingId).delete().await()
            emit(UiState.Success(Unit))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to delete listing"))
        }
    }

    fun renewListing(listingId: String): Flow<UiState<Unit>> = flow {
        emit(UiState.Loading)
        try {
            val newExpiry = Timestamp(Timestamp.now().seconds + 2592000, 0)
            firestore.collection(Constants.LISTINGS_COLLECTION).document(listingId)
                .update(mapOf("expiresAt" to newExpiry, "status" to ListingStatus.AVAILABLE.name)).await()
            emit(UiState.Success(Unit))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to renew listing"))
        }
    }

    fun expireOldListings(): Flow<UiState<Unit>> = flow {
        // Typically a cloud function, but client side check for demo
        emit(UiState.Loading)
        try {
            val now = Timestamp.now()
            val expired = firestore.collection(Constants.LISTINGS_COLLECTION)
                .whereLessThan("expiresAt", now)
                .whereEqualTo("status", ListingStatus.AVAILABLE.name)
                .get().await()
                
            val batch = firestore.batch()
            expired.documents.forEach { doc ->
                batch.update(doc.reference, "status", ListingStatus.EXPIRED.name)
            }
            batch.commit().await()
            emit(UiState.Success(Unit))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to expire listings"))
        }
    }
}
