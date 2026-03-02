package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.MaterialCategory
import com.example.stugbygget.domain.model.MaterialSpec
import kotlin.math.ceil

data class MaterialQuantityResult(
    val requiredUnits: Double,
    val roundedUnits: Int
)

class CalculateMaterialQuantityUseCase {

    /**
     * Calculates required units for supported material categories using one
     * consistent formula: (base amount / coverage) * (1 + wasteMargin).
     * Category-specific parameters are passed in via the input dimensions.
     */
    operator fun invoke(
        spec: MaterialSpec,
        areaM2: Double,
        lengthM: Double = 0.0,
        layers: Int = 1
    ): MaterialQuantityResult {
        require(spec.coveragePerUnit > 0) { "coveragePerUnit must be > 0" }
        require(spec.wasteMargin >= 0) { "wasteMargin must be >= 0" }

        val baseAmount = when (spec.category) {
            MaterialCategory.PAINT -> areaM2 * layers
            MaterialCategory.WOOD -> lengthM
            MaterialCategory.INSULATION -> areaM2
            MaterialCategory.TILE -> areaM2
        }
        val required = (baseAmount / spec.coveragePerUnit) * (1.0 + spec.wasteMargin)
        return MaterialQuantityResult(
            requiredUnits = required,
            roundedUnits = ceil(required).toInt()
        )
    }
}
