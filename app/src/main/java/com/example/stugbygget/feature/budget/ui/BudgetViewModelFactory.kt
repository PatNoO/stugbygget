package com.example.stugbygget.feature.budget.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stugbygget.di.AppContainer

class BudgetViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            return BudgetViewModel(
                observeBudgetOverviewUseCase = container.observeBudgetOverviewUseCase,
                savePhaseBudgetUseCase = container.savePhaseBudgetUseCase,
                addExpenseUseCase = container.addExpenseUseCase,
                projectId = container.projectSessionRepository.getProjectId()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
