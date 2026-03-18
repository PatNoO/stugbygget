package com.example.stugbygget.domain.repository

import com.example.stugbygget.domain.model.OwnedMaterial
import kotlinx.coroutines.flow.Flow

interface OwnedMaterialRepository {
    fun observeOwnedMaterials(projectId: String): Flow<List<OwnedMaterial>>
    suspend fun upsertOwnedMaterial(projectId: String, material: OwnedMaterial)
    suspend fun deleteOwnedMaterial(projectId: String, id: String)
}
