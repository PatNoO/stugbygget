package com.example.stugbygget.domain.repository

import com.example.stugbygget.domain.model.BudgetOverview
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun observeBudget(projectId: String): Flow<BudgetOverview>
    suspend fun setTotalBudget(projectId: String, totalBudget: Double)
    suspend fun savePhaseBudget(projectId: String, phaseId: String, budgeted: Double)
    suspend fun addExpense(projectId: String, phaseId: String, category: String, amount: Double)
}
