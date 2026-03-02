package com.example.stugbygget.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class MeasurementUnitConverterTest {
    private val converter = MeasurementUnitConverter()

    @Test
    fun `meters to rounded centimeters uses consistent rounding`() {
        assertEquals(153, converter.metersToRoundedCentimeters(1.534f))
        assertEquals(154, converter.metersToRoundedCentimeters(1.535f))
    }

    @Test
    fun `centimeters to meters converts with same base unit`() {
        assertEquals(4.85f, converter.centimetersToMeters(485))
    }
}
