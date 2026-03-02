package com.example.stugbygget.domain.usecase

import kotlin.math.roundToInt

class MeasurementUnitConverter {
    fun metersToRoundedCentimeters(valueMeters: Float): Int = (valueMeters * 100f).roundToInt()
    fun centimetersToMeters(valueCm: Int): Float = valueCm / 100f
}
