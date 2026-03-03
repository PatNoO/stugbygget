package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.repository.ArSessionRepository

class StartArSessionUseCase(
    private val repository: ArSessionRepository
) {
    operator fun invoke(): Result<Unit> = repository.startSession()
}
