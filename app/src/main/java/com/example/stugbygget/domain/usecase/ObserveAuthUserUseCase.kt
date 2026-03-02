package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.repository.AuthRepository

class ObserveAuthUserUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke() = authRepository.observeCurrentUser()
}
