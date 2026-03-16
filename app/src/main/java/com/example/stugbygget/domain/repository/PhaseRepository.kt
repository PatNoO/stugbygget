package com.example.stugbygget.domain.repository

import com.example.stugbygget.domain.model.RenovationPhase
import kotlinx.coroutines.flow.Flow

interface PhaseRepository {
    fun observePhases(projectId: String): Flow<List<RenovationPhase>>
    suspend fun upsertPhase(projectId: String, phase: RenovationPhase)
}
