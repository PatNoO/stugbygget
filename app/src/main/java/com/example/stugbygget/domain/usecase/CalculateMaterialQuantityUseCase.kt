package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.MaterialCategory
import com.example.stugbygget.domain.model.MaterialSpec
import kotlin.math.ceil

data class MaterialQuantityResult(
    val requiredUnits: Double,
    val roundedUnits: Int
)

/**
 * Calculates how many units of a material are required for a given surface area or length.
 *
 * Uses the formula: `(baseAmount / coveragePerUnit) * (1 + wasteMargin)`
 *
 * The `baseAmount` is derived from the material category:
 * - [MaterialCategory.PAINT] — `areaM2 * layers`
 * - [MaterialCategory.WOOD] — `lengthM`
 * - [MaterialCategory.INSULATION] / [MaterialCategory.TILE] — `areaM2`
 *
 * The result is provided both as a precise [Double] and as a ceiling-rounded [Int]
 * suitable for purchasing decisions.
 */
class CalculateMaterialQuantityUseCase {

    /**
     * @param spec The material specification including coveragePerUnit and wasteMargin.
     * @param areaM2 Surface area in square metres (used for paint, insulation, tiles).
     * @param lengthM Linear length in metres (used for wood/boards).
     * @param layers Number of coats/layers (used for paint calculations).
     * @return [MaterialQuantityResult] with exact and rounded unit counts.
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
