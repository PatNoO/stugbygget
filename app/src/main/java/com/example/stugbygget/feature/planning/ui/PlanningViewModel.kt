package com.example.stugbygget.feature.planning.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stugbygget.domain.model.RenovationPhase
import com.example.stugbygget.domain.usecase.BuildPlanningOverviewUseCase
import com.example.stugbygget.domain.usecase.DeletePhaseUseCase
import com.example.stugbygget.domain.usecase.ObservePhasesUseCase
import com.example.stugbygget.domain.usecase.UpsertPhaseUseCase
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeParseException
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlanningViewModel(
    observePhasesUseCase: ObservePhasesUseCase,
    projectId: String,
    private val buildPlanningOverviewUseCase: BuildPlanningOverviewUseCase,
    private val upsertPhaseUseCase: UpsertPhaseUseCase,
    private val deletePhaseUseCase: DeletePhaseUseCase,
) : ViewModel() {
    private val _projectId = projectId

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

    fun onShowAddSheet() {
        _uiState.update { it.copy(showAddSheet = true, editingPhase = null, draftName = "", draftRoom = "", draftStartDate = "", draftEndDate = "", draftColor = "#8B2E16", draftIcon = "🔧", addError = null) }
    }

    fun onShowEditSheet(phase: RenovationPhase) {
        val startStr = phase.startDate.atOffset(ZoneOffset.UTC).toLocalDate().toString()
        val endStr = phase.endDate.atOffset(ZoneOffset.UTC).toLocalDate().toString()
        _uiState.update {
            it.copy(
                showAddSheet = true,
                editingPhase = phase,
                draftName = phase.name,
                draftRoom = phase.room,
                draftStartDate = startStr,
                draftEndDate = endStr,
                draftColor = phase.color,
                draftIcon = phase.icon,
                addError = null
            )
        }
    }

    fun onDismissAddSheet() {
        _uiState.update { it.copy(showAddSheet = false, editingPhase = null, addError = null) }
    }

    fun onRequestDelete(phaseId: String) {
        _uiState.update { it.copy(pendingDeleteId = phaseId) }
    }

    fun onCancelDelete() {
        _uiState.update { it.copy(pendingDeleteId = null) }
    }

    fun onConfirmDelete() {
        val phaseId = _uiState.value.pendingDeleteId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            runCatching { deletePhaseUseCase(_projectId, phaseId) }
                .onSuccess { _uiState.update { it.copy(isDeleting = false, pendingDeleteId = null) } }
                .onFailure { e -> _uiState.update { it.copy(isDeleting = false, pendingDeleteId = null, errorMessage = e.message ?: "Failed to delete phase.") } }
        }
    }

    fun onDraftNameChanged(value: String) = _uiState.update { it.copy(draftName = value, addError = null) }
    fun onDraftRoomChanged(value: String) = _uiState.update { it.copy(draftRoom = value, addError = null) }
    fun onDraftStartDateChanged(value: String) = _uiState.update { it.copy(draftStartDate = value, addError = null) }
    fun onDraftEndDateChanged(value: String) = _uiState.update { it.copy(draftEndDate = value, addError = null) }
    fun onDraftColorChanged(value: String) = _uiState.update { it.copy(draftColor = value) }
    fun onDraftIconChanged(value: String) = _uiState.update { it.copy(draftIcon = value) }

    fun onSubmitPhase() {
        val state = _uiState.value
        if (state.draftName.isBlank()) {
            _uiState.update { it.copy(addError = "Phase name is required.") }
            return
        }
        if (state.draftRoom.isBlank()) {
            _uiState.update { it.copy(addError = "Room is required.") }
            return
        }
        val startInstant: Instant
        val endInstant: Instant
        try {
            startInstant = LocalDate.parse(state.draftStartDate.trim()).atStartOfDay().toInstant(ZoneOffset.UTC)
            endInstant = LocalDate.parse(state.draftEndDate.trim()).atStartOfDay().toInstant(ZoneOffset.UTC)
        } catch (e: DateTimeParseException) {
            _uiState.update { it.copy(addError = "Dates must be in YYYY-MM-DD format.") }
            return
        }
        if (!endInstant.isAfter(startInstant)) {
            _uiState.update { it.copy(addError = "End date must be after start date.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingPhase = true, addError = null) }
            val phase = RenovationPhase(
                id = state.editingPhase?.id ?: UUID.randomUUID().toString(),
                name = state.draftName.trim(),
                room = state.draftRoom.trim(),
                startDate = startInstant,
                endDate = endInstant,
                progress = state.editingPhase?.progress ?: 0,
                color = state.draftColor.trim().ifBlank { "#8B2E16" },
                icon = state.draftIcon.trim().ifBlank { "🔧" },
            )
            runCatching { upsertPhaseUseCase(_projectId, phase) }
                .onSuccess { _uiState.update { it.copy(isAddingPhase = false, showAddSheet = false, editingPhase = null) } }
                .onFailure { e -> _uiState.update { it.copy(isAddingPhase = false, addError = e.message ?: "Failed to save phase.") } }
        }
    }
}
