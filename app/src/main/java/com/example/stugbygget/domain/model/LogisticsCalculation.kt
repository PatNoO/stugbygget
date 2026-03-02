package com.example.stugbygget.domain.model

import java.time.Instant

data class LogisticsInput(
    val shoppingListId: String,
    val distanceKm: Double,
    val totalWeightKg: Double,
    val totalVolumeM3: Double
)

data class LogisticsAssumptions(
    val fuelCostPerKm: Double,
    val trailerRentalCost: Double,
    val deliveryBaseFee: Double,
    val deliveryFreeThreshold: Double,
    val freightRatePerKg: Double
)

enum class TransportType {
    SELF,
    STORE_DELIVERY,
    FREIGHT
}

data class TransportOptionResult(
    val type: TransportType,
    val totalCost: Double,
    val timeMinutes: Int,
    val notes: String
)

data class LogisticsCalculation(
    val shoppingListId: String,
    val distanceKm: Double,
    val totalWeightKg: Double,
    val totalVolumeM3: Double,
    val options: List<TransportOptionResult>,
    val recommendedType: TransportType,
    val assumptions: LogisticsAssumptions,
    val calculatedAt: Instant = Instant.now()
)
