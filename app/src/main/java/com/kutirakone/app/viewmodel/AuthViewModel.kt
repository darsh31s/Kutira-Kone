package com.kutirakone.app.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.kutirakone.app.model.User
import com.kutirakone.app.model.enums.UserRole
import com.google.firebase.firestore.GeoPoint
import com.kutirakone.app.repository.AuthRepository
import com.kutirakone.app.repository.UserRepository
import com.kutirakone.app.ui.common.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _village = MutableStateFlow("")
    val village: StateFlow<String> = _village.asStateFlow()

    private val _selectedRole = MutableStateFlow<UserRole?>(null)
    val selectedRole: StateFlow<UserRole?> = _selectedRole.asStateFlow()

    private val _selectedLocation = MutableStateFlow<GeoPoint?>(null)
    val selectedLocation: StateFlow<GeoPoint?> = _selectedLocation.asStateFlow()

    private val _isLoginMode = MutableStateFlow(true)
    val isLoginMode: StateFlow<Boolean> = _isLoginMode.asStateFlow()

    private val _authState = MutableStateFlow<UiState<FirebaseUser>>(UiState.Idle)
    val authState: StateFlow<UiState<FirebaseUser>> = _authState.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<FirebaseUser>>(UiState.Idle)
    val uiState: StateFlow<UiState<FirebaseUser>> = _uiState.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(authRepository.isUserLoggedIn())
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    private val _currentUserRole = MutableStateFlow<UserRole?>(null)
    val currentUserRole: StateFlow<UserRole?> = _currentUserRole.asStateFlow()

    private val _userProfileState = MutableStateFlow<UiState<UserRole>>(UiState.Idle)
    val userProfileState: StateFlow<UiState<UserRole>> = _userProfileState.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<User?>(null)
    val currentUserProfile: StateFlow<User?> = _currentUserProfile.asStateFlow()

    init {
        // Check for existing user if already logged in (will be handled by MainActivity on fresh launch)
        val user = authRepository.getCurrentUser()
        if (user != null) {
            checkExistingUser(user.uid)
        }
    }

    private val _profileSaveState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val profileSaveState: StateFlow<UiState<Unit>> = _profileSaveState.asStateFlow()

    fun updateUsername(user: String) { _username.value = user }
    fun updatePhone(p: String) { _phone.value = p }
    fun updatePassword(pass: String) { _password.value = pass }
    fun updateConfirmPassword(pass: String) { _confirmPassword.value = pass }
    fun updateName(n: String) { _name.value = n }
    fun updateVillage(v: String) { _village.value = v }
    fun updateRole(role: UserRole) { _selectedRole.value = role }
    fun updateSelectedLocation(loc: GeoPoint?) { _selectedLocation.value = loc }
    fun toggleAuthMode() { _isLoginMode.value = !_isLoginMode.value }

    fun login() {
        if (_username.value.isEmpty() || _password.value.isEmpty()) return
        viewModelScope.launch {
            authRepository.login(_username.value, _password.value).collect { state ->
                _authState.value = state
                if (state is UiState.Success) {
                    checkExistingUser(state.data.uid)
                }
            }
        }
    }

    fun register() {
        if (_username.value.isEmpty() || _password.value.isEmpty() || _phone.value.isEmpty() || _name.value.isEmpty() || _village.value.isEmpty() || _selectedRole.value == null) {
            _authState.value = UiState.Error("Please fill out all fields")
            return
        }
        if (_password.value != _confirmPassword.value) {
            _authState.value = UiState.Error("Passwords do not match")
            return
        }
        viewModelScope.launch {
            _authState.value = UiState.Loading
            


            authRepository.register(_username.value, _password.value).collect { state ->
                if (state is UiState.Success) {
                    val firebaseUser = state.data
                    val newUser = User(
                        uid = firebaseUser.uid,
                        name = _name.value.trim(),
                        phone = _phone.value.trim(),
                        role = _selectedRole.value!!,
                        village = _village.value.trim(),
                        location = _selectedLocation.value
                    )
                    
                    userRepository.createUser(newUser).collect { profileState ->
                        if (profileState is UiState.Success) {
                            _currentUserProfile.value = newUser
                            _currentUserRole.value = newUser.role
                            _userProfileState.value = UiState.Success(newUser.role)
                            _isUserLoggedIn.value = true
                            _authState.value = UiState.Success(firebaseUser)
                        } else if (profileState is UiState.Error) {
                            _authState.value = UiState.Error(profileState.message)
                        }
                    }
                } else if (state is UiState.Error) {
                    _authState.value = state
                }
            }
        }
    }

    fun resetAuthState() {
        _authState.value = UiState.Idle
    }

    private fun checkExistingUser(uid: String) {
        _userProfileState.value = UiState.Loading
        viewModelScope.launch {
            userRepository.getUser(uid).collect { state ->
                if (state is UiState.Success) {
                    _currentUserProfile.value = state.data
                    _currentUserRole.value = state.data.role
                    _userProfileState.value = UiState.Success(state.data.role)
                    _isUserLoggedIn.value = true
                } else if (state is UiState.Error) {
                    _currentUserProfile.value = null
                    _currentUserRole.value = null
                    _userProfileState.value = UiState.Error("No profile")
                    
                    // The user exists in Auth but was deleted from Firestore.
                    // We delete the orphaned Auth user so they can register again completely fresh!
                    try {
                        authRepository.getCurrentUser()?.delete()?.await()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    
                    signOut()
                    _authState.value = UiState.Error("No database profile found. We've cleaned up this username; please Sign Up to register fresh!")
                }
            }
        }
    }



    fun signOut() {
        authRepository.signOut()
        _isUserLoggedIn.value = false
        _currentUserRole.value = null
        _currentUserProfile.value = null
        _uiState.value = UiState.Idle
        _authState.value = UiState.Idle
        _userProfileState.value = UiState.Idle
    }

    fun getCurrentUser(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }
}
