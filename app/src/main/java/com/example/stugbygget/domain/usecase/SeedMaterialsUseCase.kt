package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.repository.MaterialRepository

class SeedMaterialsUseCase(
    private val materialRepository: MaterialRepository,
) {
    suspend operator fun invoke(projectId: String) {
        materialRepository.seedDefaultMaterials(projectId)
    }
}
