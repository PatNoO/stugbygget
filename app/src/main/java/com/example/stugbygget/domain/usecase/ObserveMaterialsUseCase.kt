package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.MaterialSpec
import com.example.stugbygget.domain.repository.MaterialRepository
import kotlinx.coroutines.flow.Flow

class ObserveMaterialsUseCase(private val repository: MaterialRepository) {
    operator fun invoke(projectId: String): Flow<List<MaterialSpec>> =
        repository.observeMaterials(projectId)
}
