package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.RuntimeConfig
import com.example.stugbygget.domain.repository.RuntimeConfigRepository

class GetRuntimeConfigUseCase(
    private val repository: RuntimeConfigRepository
) {
    operator fun invoke(): RuntimeConfig = repository.getCached()
}
