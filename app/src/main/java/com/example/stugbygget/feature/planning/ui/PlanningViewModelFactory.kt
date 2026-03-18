package com.example.stugbygget.feature.planning.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stugbygget.di.AppContainer

class PlanningViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlanningViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlanningViewModel(
                observePhasesUseCase = container.observePhasesUseCase,
                projectId = container.projectSessionRepository.getProjectId(),
                buildPlanningOverviewUseCase = container.buildPlanningOverviewUseCase,
                upsertPhaseUseCase = container.upsertPhaseUseCase,
                deletePhaseUseCase = container.deletePhaseUseCase,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
