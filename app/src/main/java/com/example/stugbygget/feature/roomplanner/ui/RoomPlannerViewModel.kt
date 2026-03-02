package com.example.stugbygget.feature.roomplanner.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RoomPlannerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RoomPlannerUiState())
    val uiState: StateFlow<RoomPlannerUiState> = _uiState.asStateFlow()

    fun onFurnitureSelected(id: String) {
        _uiState.update { state -> state.copy(selectedFurnitureId = id) }
    }
}
