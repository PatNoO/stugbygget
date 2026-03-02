package com.example.stugbygget.domain.repository

import com.example.stugbygget.domain.model.BudgetOverview
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun observeBudget(projectId: String): Flow<BudgetOverview>
}
