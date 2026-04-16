package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.repository.BudgetRepository

class SavePhaseBudgetUseCase(private val repository: BudgetRepository) {
    suspend operator fun invoke(projectId: String, phaseId: String, budgeted: Double) =
        repository.savePhaseBudget(projectId, phaseId, budgeted)
}
