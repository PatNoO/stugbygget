package com.example.stugbygget.feature.armeasure.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.stugbygget.domain.usecase.CalculateMeasurementDistanceUseCase
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ArMeasureViewModel(
    private val appContext: Context,
    private val calculateMeasurementDistanceUseCase: CalculateMeasurementDistanceUseCase
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
            it.copy(points = updated, measuredDistanceMeters = distance)
        }
    }

    private fun initializeArSession() {
        val availability = ArCoreApk.getInstance().checkAvailability(appContext)
        if (!availability.isSupported) {
            _uiState.update {
                it.copy(
                    isArSupported = false,
                    sessionReady = false,
                    errorMessage = "ARCore stöds inte på denna enhet."
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
                    errorMessage = throwable.message ?: "Kunde inte starta AR-session."
                )
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
    }
}
