package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.repository.TodoRepository

class DeleteTodoUseCase(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(projectId: String, todoId: String) =
        repository.deleteTodo(projectId, todoId)
}
