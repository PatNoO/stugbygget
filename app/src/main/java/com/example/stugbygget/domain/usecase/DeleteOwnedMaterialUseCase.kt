package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.repository.OwnedMaterialRepository

class DeleteOwnedMaterialUseCase(private val repository: OwnedMaterialRepository) {
    suspend operator fun invoke(projectId: String, id: String) =
        repository.deleteOwnedMaterial(projectId, id)
}
