package com.example.stugbygget.domain.repository

import com.example.stugbygget.domain.model.AppUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeCurrentUser(): Flow<AppUser?>
    suspend fun signInWithEmailPassword(email: String, password: String)
    suspend fun signOut()
}
