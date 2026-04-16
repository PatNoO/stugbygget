package com.example.stugbygget.feature.budget.ui

import com.example.stugbygget.domain.model.BudgetOverview

enum class BudgetAddType { EXPENSE, PHASE_BUDGET }

data class BudgetUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val overview: BudgetOverview? = null,
    val overspentPhaseIds: Set<String> = emptySet(),
    // Add/edit sheet state
    val showAddSheet: Boolean = false,
    val addType: BudgetAddType = BudgetAddType.EXPENSE,
    val draftPhase: String = "",
    val draftCategory: String = "MATERIALS",
    val draftAmount: String = "",
    val isSaving: Boolean = false,
    val saveError: String? = null,
)
