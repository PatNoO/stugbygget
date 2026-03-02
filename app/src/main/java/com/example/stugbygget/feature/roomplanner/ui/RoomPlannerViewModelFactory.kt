package com.example.stugbygget.feature.roomplanner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stugbygget.di.AppContainer

class RoomPlannerViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoomPlannerViewModel::class.java)) {
            return RoomPlannerViewModel(
                observeRoomLayoutUseCase = container.observeRoomLayoutUseCase,
                saveRoomLayoutUseCase = container.saveRoomLayoutUseCase,
                moveFurnitureUseCase = container.moveFurnitureUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
