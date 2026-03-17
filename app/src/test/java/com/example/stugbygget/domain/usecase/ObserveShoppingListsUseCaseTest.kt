package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.ShoppingItem
import com.example.stugbygget.domain.model.ShoppingList
import com.example.stugbygget.domain.repository.ShoppingRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveShoppingListsUseCaseTest {

    @Test
    fun `returns lists from repository`() = runBlocking {
        val expected = listOf(mockShoppingList("list-1"))
        val repo = FakeShoppingRepository(lists = expected)
        val useCase = ObserveShoppingListsUseCase(repo)

        val actual = useCase("project-1").first()

        assertEquals(expected, actual)
    }

    @Test
    fun `passes projectId to repository`() = runBlocking {
        val repo = FakeShoppingRepository()
        val useCase = ObserveShoppingListsUseCase(repo)

        useCase("my-project").first()

        assertEquals("my-project", repo.lastObserveProjectId)
    }

    @Test
    fun `returns empty list when no lists exist`() = runBlocking {
        val repo = FakeShoppingRepository(lists = emptyList())
        val useCase = ObserveShoppingListsUseCase(repo)

        val actual = useCase("project-1").first()

        assertEquals(emptyList<ShoppingList>(), actual)
    }

    // ── Fake ──────────────────────────────────────────────────────────────────

    private class FakeShoppingRepository(
        private val lists: List<ShoppingList> = emptyList()
    ) : ShoppingRepository {
        var lastObserveProjectId: String? = null

        override fun observeShoppingLists(projectId: String): Flow<List<ShoppingList>> {
            lastObserveProjectId = projectId
            return flowOf(lists)
        }

        override suspend fun createShoppingList(
            projectId: String, name: String, phaseId: String, createdBy: String
        ) = Unit

        override suspend fun addItem(projectId: String, listId: String, item: ShoppingItem) = Unit

        override suspend fun updateItemPurchased(
            projectId: String, listId: String, itemId: String, purchased: Boolean
        ) = Unit
    }
}

internal fun mockShoppingList(
    id: String = "list-1",
    name: String = "Deck Materials",
    phaseId: String = "phase-1",
    items: List<ShoppingItem> = emptyList()
): ShoppingList = ShoppingList(
    id = id,
    name = name,
    phaseId = phaseId,
    items = items,
    createdBy = "user-1",
    sharedWith = emptyList(),
    totalEstimate = 0.0,
    updatedAt = Instant.parse("2026-06-01T00:00:00Z")
)
