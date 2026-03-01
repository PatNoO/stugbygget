package com.example.stugbygget.domain.repository

import com.example.stugbygget.domain.model.AppUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeCurrentUser(): Flow<AppUser?>
    suspend fun signInWithGoogleIdToken(idToken: String)
    suspend fun signOut()
}
