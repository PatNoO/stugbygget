package com.example.stugbygget.domain.model

import java.time.Instant

data class MeasurementRecord(
    val id: String = "",
    val label: String,
    val valueCm: Int,
    val type: MeasurementType,
    val createdAt: Instant = Instant.now()
)

enum class MeasurementType {
    WALL,
    WINDOW,
    DOOR,
    CUSTOM
}
