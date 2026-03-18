package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.OwnedMaterial
import com.example.stugbygget.domain.repository.OwnedMaterialRepository
import kotlinx.coroutines.flow.Flow

class ObserveOwnedMaterialsUseCase(private val repository: OwnedMaterialRepository) {
    operator fun invoke(projectId: String): Flow<List<OwnedMaterial>> =
        repository.observeOwnedMaterials(projectId)
}
