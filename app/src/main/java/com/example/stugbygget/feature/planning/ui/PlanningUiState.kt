package com.example.stugbygget.feature.planning.ui

import com.example.stugbygget.domain.model.RenovationPhase

data class PlanningUiState(
    val isLoading: Boolean = true,
    val phases: List<RenovationPhase> = emptyList(),
    val totalProgressPercent: Int = 0,
    val daysLeft: Long = 0L,
    val isSchedulePassed: Boolean = false,
    val errorMessage: String? = null,
    // Add/edit-phase sheet state
    val showAddSheet: Boolean = false,
    val editingPhase: RenovationPhase? = null,
    val draftName: String = "",
    val draftRoom: String = "",
    val draftDescription: String = "",
    val draftStartMillis: Long? = null,
    val draftEndMillis: Long? = null,
    val isAddingPhase: Boolean = false,
    val addError: String? = null,
    // Delete state
    val pendingDeleteId: String? = null,
    val isDeleting: Boolean = false,
)
