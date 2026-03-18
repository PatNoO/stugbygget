package com.example.stugbygget.feature.todos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stugbygget.domain.model.TodoItem
import com.example.stugbygget.domain.model.TodoPriority
import com.example.stugbygget.domain.usecase.ObserveTodosUseCase
import com.example.stugbygget.domain.usecase.ToggleTodoUseCase
import com.example.stugbygget.domain.usecase.UpsertTodoUseCase
import java.time.Instant
import java.util.UUID
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
    private val toggleTodoUseCase: ToggleTodoUseCase,
    private val upsertTodoUseCase: UpsertTodoUseCase,
    private val projectId: String
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

    fun onShowAddSheet() {
        _uiState.update { it.copy(showAddSheet = true, editingTodo = null, draftText = "", draftAssignee = "", draftPriority = TodoPriority.MEDIUM, draftPhaseId = "", addError = null) }
    }

    fun onShowEditSheet(todo: TodoItem) {
        _uiState.update {
            it.copy(
                showAddSheet = true,
                editingTodo = todo,
                draftText = todo.text,
                draftAssignee = todo.assignee,
                draftPriority = todo.priority,
                draftPhaseId = todo.phaseId,
                addError = null
            )
        }
    }

    fun onDismissAddSheet() {
        _uiState.update { it.copy(showAddSheet = false, editingTodo = null, addError = null) }
    }

    fun onDraftTextChanged(text: String) = _uiState.update { it.copy(draftText = text, addError = null) }
    fun onDraftAssigneeChanged(value: String) = _uiState.update { it.copy(draftAssignee = value) }
    fun onDraftPriorityChanged(priority: TodoPriority) = _uiState.update { it.copy(draftPriority = priority) }
    fun onDraftPhaseIdChanged(value: String) = _uiState.update { it.copy(draftPhaseId = value) }

    fun onSubmitTodo() {
        val state = _uiState.value
        if (state.draftText.isBlank()) {
            _uiState.update { it.copy(addError = "Task description is required.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingTodo = true, addError = null) }
            val existing = state.editingTodo
            val now = Instant.now()
            val todo = TodoItem(
                id = existing?.id ?: UUID.randomUUID().toString(),
                text = state.draftText.trim(),
                done = existing?.done ?: false,
                phaseId = state.draftPhaseId.trim(),
                assignee = state.draftAssignee.trim(),
                priority = state.draftPriority,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            )
            runCatching { upsertTodoUseCase(projectId, todo) }
                .onSuccess { _uiState.update { it.copy(isAddingTodo = false, showAddSheet = false, editingTodo = null) } }
                .onFailure { e -> _uiState.update { it.copy(isAddingTodo = false, addError = e.message ?: "Failed to save todo.") } }
        }
    }

    fun onTodoToggle(todoId: String, checked: Boolean) {
        viewModelScope.launch {
            runCatching {
                toggleTodoUseCase(projectId, todoId, checked)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.message ?: "Failed to update todo.")
                }
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
                        projectId = projectId,
                        phaseId = filters.phaseId,
                        assignee = filters.assignee
                    )
                }
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Failed to load todos."
                        )
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
}
