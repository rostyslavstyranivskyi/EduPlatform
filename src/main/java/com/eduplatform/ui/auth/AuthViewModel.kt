package com.eduplatform.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduplatform.data.api.TokenStorage
import com.eduplatform.data.models.LoginRequest
import com.eduplatform.data.models.RegisterRequest
import com.eduplatform.data.repository.EduRepository
import com.eduplatform.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: EduRepository,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    val userRole: StateFlow<String?> = tokenStorage.userRole.stateIn(
        viewModelScope, SharingStarted.Eagerly, null
    )
    val accessToken: StateFlow<String?> = tokenStorage.accessToken.stateIn(
        viewModelScope, SharingStarted.Eagerly, null
    )
    val userName: StateFlow<String?> = tokenStorage.userName.stateIn(
        viewModelScope, SharingStarted.Eagerly, null
    )
    val userId: StateFlow<String?> = tokenStorage.userId.stateIn(
        viewModelScope, SharingStarted.Eagerly, null
    )

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)
            when (val result = repository.login(LoginRequest(email.trim(), password))) {
                is Result.Success -> _state.value = AuthState(success = true)
                is Result.Error -> _state.value = AuthState(error = result.message)
                else -> {}
            }
        }
    }

    fun register(name: String, surname: String, email: String, password: String, role: String) {
        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)
            val req = RegisterRequest(name.trim(), surname.trim(), email.trim(), password, role)
            when (val result = repository.register(req)) {
                is Result.Success -> _state.value = AuthState(success = true)
                is Result.Error -> _state.value = AuthState(error = result.message)
                else -> {}
            }
        }
    }

    // True while the app is attempting to restore session on startup.
    // UI can use this to show a splash/loading screen instead of flickering to Login.
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    /**
     * Called once on app start when the stored token is "cookie-session"
     * (i.e. the backend issued auth via httpOnly cookies, not a JWT in the body).
     * Calls POST /auth/refresh — if the refresh cookie is still valid the backend
     * returns a new access token and we stay logged in; otherwise we clear storage.
     */
    fun refreshSession() {
        viewModelScope.launch {
            _isRefreshing.value = true
            when (val result = repository.refresh()) {
                is Result.Success -> { /* tokenStorage already updated inside repository.refresh() */ }
                is Result.Error -> {
                    // Refresh cookie expired or invalid — force logout
                    repository.logout()
                }
                else -> {}
            }
            _isRefreshing.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}