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
                appContext = container.applicationContext,
                calculateMeasurementDistanceUseCase = container.calculateMeasurementDistanceUseCase,
                saveMeasurementUseCase = container.saveMeasurementUseCase,
                exportMeasurementToRoomPlannerUseCase = container.exportMeasurementToRoomPlannerUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
