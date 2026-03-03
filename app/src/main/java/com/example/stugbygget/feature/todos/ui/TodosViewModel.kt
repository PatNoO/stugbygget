package com.example.stugbygget.feature.todos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stugbygget.domain.usecase.ObserveTodosUseCase
import com.example.stugbygget.domain.usecase.ToggleTodoUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TodosViewModel(
    private val observeTodosUseCase: ObserveTodosUseCase,
    private val toggleTodoUseCase: ToggleTodoUseCase
) : ViewModel() {

    private data class TodoFilters(
        val phaseId: String?,
        val assignee: String?
    )

    private val _uiState = MutableStateFlow(TodosUiState())
    val uiState: StateFlow<TodosUiState> = _uiState.asStateFlow()

    init {
        observeTodos()
    }

    fun onPhaseFilterSelected(phase: String?) {
        _uiState.update { it.copy(selectedPhase = phase) }
    }

    fun onAssigneeFilterSelected(assignee: String?) {
        _uiState.update { it.copy(selectedAssignee = assignee) }
    }

    fun onTodoToggle(todoId: String, checked: Boolean) {
        viewModelScope.launch {
            runCatching {
                toggleTodoUseCase(DEFAULT_PROJECT_ID, todoId, checked)
            }.onFailure { throwable ->
                _uiState.update { it.copy(errorMessage = throwable.message ?: "Kunde inte uppdatera todo") }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeTodos() {
        viewModelScope.launch {
            _uiState
                .map { state ->
                    TodoFilters(
                        phaseId = state.selectedPhase,
                        assignee = state.selectedAssignee
                    )
                }
                .distinctUntilChanged()
                .flatMapLatest { filters ->
                    observeTodosUseCase(
                        projectId = DEFAULT_PROJECT_ID,
                        phaseId = filters.phaseId,
                        assignee = filters.assignee
                    )
                }
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { throwable ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = throwable.message ?: "Failed to load todos.")
                    }
                }
                .collect { todos ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            todos = todos,
                            availablePhases = todos.map { todo -> todo.phaseId }.distinct().sorted(),
                            availableAssignees = todos.map { todo -> todo.assignee }.distinct().sorted(),
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
