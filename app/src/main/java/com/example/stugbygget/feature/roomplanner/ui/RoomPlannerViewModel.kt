package com.example.stugbygget.feature.roomplanner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stugbygget.domain.usecase.MoveFurnitureUseCase
import com.example.stugbygget.domain.usecase.ObserveRoomLayoutUseCase
import com.example.stugbygget.domain.usecase.SaveRoomLayoutUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoomPlannerViewModel(
    private val observeRoomLayoutUseCase: ObserveRoomLayoutUseCase,
    private val saveRoomLayoutUseCase: SaveRoomLayoutUseCase,
    private val moveFurnitureUseCase: MoveFurnitureUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoomPlannerUiState())
    val uiState: StateFlow<RoomPlannerUiState> = _uiState.asStateFlow()

    init {
        observeRoomLayout()
    }

    fun onFurnitureSelected(id: String) {
        _uiState.update { state -> state.copy(selectedFurnitureId = id) }
    }

    fun onCanvasDragStart(xCm: Float, yCm: Float) {
        val selected = _uiState.value.furniture.firstOrNull { item ->
            xCm >= item.xCm && xCm <= item.xCm + item.widthCm &&
                yCm >= item.yCm && yCm <= item.yCm + item.depthCm
        }
        _uiState.update { it.copy(selectedFurnitureId = selected?.id, errorMessage = null) }
    }

    fun onCanvasDragged(xCm: Float, yCm: Float) {
        val state = _uiState.value
        val selectedId = state.selectedFurnitureId ?: return
        val selectedItem = state.furniture.firstOrNull { it.id == selectedId } ?: return

        val result = moveFurnitureUseCase(
            furniture = state.furniture,
            movingId = selectedId,
            targetXCm = xCm - selectedItem.widthCm / 2f,
            targetYCm = yCm - selectedItem.depthCm / 2f,
            roomWidthCm = state.roomWidthCm,
            roomHeightCm = state.roomHeightCm,
            snapStepCm = state.gridStepCm
        )
        _uiState.update {
            it.copy(
                furniture = result.furniture,
                errorMessage = if (result.applied) null else result.reason
            )
        }
    }

    fun onCanvasDragEnd() {
        val state = _uiState.value
        viewModelScope.launch {
            saveRoomLayoutUseCase(state.roomId, state.furniture)
        }
    }

    private fun observeRoomLayout() {
        viewModelScope.launch {
            observeRoomLayoutUseCase(_uiState.value.roomId)
                .catch { throwable ->
                    _uiState.update {
                        it.copy(errorMessage = throwable.message ?: "Kunde inte ladda rumslayout")
                    }
                }
                .collect { furniture ->
                    _uiState.update { it.copy(furniture = furniture, errorMessage = null) }
                }
        }
    }
}
