package com.example.stugbygget.feature.budget.ui

import com.example.stugbygget.domain.model.BudgetOverview
import com.example.stugbygget.domain.model.CategoryBudget
import com.example.stugbygget.domain.model.PhaseBudget
import com.example.stugbygget.domain.repository.BudgetRepository
import com.example.stugbygget.domain.usecase.AddExpenseUseCase
import com.example.stugbygget.domain.usecase.ObserveBudgetOverviewUseCase
import com.example.stugbygget.domain.usecase.SavePhaseBudgetUseCase
import com.example.stugbygget.domain.usecase.mockBudgetOverview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Loading ───────────────────────────────────────────────────────────────

    @Test
    fun `overview loaded into state`() = runTest {
        val overview = mockBudgetOverview()
        val vm = buildViewModel(overview = overview)

        assertEquals(overview, vm.uiState.value.overview)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `error from repository sets errorMessage`() = runTest {
        val vm = buildViewModel(throwError = true)

        assertNotNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isLoading)
    }

    // ── Overspend detection ───────────────────────────────────────────────────

    @Test
    fun `overspent phase included when spent exceeds budgeted`() = runTest {
        val overview = mockBudgetOverview().copy(
            phaseBudgets = listOf(
                PhaseBudget(phaseId = "phase-1", budgeted = 10_000.0, spent = 12_000.0, estimated = 12_000.0)
            )
        )
        val vm = buildViewModel(overview = overview)

        assertTrue(vm.uiState.value.overspentPhaseIds.contains("phase-1"))
    }

    @Test
    fun `phase not overspent when spent equals budgeted`() = runTest {
        val overview = mockBudgetOverview().copy(
            phaseBudgets = listOf(
                PhaseBudget(phaseId = "phase-1", budgeted = 10_000.0, spent = 10_000.0, estimated = 10_000.0)
            )
        )
        val vm = buildViewModel(overview = overview)

        assertFalse(vm.uiState.value.overspentPhaseIds.contains("phase-1"))
    }

    @Test
    fun `phase with zero budget not included even if spent is positive`() = runTest {
        val overview = mockBudgetOverview().copy(
            phaseBudgets = listOf(
                PhaseBudget(phaseId = "phase-1", budgeted = 0.0, spent = 5_000.0, estimated = 5_000.0)
            )
        )
        val vm = buildViewModel(overview = overview)

        assertFalse(vm.uiState.value.overspentPhaseIds.contains("phase-1"))
    }

    @Test
    fun `multiple phases tracked independently`() = runTest {
        val overview = mockBudgetOverview().copy(
            phaseBudgets = listOf(
                PhaseBudget("p1", budgeted = 5_000.0, spent = 6_000.0, estimated = 6_000.0),
                PhaseBudget("p2", budgeted = 8_000.0, spent = 3_000.0, estimated = 3_000.0),
                PhaseBudget("p3", budgeted = 2_000.0, spent = 4_000.0, estimated = 4_000.0),
            )
        )
        val vm = buildViewModel(overview = overview)

        assertEquals(setOf("p1", "p3"), vm.uiState.value.overspentPhaseIds)
    }

    @Test
    fun `overspentPhaseIds empty when no phases overspent`() = runTest {
        val overview = mockBudgetOverview().copy(
            phaseBudgets = listOf(
                PhaseBudget("p1", budgeted = 10_000.0, spent = 5_000.0, estimated = 5_000.0)
            )
        )
        val vm = buildViewModel(overview = overview)

        assertTrue(vm.uiState.value.overspentPhaseIds.isEmpty())
    }

    @Test
    fun `error message is null on successful load`() = runTest {
        val vm = buildViewModel()

        assertNull(vm.uiState.value.errorMessage)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildViewModel(
        overview: BudgetOverview = mockBudgetOverview(),
        throwError: Boolean = false
    ): BudgetViewModel {
        val repo = FakeBudgetRepository(overview = overview, throwError = throwError)
        return BudgetViewModel(
            observeBudgetOverviewUseCase = ObserveBudgetOverviewUseCase(repo),
            savePhaseBudgetUseCase = SavePhaseBudgetUseCase(repo),
            addExpenseUseCase = AddExpenseUseCase(repo),
            projectId = "project-1"
        )
    }

    private class FakeBudgetRepository(
        private val overview: BudgetOverview,
        private val throwError: Boolean = false
    ) : BudgetRepository {
        override fun observeBudget(projectId: String): Flow<BudgetOverview> = flow {
            if (throwError) throw RuntimeException("Firestore unavailable")
            emit(overview)
        }

        override suspend fun setTotalBudget(projectId: String, totalBudget: Double) { /* no-op */ }
        override suspend fun savePhaseBudget(projectId: String, phaseId: String, budgeted: Double) { /* no-op */ }
        override suspend fun addExpense(projectId: String, phaseId: String, category: String, amount: Double) { /* no-op */ }
    }
}
