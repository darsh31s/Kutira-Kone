package com.kutirakone.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kutirakone.app.model.User
import com.kutirakone.app.repository.UserRepository
import com.kutirakone.app.ui.common.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await

class ProfileViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _userProfileState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val userProfileState: StateFlow<UiState<User>> = _userProfileState.asStateFlow()

    private val _updateProfileState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val updateProfileState: StateFlow<UiState<Unit>> = _updateProfileState.asStateFlow()

    fun loadUserProfile(uid: String) {
        viewModelScope.launch {
            userRepository.getUser(uid).collect { state ->
                _userProfileState.value = state
            }
        }
    }

    fun updateProfileAndCredentials(
        uid: String,
        name: String,
        village: String,
        location: GeoPoint?,
        newUsername: String,
        newPhone: String,
        newPassword: String?,
        newRole: com.kutirakone.app.model.enums.UserRole
    ) {
        viewModelScope.launch {
            _updateProfileState.value = UiState.Loading
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    // 1. Update Username if it changed
                    val currentUsername = firebaseUser.email?.substringBefore("@") ?: ""
                    if (newUsername.trim().lowercase() != currentUsername.trim().lowercase()) {
                        val newEmail = "${newUsername.trim().lowercase()}@kutirakone.com"
                        firebaseUser.updateEmail(newEmail).await()
                    }

                    // 2. Update Password if provided
                    if (!newPassword.isNullOrBlank()) {
                        firebaseUser.updatePassword(newPassword).await()
                    }
                }

                // 3. Update Firestore details
                val updates = mutableMapOf<String, Any>(
                    "name" to name,
                    "village" to village,
                    "phone" to newPhone.trim(),
                    "role" to newRole.name
                )
                if (location != null) {
                    updates["location"] = location
                }
                userRepository.updateUser(uid, updates).collect { state ->
                    _updateProfileState.value = state
                }
            } catch (e: com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
                _updateProfileState.value = UiState.Error("Security action requires recent login. Please log out and log back in.")
            } catch (e: Exception) {
                _updateProfileState.value = UiState.Error(e.message ?: "Failed to save profile changes")
            }
        }
    }

    fun resetUpdateState() {
        _updateProfileState.value = UiState.Idle
    }
}
