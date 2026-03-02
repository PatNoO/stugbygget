package com.example.stugbygget.domain.model

data class MaterialSpec(
    val id: String,
    val name: String,
    val category: MaterialCategory,
    val unitType: UnitType,
    val coveragePerUnit: Double,
    val wasteMargin: Double
)

enum class MaterialCategory {
    PAINT,
    WOOD,
    INSULATION,
    TILE
}

enum class UnitType {
    LITER,
    METER,
    SQUARE_METER,
    ROLL,
    PIECE,
    KG
}
