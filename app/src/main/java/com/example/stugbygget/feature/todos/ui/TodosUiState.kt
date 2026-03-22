package com.example.stugbygget.feature.todos.ui

import com.example.stugbygget.domain.model.TodoItem
import com.example.stugbygget.domain.model.TodoPriority

data class TodosUiState(
    val isLoading: Boolean = true,
    val todos: List<TodoItem> = emptyList(),
    val selectedPhase: String? = null,
    val selectedAssignee: String? = null,
    val availablePhases: List<String> = emptyList(),
    val availableAssignees: List<String> = emptyList(),
    val errorMessage: String? = null,
    // Add/edit-todo sheet state
    val showAddSheet: Boolean = false,
    val editingTodo: TodoItem? = null,
    val draftText: String = "",
    val draftAssignee: String = "",
    val draftPriority: TodoPriority = TodoPriority.MEDIUM,
    val draftPhaseId: String = "",
    val isAddingTodo: Boolean = false,
    val addError: String? = null,
    // Detail popup
    val viewingTodo: TodoItem? = null,
)
