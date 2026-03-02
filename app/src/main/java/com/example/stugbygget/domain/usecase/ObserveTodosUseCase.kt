package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.repository.TodoRepository

class ObserveTodosUseCase(
    private val repository: TodoRepository
) {
    operator fun invoke(
        projectId: String,
        phaseId: String? = null,
        assignee: String? = null
    ) = repository.observeTodos(projectId, phaseId, assignee)
}
