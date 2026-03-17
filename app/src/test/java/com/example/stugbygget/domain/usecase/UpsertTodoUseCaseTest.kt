package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.TodoItem
import com.example.stugbygget.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class UpsertTodoUseCaseTest {

    @Test
    fun `delegates upsert to repository`() = runBlocking {
        val repo = FakeTodoRepository()
        val useCase = UpsertTodoUseCase(repo)
        val todo = mockTodo("t1")

        useCase("project-1", todo)

        assertEquals(todo, repo.lastUpserted)
    }

    @Test
    fun `passes correct projectId`() = runBlocking {
        val repo = FakeTodoRepository()
        val useCase = UpsertTodoUseCase(repo)

        useCase("my-project", mockTodo())

        assertEquals("my-project", repo.lastProjectId)
    }

    @Test
    fun `can upsert multiple todos independently`() = runBlocking {
        val repo = FakeTodoRepository()
        val useCase = UpsertTodoUseCase(repo)
        val todoA = mockTodo("a")
        val todoB = mockTodo("b")

        useCase("project-1", todoA)
        useCase("project-1", todoB)

        assertNotNull(repo.lastUpserted)
        assertEquals(todoB, repo.lastUpserted)
    }

    // ── Fake ──────────────────────────────────────────────────────────────────

    private class FakeTodoRepository : TodoRepository {
        var lastProjectId: String? = null
        var lastUpserted: TodoItem? = null

        override fun observeTodos(
            projectId: String,
            phaseId: String?,
            assignee: String?
        ): Flow<List<TodoItem>> = flowOf(emptyList())

        override suspend fun upsertTodo(projectId: String, todo: TodoItem) {
            lastProjectId = projectId
            lastUpserted = todo
        }

        override suspend fun toggleTodo(projectId: String, todoId: String, done: Boolean) = Unit
        override suspend fun deleteTodo(projectId: String, todoId: String) = Unit
    }
}
