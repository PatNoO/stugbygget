package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.repository.TodoRepository

class ToggleTodoUseCase(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(projectId: String, todoId: String, done: Boolean) =
        repository.toggleTodo(projectId, todoId, done)
}
