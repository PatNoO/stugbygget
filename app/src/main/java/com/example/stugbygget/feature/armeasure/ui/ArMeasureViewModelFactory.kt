package com.example.stugbygget.feature.armeasure.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stugbygget.di.AppContainer

class ArMeasureViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArMeasureViewModel::class.java)) {
            return ArMeasureViewModel(
                isArSupportedUseCase = container.isArSupportedUseCase,
                startArSessionUseCase = container.startArSessionUseCase,
                stopArSessionUseCase = container.stopArSessionUseCase,
                calculateMeasurementDistanceUseCase = container.calculateMeasurementDistanceUseCase,
                saveMeasurementUseCase = container.saveMeasurementUseCase,
                projectId = container.projectSessionRepository.getProjectId()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
