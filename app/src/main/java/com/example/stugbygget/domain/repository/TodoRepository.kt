package com.example.stugbygget.domain.repository

import com.example.stugbygget.domain.model.TodoItem
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    fun observeTodos(
        projectId: String,
        phaseId: String? = null,
        assignee: String? = null
    ): Flow<List<TodoItem>>

    suspend fun upsertTodo(projectId: String, todo: TodoItem)
    suspend fun toggleTodo(projectId: String, todoId: String, done: Boolean)
    suspend fun deleteTodo(projectId: String, todoId: String)
}
