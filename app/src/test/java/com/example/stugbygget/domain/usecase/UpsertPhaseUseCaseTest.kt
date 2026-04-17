package com.example.stugbygget.domain.usecase

import com.example.stugbygget.data.firebase.firestore.PhaseDocumentMapper
import com.example.stugbygget.domain.model.RenovationPhase
import com.example.stugbygget.domain.repository.PhaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class UpsertPhaseUseCaseTest {

    @Test
    fun `delegates upsert to repository`() = runBlocking {
        val repo = FakePhaseRepository()
        val useCase = UpsertPhaseUseCase(repo)
        val phase = PhaseDocumentMapper.mockPhase()

        useCase("project-1", phase)

        assertEquals(phase, repo.lastUpserted)
    }

    @Test
    fun `upsert called with correct projectId`() = runBlocking {
        val repo = FakePhaseRepository()
        val useCase = UpsertPhaseUseCase(repo)
        val phase = PhaseDocumentMapper.mockPhase()

        useCase("my-project", phase)

        assertEquals("my-project", repo.lastProjectId)
    }

    @Test
    fun `can upsert multiple phases independently`() = runBlocking {
        val repo = FakePhaseRepository()
        val useCase = UpsertPhaseUseCase(repo)
        val phaseA = PhaseDocumentMapper.mockPhase("a")
        val phaseB = PhaseDocumentMapper.mockPhase("b")

        useCase("project-1", phaseA)
        useCase("project-1", phaseB)

        assertEquals(phaseB, repo.lastUpserted)
        assertNotNull(repo.lastUpserted)
    }

    // ── Fake ──────────────────────────────────────────────────────────────────

    private class FakePhaseRepository : PhaseRepository {
        var lastProjectId: String? = null
        var lastUpserted: RenovationPhase? = null

        override fun observePhases(projectId: String): Flow<List<RenovationPhase>> = flowOf(emptyList())

        override suspend fun upsertPhase(projectId: String, phase: RenovationPhase) {
            lastProjectId = projectId
            lastUpserted = phase
        }

        override suspend fun deletePhase(projectId: String, phaseId: String) { /* no-op */ }
    }
}
