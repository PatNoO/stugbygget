package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.TodoItem
import com.example.stugbygget.domain.repository.TodoRepository

class UpsertTodoUseCase(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(projectId: String, todo: TodoItem) =
        repository.upsertTodo(projectId, todo)
}
