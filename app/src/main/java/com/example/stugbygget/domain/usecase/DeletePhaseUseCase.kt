package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.repository.PhaseRepository

class DeletePhaseUseCase(private val repository: PhaseRepository) {
    suspend operator fun invoke(projectId: String, phaseId: String) =
        repository.deletePhase(projectId, phaseId)
}
