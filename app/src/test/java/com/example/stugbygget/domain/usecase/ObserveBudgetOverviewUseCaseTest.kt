package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.BudgetOverview
import com.example.stugbygget.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveBudgetOverviewUseCaseTest {

    @Test
    fun `returns budget overview from repository`() = runBlocking {
        val expected = mockBudgetOverview()
        val repo = FakeBudgetRepository(overview = expected)
        val useCase = ObserveBudgetOverviewUseCase(repo)

        val actual = useCase("project-1").first()

        assertEquals(expected, actual)
    }

    @Test
    fun `passes projectId to repository`() = runBlocking {
        val repo = FakeBudgetRepository()
        val useCase = ObserveBudgetOverviewUseCase(repo)

        useCase("my-project").first()

        assertEquals("my-project", repo.lastProjectId)
    }

    // ── Fake ──────────────────────────────────────────────────────────────────

    private class FakeBudgetRepository(
        private val overview: BudgetOverview = mockBudgetOverview()
    ) : BudgetRepository {
        var lastProjectId: String? = null

        override fun observeBudget(projectId: String): Flow<BudgetOverview> {
            lastProjectId = projectId
            return flowOf(overview)
        }
    }
}

internal fun mockBudgetOverview(
    totalBudget: Double = 100_000.0,
    totalSpent: Double = 40_000.0
): BudgetOverview = BudgetOverview(
    totalBudget = totalBudget,
    totalSpent = totalSpent,
    estimatedFinalCost = totalSpent,
    phaseBudgets = emptyList(),
    categoryBudgets = emptyList()
)
