package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.MaterialCategory
import com.example.stugbygget.domain.model.MaterialSpec
import com.example.stugbygget.domain.model.PriceQuote
import com.example.stugbygget.domain.model.UnitType
import com.example.stugbygget.domain.repository.MaterialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveMaterialsUseCaseTest {

    @Test
    fun `returns materials from repository`() = runBlocking {
        val expected = listOf(mockMaterialSpec("m1"))
        val repo = FakeMaterialRepository(materials = expected)
        val useCase = ObserveMaterialsUseCase(repo)

        val actual = useCase("project-1").first()

        assertEquals(expected, actual)
    }

    @Test
    fun `passes projectId to repository`() = runBlocking {
        val repo = FakeMaterialRepository()
        val useCase = ObserveMaterialsUseCase(repo)

        useCase("my-project").first()

        assertEquals("my-project", repo.lastProjectId)
    }

    @Test
    fun `returns empty list when repository has no materials`() = runBlocking {
        val repo = FakeMaterialRepository(materials = emptyList())
        val useCase = ObserveMaterialsUseCase(repo)

        val actual = useCase("project-1").first()

        assertEquals(emptyList<MaterialSpec>(), actual)
    }

    // ── Fake ──────────────────────────────────────────────────────────────────

    private class FakeMaterialRepository(
        private val materials: List<MaterialSpec> = emptyList()
    ) : MaterialRepository {
        var lastProjectId: String? = null

        override fun observeMaterials(projectId: String): Flow<List<MaterialSpec>> {
            lastProjectId = projectId
            return flowOf(materials)
        }

        override fun observePriceQuotes(
            projectId: String,
            materialId: String
        ): Flow<List<PriceQuote>> = flowOf(emptyList())

        override suspend fun seedDefaultMaterials(projectId: String) { /* no-op */ }
    }
}

internal fun mockMaterialSpec(
    id: String = "mat-1",
    name: String = "Vit Färg",
    category: MaterialCategory = MaterialCategory.PAINT,
    unitType: UnitType = UnitType.LITER,
    coveragePerUnit: Double = 10.0,
    wasteMargin: Double = 0.1
): MaterialSpec = MaterialSpec(
    id = id,
    name = name,
    category = category,
    unitType = unitType,
    coveragePerUnit = coveragePerUnit,
    wasteMargin = wasteMargin
)
