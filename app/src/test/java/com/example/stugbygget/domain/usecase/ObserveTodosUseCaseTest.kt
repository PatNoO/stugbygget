package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.TodoItem
import com.example.stugbygget.domain.model.TodoPriority
import com.example.stugbygget.domain.repository.TodoRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveTodosUseCaseTest {

    @Test
    fun `returns todos from repository`() = runBlocking {
        val expected = listOf(mockTodo("t1"))
        val repo = FakeTodoRepository(todos = expected)
        val useCase = ObserveTodosUseCase(repo)

        val actual = useCase("project-1").first()

        assertEquals(expected, actual)
    }

    @Test
    fun `forwards phaseId filter to repository`() = runBlocking {
        val repo = FakeTodoRepository()
        val useCase = ObserveTodosUseCase(repo)

        useCase("project-1", phaseId = "phase-A").first()

        assertEquals("phase-A", repo.lastPhaseId)
    }

    @Test
    fun `forwards assignee filter to repository`() = runBlocking {
        val repo = FakeTodoRepository()
        val useCase = ObserveTodosUseCase(repo)

        useCase("project-1", assignee = "Alice").first()

        assertEquals("Alice", repo.lastAssignee)
    }

    @Test
    fun `returns empty list when repository has no todos`() = runBlocking {
        val repo = FakeTodoRepository(todos = emptyList())
        val useCase = ObserveTodosUseCase(repo)

        val actual = useCase("project-1").first()

        assertEquals(emptyList<TodoItem>(), actual)
    }

    // ── Fake ──────────────────────────────────────────────────────────────────

    private class FakeTodoRepository(
        private val todos: List<TodoItem> = emptyList()
    ) : TodoRepository {
        var lastPhaseId: String? = null
        var lastAssignee: String? = null

        override fun observeTodos(
            projectId: String,
            phaseId: String?,
            assignee: String?
        ): Flow<List<TodoItem>> {
            lastPhaseId = phaseId
            lastAssignee = assignee
            return flowOf(todos)
        }

        override suspend fun upsertTodo(projectId: String, todo: TodoItem) = Unit
        override suspend fun toggleTodo(projectId: String, todoId: String, done: Boolean) = Unit
        override suspend fun deleteTodo(projectId: String, todoId: String) = Unit
    }
}

internal fun mockTodo(
    id: String = "todo-1",
    text: String = "Paint the walls",
    done: Boolean = false,
    phaseId: String = "phase-1",
    assignee: String = "Alice",
    priority: TodoPriority = TodoPriority.MEDIUM
): TodoItem = TodoItem(
    id = id,
    text = text,
    done = done,
    phaseId = phaseId,
    assignee = assignee,
    priority = priority,
    createdAt = Instant.parse("2026-06-01T00:00:00Z"),
    updatedAt = Instant.parse("2026-06-01T00:00:00Z")
)
