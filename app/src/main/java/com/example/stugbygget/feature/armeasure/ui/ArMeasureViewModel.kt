package com.example.stugbygget.feature.armeasure.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stugbygget.domain.model.MeasurementType
import com.example.stugbygget.domain.usecase.CalculateMeasurementDistanceUseCase
import com.example.stugbygget.domain.usecase.ExportMeasurementToRoomPlannerUseCase
import com.example.stugbygget.domain.usecase.RoomDimensionTarget
import com.example.stugbygget.domain.usecase.SaveMeasurementUseCase
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ArMeasureViewModel(
    private val appContext: Context,
    private val calculateMeasurementDistanceUseCase: CalculateMeasurementDistanceUseCase,
    private val saveMeasurementUseCase: SaveMeasurementUseCase,
    private val exportMeasurementToRoomPlannerUseCase: ExportMeasurementToRoomPlannerUseCase,
    private val projectId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArMeasureUiState())
    val uiState: StateFlow<ArMeasureUiState> = _uiState.asStateFlow()

    private var arSession: Session? = null

    fun onCameraPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasCameraPermission = granted) }
        if (granted) initializeArSession()
    }

    fun onOverlayTapped(x: Float, y: Float) {
        val existing = _uiState.value.points
        val updated = if (existing.size >= 2) {
            listOf(MeasurePoint(x, y))
        } else {
            existing + MeasurePoint(x, y)
        }

        val distance = if (updated.size == 2) {
            calculateMeasurementDistanceUseCase(
                startX = updated[0].x,
                startY = updated[0].y,
                endX = updated[1].x,
                endY = updated[1].y,
                pixelsToMetersScale = PIXELS_TO_METERS
            )
        } else {
            null
        }

        _uiState.update {
            it.copy(points = updated, measuredDistanceMeters = distance, errorMessage = null, statusMessage = null)
        }
    }

    fun onMeasurementLabelChanged(value: String) {
        _uiState.update { it.copy(measurementLabel = value) }
    }

    fun onMeasurementTypeSelected(value: String) {
        _uiState.update { it.copy(selectedType = value) }
    }

    fun onSaveMeasurement() {
        val state = _uiState.value
        val distance = state.measuredDistanceMeters ?: return
        val type = runCatching { MeasurementType.valueOf(state.selectedType) }
            .getOrElse { MeasurementType.CUSTOM }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, statusMessage = null) }
            runCatching {
                saveMeasurementUseCase(
                    projectId = projectId,
                    label = state.measurementLabel.ifBlank { "Measured distance" },
                    valueMeters = distance,
                    type = type
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(isSaving = false, statusMessage = "Measurement saved.")
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = throwable.message ?: "Failed to save measurement.")
                }
            }
        }
    }

    fun onExportToRoomWidth() {
        exportMeasurement(RoomDimensionTarget.WIDTH)
    }

    fun onExportToRoomHeight() {
        exportMeasurement(RoomDimensionTarget.HEIGHT)
    }

    private fun initializeArSession() {
        val availability = ArCoreApk.getInstance().checkAvailability(appContext)
        if (!availability.isSupported) {
            _uiState.update {
                it.copy(
                    isArSupported = false,
                    sessionReady = false,
                    errorMessage = "ARCore is not supported on this device."
                )
            }
            return
        }

        runCatching {
            Session(appContext)
        }.onSuccess { session ->
            arSession?.close()
            arSession = session
            _uiState.update {
                it.copy(
                    isArSupported = true,
                    sessionReady = true,
                    errorMessage = null
                )
            }
        }.onFailure { throwable ->
            _uiState.update {
                it.copy(
                    isArSupported = true,
                    sessionReady = false,
                    errorMessage = throwable.message ?: "Could not start AR session."
                )
            }
        }
    }

    private fun exportMeasurement(target: RoomDimensionTarget) {
        val distance = _uiState.value.measuredDistanceMeters ?: return
        runCatching {
            exportMeasurementToRoomPlannerUseCase(
                roomId = DEFAULT_ROOM_ID,
                valueMeters = distance,
                target = target
            )
        }.onSuccess { dims ->
            _uiState.update {
                it.copy(
                    statusMessage = "Exported to room planner (${dims.widthCm}cm × ${dims.heightCm}cm).",
                    errorMessage = null
                )
            }
        }.onFailure { throwable ->
            _uiState.update {
                it.copy(errorMessage = throwable.message ?: "Failed to export measurement.")
            }
        }
    }

    override fun onCleared() {
        arSession?.close()
        arSession = null
        super.onCleared()
    }

    companion object {
        private const val PIXELS_TO_METERS = 0.0025f
        private const val DEFAULT_ROOM_ID = "default-room"
    }
}
