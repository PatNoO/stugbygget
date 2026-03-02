package com.example.stugbygget.feature.budget.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stugbygget.domain.usecase.ObserveBudgetOverviewUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BudgetViewModel(
    private val observeBudgetOverviewUseCase: ObserveBudgetOverviewUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeBudgetOverviewUseCase(DEFAULT_PROJECT_ID)
                .catch { throwable ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = throwable.message ?: "Failed to load budget.")
                    }
                }
                .collect { overview ->
                    val overspent = overview.phaseBudgets
                        .filter { phase -> phase.spent > phase.budgeted && phase.budgeted > 0.0 }
                        .map { it.phaseId }
                        .toSet()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            overview = overview,
                            overspentPhaseIds = overspent,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    companion object {
        private const val DEFAULT_PROJECT_ID = "default-project"
    }
}
