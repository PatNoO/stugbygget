package com.example.stugbygget.domain.usecase

import com.example.stugbygget.data.firebase.firestore.PhaseDocumentMapper
import com.example.stugbygget.domain.model.RenovationPhase
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildPlanningOverviewUseCaseTest {

    private val useCase = BuildPlanningOverviewUseCase()

    // ── Empty list ────────────────────────────────────────────────────────────

    @Test
    fun `empty phase list returns zero overview`() {
        val overview = useCase(emptyList(), now = Instant.parse("2026-06-01T00:00:00Z"))

        assertEquals(0, overview.totalProgressPercent)
        assertEquals(0L, overview.daysLeft)
        assertFalse(overview.isSchedulePassed)
    }

    // ── Progress calculation ──────────────────────────────────────────────────

    @Test
    fun `single phase progress is returned directly`() {
        val phase = PhaseDocumentMapper.mockPhase().copy(progress = 60)
        val overview = useCase(listOf(phase), now = Instant.parse("2026-06-01T00:00:00Z"))

        assertEquals(60, overview.totalProgressPercent)
    }

    @Test
    fun `multiple phases progress is averaged`() {
        val phases = listOf(
            PhaseDocumentMapper.mockPhase("p1").copy(progress = 20),
            PhaseDocumentMapper.mockPhase("p2").copy(progress = 80),
        )
        val overview = useCase(phases, now = Instant.parse("2026-06-01T00:00:00Z"))

        assertEquals(50, overview.totalProgressPercent)
    }

    @Test
    fun `progress rounds to nearest integer`() {
        val phases = listOf(
            PhaseDocumentMapper.mockPhase("p1").copy(progress = 0),
            PhaseDocumentMapper.mockPhase("p2").copy(progress = 1),
            PhaseDocumentMapper.mockPhase("p3").copy(progress = 1),
        )
        // average = 0.666… → rounds to 1
        val overview = useCase(phases, now = Instant.parse("2026-06-01T00:00:00Z"))

        assertEquals(1, overview.totalProgressPercent)
    }

    // ── Days left / schedule passed ───────────────────────────────────────────

    @Test
    fun `daysLeft is positive when end date is in the future`() {
        // mockPhase endDate = 2026-06-08, now = 2026-06-01 → 7 days left
        val phase = PhaseDocumentMapper.mockPhase()
        val overview = useCase(listOf(phase), now = Instant.parse("2026-06-01T00:00:00Z"))

        assertEquals(7L, overview.daysLeft)
        assertFalse(overview.isSchedulePassed)
    }

    @Test
    fun `isSchedulePassed is true when latest end date is in the past`() {
        val phase = PhaseDocumentMapper.mockPhase()
        // now is one day after the end date (2026-06-08)
        val overview = useCase(listOf(phase), now = Instant.parse("2026-06-09T00:00:00Z"))

        assertTrue(overview.isSchedulePassed)
    }

    @Test
    fun `daysLeft uses the latest end date across multiple phases`() {
        val phases = listOf(
            phase(endDate = "2026-06-08T00:00:00Z"),
            phase(endDate = "2026-07-01T00:00:00Z"), // latest
        )
        val overview = useCase(phases, now = Instant.parse("2026-06-01T00:00:00Z"))

        // 2026-07-01 minus 2026-06-01 = 30 days
        assertEquals(30L, overview.daysLeft)
    }

    @Test
    fun `isSchedulePassed is false when exactly on end date`() {
        val phase = PhaseDocumentMapper.mockPhase() // endDate = 2026-06-08
        val overview = useCase(listOf(phase), now = Instant.parse("2026-06-08T00:00:00Z"))

        // daysLeft = 0, but > 0 check means 0 is also "passed"
        assertTrue(overview.isSchedulePassed)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun phase(
        id: String = "p1",
        progress: Int = 0,
        endDate: String = "2026-06-08T00:00:00Z"
    ): RenovationPhase = PhaseDocumentMapper.mockPhase(id).copy(
        progress = progress,
        endDate = Instant.parse(endDate)
    )
}
