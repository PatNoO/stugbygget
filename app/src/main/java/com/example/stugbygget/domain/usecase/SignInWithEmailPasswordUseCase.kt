package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.repository.AuthRepository

class SignInWithEmailPasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String) {
        authRepository.signInWithEmailPassword(email.trim(), password)
    }
}
