package com.example.stugbygget.feature.planning.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stugbygget.domain.usecase.BuildPlanningOverviewUseCase
import com.example.stugbygget.domain.usecase.ObservePhasesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlanningViewModel(
    observePhasesUseCase: ObservePhasesUseCase,
    projectId: String
    private val buildPlanningOverviewUseCase: BuildPlanningOverviewUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanningUiState())
    val uiState: StateFlow<PlanningUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observePhasesUseCase(projectId)
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Kunde inte ladda faser"
                        )
                    }
                }
                .collect { phases ->
                    val overview = buildPlanningOverviewUseCase(phases)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            phases = phases,
                            totalProgressPercent = overview.totalProgressPercent,
                            daysLeft = overview.daysLeft,
                            isSchedulePassed = overview.isSchedulePassed,
                            errorMessage = null
                        )
                    }
                }
        }
    }
}
