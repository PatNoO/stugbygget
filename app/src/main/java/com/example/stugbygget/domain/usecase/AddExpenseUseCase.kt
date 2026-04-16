package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.repository.BudgetRepository

class AddExpenseUseCase(private val repository: BudgetRepository) {
    suspend operator fun invoke(projectId: String, phaseId: String, category: String, amount: Double) =
        repository.addExpense(projectId, phaseId, category, amount)
}
