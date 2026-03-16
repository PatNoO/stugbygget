package com.example.stugbygget.feature.planning.ui

import com.example.stugbygget.domain.model.RenovationPhase

data class PlanningUiState(
    val isLoading: Boolean = true,
    val phases: List<RenovationPhase> = emptyList(),
    val totalProgressPercent: Int = 0,
    val daysLeft: Long = 0L,
    val isSchedulePassed: Boolean = false,
    val errorMessage: String? = null,
    // Add-phase sheet state
    val showAddSheet: Boolean = false,
    val draftName: String = "",
    val draftRoom: String = "",
    val draftStartDate: String = "",
    val draftEndDate: String = "",
    val draftColor: String = "#8B2E16",
    val draftIcon: String = "🔧",
    val isAddingPhase: Boolean = false,
    val addError: String? = null,
)
