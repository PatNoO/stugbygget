package com.example.stugbygget.feature.todos.ui

import com.example.stugbygget.domain.model.TodoItem

data class TodosUiState(
    val isLoading: Boolean = true,
    val todos: List<TodoItem> = emptyList(),
    val selectedPhase: String? = null,
    val selectedAssignee: String? = null,
    val availablePhases: List<String> = emptyList(),
    val availableAssignees: List<String> = emptyList(),
    val errorMessage: String? = null
)
