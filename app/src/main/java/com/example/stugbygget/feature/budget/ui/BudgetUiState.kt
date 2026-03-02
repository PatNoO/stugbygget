package com.example.stugbygget.feature.budget.ui

import com.example.stugbygget.domain.model.BudgetOverview

data class BudgetUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val overview: BudgetOverview? = null,
    val overspentPhaseIds: Set<String> = emptySet()
)
