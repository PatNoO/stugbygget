package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.repository.AuthRepository

class SignInWithGoogleUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String) = authRepository.signInWithGoogleIdToken(idToken)
}
