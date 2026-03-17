package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.ShoppingItem
import com.example.stugbygget.domain.model.ShoppingList
import com.example.stugbygget.domain.repository.ShoppingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AddShoppingItemUseCaseTest {

    @Test
    fun `creates item with trimmed name`() = runBlocking {
        val repo = FakeShoppingRepository()
        val useCase = AddShoppingItemUseCase(repo)

        useCase("project-1", "list-1", "  Skruvar  ", 2.0, "box")

        assertEquals("Skruvar", repo.lastItem?.name)
    }

    @Test
    fun `defaults unit to pcs when blank`() = runBlocking {
        val repo = FakeShoppingRepository()
        val useCase = AddShoppingItemUseCase(repo)

        useCase("project-1", "list-1", "Bräda", 5.0, "   ")

        assertEquals("pcs", repo.lastItem?.unit)
    }

    @Test
    fun `trims unit`() = runBlocking {
        val repo = FakeShoppingRepository()
        val useCase = AddShoppingItemUseCase(repo)

        useCase("project-1", "list-1", "Bräda", 5.0, "  m  ")

        assertEquals("m", repo.lastItem?.unit)
    }

    @Test
    fun `item is created as not purchased`() = runBlocking {
        val repo = FakeShoppingRepository()
        val useCase = AddShoppingItemUseCase(repo)

        useCase("project-1", "list-1", "Bräda", 1.0, "pcs")

        assertFalse(repo.lastItem?.purchased ?: true)
    }

    @Test
    fun `item has null purchasedPrice and purchasedAt`() = runBlocking {
        val repo = FakeShoppingRepository()
        val useCase = AddShoppingItemUseCase(repo)

        useCase("project-1", "list-1", "Bräda", 1.0, "pcs")

        assertNull(repo.lastItem?.purchasedPrice)
        assertNull(repo.lastItem?.purchasedAt)
    }

    @Test
    fun `item gets a generated id`() = runBlocking {
        val repo = FakeShoppingRepository()
        val useCase = AddShoppingItemUseCase(repo)

        useCase("project-1", "list-1", "Bräda", 1.0, "pcs")

        assertNotNull(repo.lastItem?.id)
    }

    @Test
    fun `passes correct quantity`() = runBlocking {
        val repo = FakeShoppingRepository()
        val useCase = AddShoppingItemUseCase(repo)

        useCase("project-1", "list-1", "Bräda", 3.5, "m")

        assertEquals(3.5, repo.lastItem?.quantity)
    }

    // ── Fake ──────────────────────────────────────────────────────────────────

    private class FakeShoppingRepository : ShoppingRepository {
        var lastItem: ShoppingItem? = null

        override fun observeShoppingLists(projectId: String): Flow<List<ShoppingList>> =
            flowOf(emptyList())

        override suspend fun createShoppingList(
            projectId: String, name: String, phaseId: String, createdBy: String
        ) = Unit

        override suspend fun addItem(projectId: String, listId: String, item: ShoppingItem) {
            lastItem = item
        }

        override suspend fun updateItemPurchased(
            projectId: String, listId: String, itemId: String, purchased: Boolean
        ) = Unit
    }
}
