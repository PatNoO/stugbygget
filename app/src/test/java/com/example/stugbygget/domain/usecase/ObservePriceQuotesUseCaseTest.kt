package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.MaterialSpec
import com.example.stugbygget.domain.model.PriceQuote
import com.example.stugbygget.domain.repository.MaterialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ObservePriceQuotesUseCaseTest {

    @Test
    fun `returns quotes from repository`() = runBlocking {
        val expected = listOf(mockPriceQuote("Byggmax"))
        val repo = FakeMaterialRepository(quotes = expected)
        val useCase = ObservePriceQuotesUseCase(repo)

        val actual = useCase("project-1", "mat-1").first()

        assertEquals(expected, actual)
    }

    @Test
    fun `passes projectId and materialId to repository`() = runBlocking {
        val repo = FakeMaterialRepository()
        val useCase = ObservePriceQuotesUseCase(repo)

        useCase("my-project", "mat-42").first()

        assertEquals("my-project", repo.lastProjectId)
        assertEquals("mat-42", repo.lastMaterialId)
    }

    @Test
    fun `returns empty list when no quotes exist`() = runBlocking {
        val repo = FakeMaterialRepository(quotes = emptyList())
        val useCase = ObservePriceQuotesUseCase(repo)

        val actual = useCase("project-1", "mat-1").first()

        assertEquals(emptyList<PriceQuote>(), actual)
    }

    // ── Fake ──────────────────────────────────────────────────────────────────

    private class FakeMaterialRepository(
        private val quotes: List<PriceQuote> = emptyList()
    ) : MaterialRepository {
        var lastProjectId: String? = null
        var lastMaterialId: String? = null

        override fun observeMaterials(projectId: String): Flow<List<MaterialSpec>> =
            flowOf(emptyList())

        override fun observePriceQuotes(
            projectId: String,
            materialId: String
        ): Flow<List<PriceQuote>> {
            lastProjectId = projectId
            lastMaterialId = materialId
            return flowOf(quotes)
        }

        override suspend fun seedDefaultMaterials(projectId: String) { /* no-op */ }
    }
}

internal fun mockPriceQuote(
    store: String = "Byggmax",
    materialName: String = "Vit Färg",
    unitPrice: Double = 89.0,
    inStock: Boolean = true
): PriceQuote = PriceQuote(
    materialName = materialName,
    store = store,
    unitPrice = unitPrice,
    inStock = inStock,
    productUrl = "https://example.com/$store"
)
