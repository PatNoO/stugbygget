package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.BudgetOverview
import com.example.stugbygget.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observes the live budget overview for a renovation project.
 *
 * Emits a new [BudgetOverview] whenever Firestore data changes, enabling real-time
 * progress bars and over-budget warnings in the UI. The overview aggregates:
 * - Total budget vs. total spent across all phases
 * - Per-phase breakdown with spent, budgeted, and estimated-final amounts
 * - Per-category spend (Materials, Contractors, Transport, etc.)
 *
 * @param projectId The active renovation project ID.
 * @return A cold [Flow] that stays active and emits on every budget update.
 */
class ObserveBudgetOverviewUseCase(
    private val repository: BudgetRepository
) {
    operator fun invoke(projectId: String): Flow<BudgetOverview> = repository.observeBudget(projectId)
}
