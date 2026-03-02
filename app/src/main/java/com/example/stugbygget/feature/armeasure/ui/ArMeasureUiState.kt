package com.example.stugbygget.feature.armeasure.ui

data class ArMeasureUiState(
    val hasCameraPermission: Boolean = false,
    val isArSupported: Boolean = false,
    val sessionReady: Boolean = false,
    val points: List<MeasurePoint> = emptyList(),
    val measuredDistanceMeters: Float? = null,
    val errorMessage: String? = null,
    val accuracyNote: String = "Mål: ±2 cm. Baslinjemätning kräver kalibrering per enhet."
)

data class MeasurePoint(
    val x: Float,
    val y: Float
)
