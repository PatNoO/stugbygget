package com.example.stugbygget.feature.planning.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stugbygget.domain.model.RenovationPhase
import com.example.stugbygget.domain.usecase.BuildPlanningOverviewUseCase
import com.example.stugbygget.domain.usecase.DeletePhaseUseCase
import com.example.stugbygget.domain.usecase.ObservePhasesUseCase
import com.example.stugbygget.domain.usecase.UpsertPhaseUseCase
import java.time.Instant
import java.time.ZoneOffset
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
        _uiState.update {
            it.copy(
                showAddSheet = true,
                editingPhase = null,
                draftName = "",
                draftRoom = "",
                draftDescription = "",
                draftStartMillis = null,
                draftEndMillis = null,
                addError = null,
            )
        }
    }

    fun onShowEditSheet(phase: RenovationPhase) {
        _uiState.update {
            it.copy(
                showAddSheet = true,
                editingPhase = phase,
                draftName = phase.name,
                draftRoom = phase.room,
                draftDescription = phase.description,
                draftStartMillis = phase.startDate.toEpochMilli(),
                draftEndMillis = phase.endDate.toEpochMilli(),
                addError = null,
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
    fun onDraftDescriptionChanged(value: String) = _uiState.update { it.copy(draftDescription = value) }
    fun onDraftStartMillisChanged(millis: Long) = _uiState.update { it.copy(draftStartMillis = millis, addError = null) }
    fun onDraftEndMillisChanged(millis: Long) = _uiState.update { it.copy(draftEndMillis = millis, addError = null) }

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
        val startMillis = state.draftStartMillis
        val endMillis = state.draftEndMillis
        if (startMillis == null || endMillis == null) {
            _uiState.update { it.copy(addError = "Both dates are required.") }
            return
        }
        val startInstant = Instant.ofEpochMilli(startMillis)
        val endInstant = Instant.ofEpochMilli(endMillis)
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
                description = state.draftDescription.trim(),
                startDate = startInstant,
                endDate = endInstant,
                progress = state.editingPhase?.progress ?: 0,
                color = state.editingPhase?.color ?: "#8B2E16",
                icon = state.editingPhase?.icon ?: "🔧",
            )
            runCatching { upsertPhaseUseCase(_projectId, phase) }
                .onSuccess { _uiState.update { it.copy(isAddingPhase = false, showAddSheet = false, editingPhase = null) } }
                .onFailure { e -> _uiState.update { it.copy(isAddingPhase = false, addError = e.message ?: "Failed to save phase.") } }
        }
    }
}
