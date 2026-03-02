package com.example.stugbygget.feature.armeasure.ui

data class ArMeasureUiState(
    val hasCameraPermission: Boolean = false,
    val isArSupported: Boolean = false,
    val sessionReady: Boolean = false,
    val isSaving: Boolean = false,
    val points: List<MeasurePoint> = emptyList(),
    val measuredDistanceMeters: Float? = null,
    val measurementLabel: String = "Wall A-B",
    val selectedType: String = "WALL",
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val accuracyNote: String = "Target accuracy: ±2 cm. Baseline mode requires device calibration."
)

data class MeasurePoint(
    val x: Float,
    val y: Float
)
