package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.MaterialCategory
import com.example.stugbygget.domain.model.MaterialSpec
import com.example.stugbygget.domain.model.UnitType
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateMaterialQuantityUseCaseTest {

    private val useCase = CalculateMaterialQuantityUseCase()

    @Test
    fun `paint calculation includes layers and waste`() {
        val spec = MaterialSpec(
            id = "paint-1",
            name = "Falu Red 10L",
            category = MaterialCategory.PAINT,
            unitType = UnitType.LITER,
            coveragePerUnit = 6.0,
            wasteMargin = 0.05
        )
        val result = useCase(spec = spec, areaM2 = 48.0, layers = 2)
        assertEquals(16.8, result.requiredUnits, 0.0001)
        assertEquals(17, result.roundedUnits)
    }

    @Test
    fun `wood calculation uses length and waste`() {
        val spec = MaterialSpec(
            id = "wood-1",
            name = "Deck board",
            category = MaterialCategory.WOOD,
            unitType = UnitType.METER,
            coveragePerUnit = 1.0,
            wasteMargin = 0.1
        )
        val result = useCase(spec = spec, areaM2 = 0.0, lengthM = 80.0)
        assertEquals(88.0, result.requiredUnits, 0.0001)
        assertEquals(88, result.roundedUnits)
    }

    @Test
    fun `insulation calculation uses area`() {
        val spec = MaterialSpec(
            id = "insulation-1",
            name = "Isover roll",
            category = MaterialCategory.INSULATION,
            unitType = UnitType.ROLL,
            coveragePerUnit = 2.5,
            wasteMargin = 0.0
        )
        val result = useCase(spec = spec, areaM2 = 35.0)
        assertEquals(14.0, result.requiredUnits, 0.0001)
        assertEquals(14, result.roundedUnits)
    }

    @Test
    fun `tile calculation applies waste margin`() {
        val spec = MaterialSpec(
            id = "tile-1",
            name = "Bathroom tile pack",
            category = MaterialCategory.TILE,
            unitType = UnitType.SQUARE_METER,
            coveragePerUnit = 1.2,
            wasteMargin = 0.1
        )
        val result = useCase(spec = spec, areaM2 = 15.0)
        assertEquals(13.75, result.requiredUnits, 0.0001)
        assertEquals(14, result.roundedUnits)
    }
}
