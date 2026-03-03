package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.repository.ArSessionRepository

class StopArSessionUseCase(
    private val repository: ArSessionRepository
) {
    operator fun invoke() = repository.stopSession()
}
