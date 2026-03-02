package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.repository.PhaseRepository

class ObservePhasesUseCase(
    private val phaseRepository: PhaseRepository
) {
    operator fun invoke(projectId: String) = phaseRepository.observePhases(projectId)
}
