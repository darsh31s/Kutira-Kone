package com.kutirakone.app.repository

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.kutirakone.app.ui.common.utils.UiState
import com.kutirakone.app.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun getFakeEmail(username: String): String {
        return "${username.trim().lowercase()}@kutirakone.com"
    }

    fun login(username: String, password: String): Flow<UiState<FirebaseUser>> = flow {
        emit(UiState.Loading)
        try {
            val email = getFakeEmail(username)
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                emit(UiState.Success(user))
            } else {
                emit(UiState.Error("Sign in failed"))
            }
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Invalid username or password"))
        }
    }

    fun register(username: String, password: String): Flow<UiState<FirebaseUser>> = flow {
        emit(UiState.Loading)
        try {
            val email = getFakeEmail(username)
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                emit(UiState.Success(user))
            } else {
                emit(UiState.Error("Registration failed"))
            }
        } catch (e: Exception) {
            if (e is com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                try {
                    val email = getFakeEmail(username)
                    val loginResult = auth.signInWithEmailAndPassword(email, password).await()
                    val user = loginResult.user
                    if (user != null) {
                        val doc = firestore.collection(Constants.USERS_COLLECTION).document(user.uid).get().await()
                        if (!doc.exists()) {
                            // The user exists in Auth but was deleted from Firestore database.
                            // We can re-use the Auth user and re-create their Firestore profile!
                            emit(UiState.Success(user))
                        } else {
                            emit(UiState.Error("Username is already taken. Please choose another one."))
                        }
                    } else {
                        emit(UiState.Error("Username is already taken. Please choose another one."))
                    }
                } catch (loginEx: Exception) {
                    emit(UiState.Error("Username is already taken. Please choose another one."))
                }
            } else {
                emit(UiState.Error(e.message ?: "Registration failed. Username might be taken."))
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun updateFcmToken(uid: String, token: String) {
        try {
            firestore.collection(Constants.USERS_COLLECTION).document(uid).update("fcmToken", token).await()
        } catch (e: Exception) {
            // Ignore error if token update fails
        }
    }
}
