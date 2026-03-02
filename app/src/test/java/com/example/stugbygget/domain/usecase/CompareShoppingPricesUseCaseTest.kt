package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.PriceComparisonResult
import com.example.stugbygget.domain.model.PriceQuote
import com.example.stugbygget.domain.model.ShoppingItem
import com.example.stugbygget.domain.model.ShoppingList
import com.example.stugbygget.domain.repository.PriceRecommendationRepository
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CompareShoppingPricesUseCaseTest {

    @Test
    fun `computes cheapest single store and best split`() = runBlocking {
        val repository = FakePriceRecommendationRepository()
        val useCase = CompareShoppingPricesUseCase(repository)
        val list = ShoppingList(
            id = "list-1",
            name = "Deck Materials",
            phaseId = "deck",
            items = listOf(
                ShoppingItem("i1", null, "Deck board", 10.0, "m", false, null, null),
                ShoppingItem("i2", null, "Screws", 2.0, "box", false, null, null)
            ),
            createdBy = "user",
            sharedWith = listOf("user"),
            totalEstimate = 0.0,
            updatedAt = Instant.now()
        )
        val quotes = listOf(
            PriceQuote("Deck board", "StoreA", 20.0, true, "a"),
            PriceQuote("Screws", "StoreA", 50.0, true, "a"),
            PriceQuote("Deck board", "StoreB", 19.0, true, "b"),
            PriceQuote("Screws", "StoreB", 70.0, true, "b"),
            PriceQuote("Screws", "StoreC", 40.0, true, "c")
        )

        val result = useCase("project", list, quotes)

        assertNotNull(result.cheapestSingleStore)
        assertEquals("StoreA", result.cheapestSingleStore?.store)
        assertEquals(300.0, result.cheapestSingleStore?.totalCost ?: 0.0, 0.0001)
        assertEquals(270.0, result.bestSplitTotal, 0.0001)
        assertNotNull(repository.lastSaved)
    }

    private class FakePriceRecommendationRepository : PriceRecommendationRepository {
        var lastSaved: PriceComparisonResult? = null

        override suspend fun saveSnapshot(
            projectId: String,
            shoppingListId: String,
            result: PriceComparisonResult
        ) {
            lastSaved = result
        }
    }
}
