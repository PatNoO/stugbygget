package com.example.stugbygget.feature.planning.ui

import com.example.stugbygget.domain.model.RenovationPhase

data class PlanningUiState(
    val isLoading: Boolean = true,
    val phases: List<RenovationPhase> = emptyList(),
    val errorMessage: String? = null
)
