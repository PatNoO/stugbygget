package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.repository.ArSessionRepository

class IsArSupportedUseCase(
    private val repository: ArSessionRepository
) {
    operator fun invoke(): Boolean = repository.isSupported()
}
