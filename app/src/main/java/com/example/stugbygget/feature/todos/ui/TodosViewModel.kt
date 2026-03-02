package com.example.stugbygget.feature.todos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stugbygget.domain.usecase.ObserveTodosUseCase
import com.example.stugbygget.domain.usecase.ToggleTodoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TodosViewModel(
    private val observeTodosUseCase: ObserveTodosUseCase,
    private val toggleTodoUseCase: ToggleTodoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodosUiState())
    val uiState: StateFlow<TodosUiState> = _uiState.asStateFlow()

    init {
        observeTodos()
    }

    fun onPhaseFilterSelected(phase: String?) {
        _uiState.update { it.copy(selectedPhase = phase) }
        observeTodos()
    }

    fun onAssigneeFilterSelected(assignee: String?) {
        _uiState.update { it.copy(selectedAssignee = assignee) }
        observeTodos()
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

    private fun observeTodos() {
        viewModelScope.launch {
            observeTodosUseCase(
                projectId = DEFAULT_PROJECT_ID,
                phaseId = _uiState.value.selectedPhase,
                assignee = _uiState.value.selectedAssignee
            ).catch { throwable ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = throwable.message ?: "Kunde inte ladda todos")
                }
            }.collect { todos ->
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
