package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.OwnedMaterial
import com.example.stugbygget.domain.repository.OwnedMaterialRepository

class UpsertOwnedMaterialUseCase(private val repository: OwnedMaterialRepository) {
    suspend operator fun invoke(projectId: String, material: OwnedMaterial) =
        repository.upsertOwnedMaterial(projectId, material)
}
