package com.example.stugbygget.domain.usecase

import com.example.stugbygget.data.firebase.firestore.PhaseDocumentMapper
import com.example.stugbygget.domain.model.RenovationPhase
import com.example.stugbygget.domain.repository.PhaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ObservePhasesUseCaseTest {

    @Test
    fun `returns phases from repository`() = runBlocking {
        val expected = listOf(PhaseDocumentMapper.mockPhase())
        val repository = FakePhaseRepository(expected)
        val useCase = ObservePhasesUseCase(repository)

        val actual = useCase("project-1")

        var collected: List<RenovationPhase> = emptyList()
        actual.collect { collected = it }

        assertEquals(expected, collected)
    }

    private class FakePhaseRepository(
        private val phases: List<RenovationPhase>
    ) : PhaseRepository {
        override fun observePhases(projectId: String): Flow<List<RenovationPhase>> = flowOf(phases)
        override suspend fun upsertPhase(projectId: String, phase: RenovationPhase) = Unit
    }
}
