package com.example.stugbygget.feature.materials.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stugbygget.di.AppContainer

class MaterialsViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MaterialsViewModel::class.java)) {
            return MaterialsViewModel(
                observeMaterialsUseCase = container.observeMaterialsUseCase,
                observeOwnedMaterialsUseCase = container.observeOwnedMaterialsUseCase,
                upsertOwnedMaterialUseCase = container.upsertOwnedMaterialUseCase,
                deleteOwnedMaterialUseCase = container.deleteOwnedMaterialUseCase,
                projectId = container.projectSessionRepository.getProjectId(),
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

class MaterialDetailViewModelFactory(
    private val container: AppContainer,
    private val materialId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MaterialDetailViewModel::class.java)) {
            return MaterialDetailViewModel(
                observeMaterialsUseCase = container.observeMaterialsUseCase,
                observePriceQuotesUseCase = container.observePriceQuotesUseCase,
                calculateMaterialQuantityUseCase = container.calculateMaterialQuantityUseCase,
                projectId = container.projectSessionRepository.getProjectId(),
                materialId = materialId,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
