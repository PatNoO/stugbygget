package com.example.stugbygget.feature.planning.ui

import com.example.stugbygget.domain.model.RenovationPhase

data class PlanningUiState(
    val isLoading: Boolean = true,
    val phases: List<RenovationPhase> = emptyList(),
    val totalProgressPercent: Int = 0,
    val daysLeft: Long = 0L,
    val isSchedulePassed: Boolean = false,
    val errorMessage: String? = null
)
