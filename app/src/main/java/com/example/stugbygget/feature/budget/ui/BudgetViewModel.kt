package com.example.stugbygget.feature.budget.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stugbygget.domain.usecase.AddExpenseUseCase
import com.example.stugbygget.domain.usecase.ObserveBudgetOverviewUseCase
import com.example.stugbygget.domain.usecase.SavePhaseBudgetUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BudgetViewModel(
    private val observeBudgetOverviewUseCase: ObserveBudgetOverviewUseCase,
    private val savePhaseBudgetUseCase: SavePhaseBudgetUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val projectId: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeBudgetOverviewUseCase(projectId)
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

    fun onOpenAddSheet(type: BudgetAddType) {
        _uiState.update { it.copy(showAddSheet = true, addType = type, draftPhase = "", draftCategory = "MATERIALS", draftAmount = "", saveError = null) }
    }

    fun onDismissSheet() {
        _uiState.update { it.copy(showAddSheet = false, saveError = null) }
    }

    fun onAddTypeChanged(type: BudgetAddType) {
        _uiState.update { it.copy(addType = type, saveError = null) }
    }

    fun onPhaseChanged(value: String) {
        _uiState.update { it.copy(draftPhase = value) }
    }

    fun onCategoryChanged(value: String) {
        _uiState.update { it.copy(draftCategory = value) }
    }

    fun onAmountChanged(value: String) {
        _uiState.update { it.copy(draftAmount = value) }
    }

    fun onSave() {
        val state = _uiState.value
        val amount = state.draftAmount.toDoubleOrNull()
        val phase = state.draftPhase.trim()

        if (phase.isBlank()) {
            _uiState.update { it.copy(saveError = "Phase name is required.") }
            return
        }
        if (amount == null || amount <= 0.0) {
            _uiState.update { it.copy(saveError = "Enter a valid amount.") }
            return
        }

        _uiState.update { it.copy(isSaving = true, saveError = null) }
        viewModelScope.launch {
            try {
                if (state.addType == BudgetAddType.EXPENSE) {
                    addExpenseUseCase(projectId, phase, state.draftCategory, amount)
                } else {
                    savePhaseBudgetUseCase(projectId, phase, amount)
                }
                _uiState.update { it.copy(isSaving = false, showAddSheet = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Failed to save.") }
            }
        }
    }
}
