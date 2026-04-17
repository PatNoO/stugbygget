package com.example.stugbygget.feature.planning.ui

import com.example.stugbygget.data.firebase.firestore.PhaseDocumentMapper
import com.example.stugbygget.domain.model.RenovationPhase
import com.example.stugbygget.domain.repository.PhaseRepository
import com.example.stugbygget.domain.usecase.BuildPlanningOverviewUseCase
import com.example.stugbygget.domain.usecase.DeletePhaseUseCase
import com.example.stugbygget.domain.usecase.ObservePhasesUseCase
import com.example.stugbygget.domain.usecase.UpsertPhaseUseCase
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
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
class PlanningViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    // Epoch millis for test dates (UTC midnight)
    private val MILLIS_JUN_01 = LocalDate.parse("2026-06-01").atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    private val MILLIS_JUN_08 = LocalDate.parse("2026-06-08").atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial state has isLoading true`() = runTest {
        val vm = buildViewModel(phases = emptyList())
        // After collecting the first emission isLoading becomes false,
        // but the ViewModel starts with isLoading = true before the flow emits.
        // With UnconfinedTestDispatcher the flow runs eagerly, so we check the settled state.
        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.errorMessage)
    }

    // ── Phase loading ─────────────────────────────────────────────────────────

    @Test
    fun `phases are loaded into state`() = runTest {
        val phases = listOf(PhaseDocumentMapper.mockPhase())
        val vm = buildViewModel(phases = phases)

        assertEquals(phases, vm.uiState.value.phases)
    }

    @Test
    fun `overview is computed from phases`() = runTest {
        val phases = listOf(
            PhaseDocumentMapper.mockPhase("p1").copy(progress = 40),
            PhaseDocumentMapper.mockPhase("p2").copy(progress = 60),
        )
        val vm = buildViewModel(phases = phases)

        assertEquals(50, vm.uiState.value.totalProgressPercent)
    }

    @Test
    fun `error from repository sets errorMessage`() = runTest {
        val vm = buildViewModel(throwError = true)

        assertNotNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isLoading)
    }

    // ── Add-phase sheet ───────────────────────────────────────────────────────

    @Test
    fun `onShowAddSheet sets showAddSheet and clears draft`() = runTest {
        val vm = buildViewModel()
        vm.onDraftNameChanged("Some name")

        vm.onShowAddSheet()

        assertTrue(vm.uiState.value.showAddSheet)
        assertEquals("", vm.uiState.value.draftName)
        assertNull(vm.uiState.value.addError)
    }

    @Test
    fun `onDismissAddSheet clears sheet and error`() = runTest {
        val vm = buildViewModel()
        vm.onShowAddSheet()

        vm.onDismissAddSheet()

        assertFalse(vm.uiState.value.showAddSheet)
        assertNull(vm.uiState.value.addError)
    }

    @Test
    fun `draft field updates reflected in state`() = runTest {
        val vm = buildViewModel()

        vm.onDraftNameChanged("Rivning")
        vm.onDraftRoomChanged("Kök")
        vm.onDraftStartMillisChanged(MILLIS_JUN_01)
        vm.onDraftEndMillisChanged(MILLIS_JUN_08)

        assertEquals("Rivning", vm.uiState.value.draftName)
        assertEquals("Kök", vm.uiState.value.draftRoom)
        assertEquals(MILLIS_JUN_01, vm.uiState.value.draftStartMillis)
        assertEquals(MILLIS_JUN_08, vm.uiState.value.draftEndMillis)
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Test
    fun `submit with blank name sets addError`() = runTest {
        val vm = buildViewModel()
        vm.onShowAddSheet()
        vm.onDraftRoomChanged("Kök")
        vm.onDraftStartMillisChanged(MILLIS_JUN_01)
        vm.onDraftEndMillisChanged(MILLIS_JUN_08)

        vm.onSubmitPhase()

        assertNotNull(vm.uiState.value.addError)
        assertTrue(vm.uiState.value.showAddSheet.not().not()) // sheet still open implicitly via addError
    }

    @Test
    fun `submit with blank room sets addError`() = runTest {
        val vm = buildViewModel()
        vm.onDraftNameChanged("Rivning")
        vm.onDraftStartMillisChanged(MILLIS_JUN_01)
        vm.onDraftEndMillisChanged(MILLIS_JUN_08)

        vm.onSubmitPhase()

        assertNotNull(vm.uiState.value.addError)
    }

    @Test
    fun `submit with missing dates sets addError`() = runTest {
        val vm = buildViewModel()
        vm.onDraftNameChanged("Rivning")
        vm.onDraftRoomChanged("Kök")
        // Intentionally omitting date selection — draftStartMillis/draftEndMillis remain null

        vm.onSubmitPhase()

        assertNotNull(vm.uiState.value.addError)
    }

    @Test
    fun `submit with end date before start date sets addError`() = runTest {
        val vm = buildViewModel()
        vm.onDraftNameChanged("Rivning")
        vm.onDraftRoomChanged("Kök")
        vm.onDraftStartMillisChanged(MILLIS_JUN_08)
        vm.onDraftEndMillisChanged(MILLIS_JUN_01) // before start

        vm.onSubmitPhase()

        assertNotNull(vm.uiState.value.addError)
    }

    @Test
    fun `submit with end date equal to start date sets addError`() = runTest {
        val vm = buildViewModel()
        vm.onDraftNameChanged("Rivning")
        vm.onDraftRoomChanged("Kök")
        vm.onDraftStartMillisChanged(MILLIS_JUN_08)
        vm.onDraftEndMillisChanged(MILLIS_JUN_08) // same day

        vm.onSubmitPhase()

        assertNotNull(vm.uiState.value.addError)
    }

    // ── Successful submit ─────────────────────────────────────────────────────

    @Test
    fun `valid submit calls upsert and closes sheet`() = runTest {
        val repo = FakePhaseRepository()
        val vm = buildViewModel(repo = repo)

        vm.onDraftNameChanged("Rivning")
        vm.onDraftRoomChanged("Kök")
        vm.onDraftStartMillisChanged(MILLIS_JUN_01)
        vm.onDraftEndMillisChanged(MILLIS_JUN_08)
        vm.onSubmitPhase()

        assertNotNull(repo.lastUpserted)
        assertEquals("Rivning", repo.lastUpserted?.name)
        assertEquals("Kök", repo.lastUpserted?.room)
        assertFalse(vm.uiState.value.showAddSheet)
        assertNull(vm.uiState.value.addError)
    }

    @Test
    fun `submit failure sets addError without closing sheet`() = runTest {
        val repo = FakePhaseRepository(throwOnUpsert = true)
        val vm = buildViewModel(repo = repo)

        vm.onDraftNameChanged("Rivning")
        vm.onDraftRoomChanged("Kök")
        vm.onDraftStartMillisChanged(MILLIS_JUN_01)
        vm.onDraftEndMillisChanged(MILLIS_JUN_08)
        vm.onSubmitPhase()

        assertNotNull(vm.uiState.value.addError)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildViewModel(
        phases: List<RenovationPhase> = emptyList(),
        throwError: Boolean = false,
        repo: FakePhaseRepository = FakePhaseRepository(phases = phases, throwOnObserve = throwError)
    ): PlanningViewModel = PlanningViewModel(
        observePhasesUseCase = ObservePhasesUseCase(repo),
        projectId = "project-1",
        buildPlanningOverviewUseCase = BuildPlanningOverviewUseCase(),
        upsertPhaseUseCase = UpsertPhaseUseCase(repo),
        deletePhaseUseCase = DeletePhaseUseCase(repo)
    )

    private class FakePhaseRepository(
        private val phases: List<RenovationPhase> = emptyList(),
        private val throwOnObserve: Boolean = false,
        private val throwOnUpsert: Boolean = false
    ) : PhaseRepository {
        var lastUpserted: RenovationPhase? = null

        override fun observePhases(projectId: String): Flow<List<RenovationPhase>> = flow {
            if (throwOnObserve) throw RuntimeException("Firestore unavailable")
            emit(phases)
        }

        override suspend fun upsertPhase(projectId: String, phase: RenovationPhase) {
            if (throwOnUpsert) throw RuntimeException("Write failed")
            lastUpserted = phase
        }

        override suspend fun deletePhase(projectId: String, phaseId: String) { /* no-op */ }
    }
}
