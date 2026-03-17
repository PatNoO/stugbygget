package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.TodoItem
import com.example.stugbygget.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DeleteTodoUseCaseTest {

    @Test
    fun `delegates delete to repository`() = runBlocking {
        val repo = FakeTodoRepository()
        val useCase = DeleteTodoUseCase(repo)

        useCase("project-1", "todo-abc")

        assertEquals("todo-abc", repo.lastDeletedId)
    }

    @Test
    fun `passes correct projectId`() = runBlocking {
        val repo = FakeTodoRepository()
        val useCase = DeleteTodoUseCase(repo)

        useCase("my-project", "todo-1")

        assertEquals("my-project", repo.lastProjectId)
    }

    // ── Fake ──────────────────────────────────────────────────────────────────

    private class FakeTodoRepository : TodoRepository {
        var lastProjectId: String? = null
        var lastDeletedId: String? = null

        override fun observeTodos(
            projectId: String,
            phaseId: String?,
            assignee: String?
        ): Flow<List<TodoItem>> = flowOf(emptyList())

        override suspend fun upsertTodo(projectId: String, todo: TodoItem) = Unit
        override suspend fun toggleTodo(projectId: String, todoId: String, done: Boolean) = Unit

        override suspend fun deleteTodo(projectId: String, todoId: String) {
            lastProjectId = projectId
            lastDeletedId = todoId
        }
    }
}
