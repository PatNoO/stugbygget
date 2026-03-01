package com.example.stugbygget.feature.auth.ui

import com.example.stugbygget.domain.model.AppUser

data class AuthUiState(
    val isLoading: Boolean = false,
    val currentUser: AppUser? = null,
    val errorMessage: String? = null
)
