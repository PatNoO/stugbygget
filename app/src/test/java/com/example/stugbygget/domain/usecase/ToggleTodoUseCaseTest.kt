package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.TodoItem
import com.example.stugbygget.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ToggleTodoUseCaseTest {

    @Test
    fun `delegates toggle to repository`() = runBlocking {
        val repo = FakeTodoRepository()
        val useCase = ToggleTodoUseCase(repo)

        useCase("project-1", "todo-1", true)

        assertEquals("todo-1", repo.lastToggledId)
        assertEquals(true, repo.lastToggledDone)
    }

    @Test
    fun `passes correct projectId`() = runBlocking {
        val repo = FakeTodoRepository()
        val useCase = ToggleTodoUseCase(repo)

        useCase("my-project", "todo-2", false)

        assertEquals("my-project", repo.lastProjectId)
    }

    @Test
    fun `can toggle done to false`() = runBlocking {
        val repo = FakeTodoRepository()
        val useCase = ToggleTodoUseCase(repo)

        useCase("project-1", "todo-1", false)

        assertEquals(false, repo.lastToggledDone)
    }

    // ── Fake ──────────────────────────────────────────────────────────────────

    private class FakeTodoRepository : TodoRepository {
        var lastProjectId: String? = null
        var lastToggledId: String? = null
        var lastToggledDone: Boolean? = null

        override fun observeTodos(
            projectId: String,
            phaseId: String?,
            assignee: String?
        ): Flow<List<TodoItem>> = flowOf(emptyList())

        override suspend fun upsertTodo(projectId: String, todo: TodoItem) = Unit

        override suspend fun toggleTodo(projectId: String, todoId: String, done: Boolean) {
            lastProjectId = projectId
            lastToggledId = todoId
            lastToggledDone = done
        }

        override suspend fun deleteTodo(projectId: String, todoId: String) = Unit
    }
}
