package com.kutirakone.app.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.kutirakone.app.model.User
import com.kutirakone.app.ui.common.utils.UiState
import com.kutirakone.app.utils.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class UserRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    fun createUser(user: User): Flow<UiState<Unit>> = flow {
        emit(UiState.Loading)
        try {
            firestore.collection(Constants.USERS_COLLECTION).document(user.uid).set(user.toMap()).await()
            emit(UiState.Success(Unit))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to create user"))
        }
    }

    fun getUser(uid: String): Flow<UiState<User>> = callbackFlow {
        trySend(UiState.Loading)
        val listener = firestore.collection(Constants.USERS_COLLECTION).document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(UiState.Error(error.message ?: "Failed to get user"))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = User.fromDocument(snapshot)
                    if (user != null) {
                        trySend(UiState.Success(user))
                    } else {
                        trySend(UiState.Error("Failed to parse user"))
                    }
                } else {
                    trySend(UiState.Error("User not found"))
                }
            }
        awaitClose { listener.remove() }
    }

    fun updateUser(uid: String, updates: Map<String, Any>): Flow<UiState<Unit>> = flow {
        emit(UiState.Loading)
        try {
            firestore.collection(Constants.USERS_COLLECTION).document(uid).update(updates).await()
            emit(UiState.Success(Unit))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to update user"))
        }
    }

    fun userExists(uid: String): Flow<UiState<Boolean>> = flow {
        emit(UiState.Loading)
        try {
            val doc = firestore.collection(Constants.USERS_COLLECTION).document(uid).get().await()
            emit(UiState.Success(doc.exists()))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to check user existence"))
        }
    }

    suspend fun isNameUnique(name: String, excludeUid: String? = null): Boolean {
        return try {
            val querySnapshot = firestore.collection(Constants.USERS_COLLECTION)
                .whereEqualTo("name", name)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                true // No one has this name
            } else {
                if (excludeUid != null) {
                    // It's not unique if the matching document belongs to someone else
                    querySnapshot.documents.none { it.id != excludeUid }
                } else {
                    false // Someone has this name, and we aren't excluding anyone
                }
            }
        } catch (e: Exception) {
            false // If the query fails, fail safe by assuming it's not unique
        }
    }

    fun updateAvgRating(vendorId: String): Flow<UiState<Unit>> = flow {
        emit(UiState.Loading)
        try {
            val reviews = firestore.collection(Constants.REVIEWS_COLLECTION)
                .whereEqualTo("vendorId", vendorId)
                .get()
                .await()
            
            if (!reviews.isEmpty) {
                var totalRating = 0.0
                reviews.forEach { doc ->
                    totalRating += doc.getLong("rating")?.toDouble() ?: 0.0
                }
                val avg = totalRating / reviews.size()
                firestore.collection(Constants.USERS_COLLECTION).document(vendorId).update("avgRating", avg).await()
            }
            emit(UiState.Success(Unit))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to update rating"))
        }
    }
}
