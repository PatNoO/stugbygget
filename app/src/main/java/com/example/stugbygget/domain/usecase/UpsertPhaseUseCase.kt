package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.RenovationPhase
import com.example.stugbygget.domain.repository.PhaseRepository

class UpsertPhaseUseCase(
    private val repository: PhaseRepository
) {
    suspend operator fun invoke(projectId: String, phase: RenovationPhase) =
        repository.upsertPhase(projectId, phase)
}
