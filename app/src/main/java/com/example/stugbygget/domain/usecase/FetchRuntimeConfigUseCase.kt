package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.RuntimeConfig
import com.example.stugbygget.domain.repository.RuntimeConfigRepository

class FetchRuntimeConfigUseCase(
    private val repository: RuntimeConfigRepository
) {
    suspend operator fun invoke(): RuntimeConfig = repository.fetchAndActivate()
}
