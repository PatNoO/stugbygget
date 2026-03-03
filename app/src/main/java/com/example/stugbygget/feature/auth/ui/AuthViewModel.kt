package com.example.stugbygget.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stugbygget.domain.usecase.ObserveAuthUserUseCase
import com.example.stugbygget.domain.usecase.SignInWithEmailPasswordUseCase
import com.example.stugbygget.domain.usecase.SignOutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    observeAuthUserUseCase: ObserveAuthUserUseCase,
    private val signInWithEmailPasswordUseCase: SignInWithEmailPasswordUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeAuthUserUseCase()
                .catch { throwable ->
                    _uiState.update { it.copy(errorMessage = throwable.message) }
                }
                .collect { user ->
                    _uiState.update { it.copy(currentUser = user, isLoading = false, errorMessage = null) }
                }
        }
    }

    fun onEmailPasswordSignIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email and password are required.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { signInWithEmailPasswordUseCase(email, password) }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Could not sign in."
                        )
                    }
                }
        }
    }

    // Temporary compatibility method until SB45 removes Google login UI flow.
    fun onGoogleTokenReceived(idToken: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = "Google sign-in is no longer supported. Use email and password."
            )
        }
    }

    fun onGoogleSignInFailed(message: String) {
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
    }

    fun signOut() {
        viewModelScope.launch {
            runCatching { signOutUseCase() }
                .onFailure { throwable ->
                    _uiState.update { it.copy(errorMessage = throwable.message ?: "Kunde inte logga ut") }
                }
        }
    }
}
