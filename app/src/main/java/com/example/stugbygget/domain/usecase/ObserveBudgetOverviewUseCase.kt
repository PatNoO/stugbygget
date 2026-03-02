package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.BudgetOverview
import com.example.stugbygget.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow

class ObserveBudgetOverviewUseCase(
    private val repository: BudgetRepository
) {
    operator fun invoke(projectId: String): Flow<BudgetOverview> = repository.observeBudget(projectId)
}
