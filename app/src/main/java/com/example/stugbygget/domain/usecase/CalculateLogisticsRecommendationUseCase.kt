package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.LogisticsAssumptions
import com.example.stugbygget.domain.model.LogisticsCalculation
import com.example.stugbygget.domain.model.LogisticsInput
import com.example.stugbygget.domain.model.TransportOptionResult
import com.example.stugbygget.domain.model.TransportType
import com.example.stugbygget.domain.repository.LogisticsRepository
import kotlin.math.roundToInt

class CalculateLogisticsRecommendationUseCase(
    private val logisticsRepository: LogisticsRepository
) {

    /**
     * Deterministic recommendation rules:
     * 1) Calculate all three transport options from the same assumptions.
     * 2) Choose the lowest total cost.
     * 3) On equal cost, choose lower time; if still equal, prefer STORE_DELIVERY.
     */
    suspend operator fun invoke(
        projectId: String,
        input: LogisticsInput,
        assumptions: LogisticsAssumptions,
        orderValue: Double
    ): LogisticsCalculation {
        val selfOption = calculateSelfOption(input, assumptions)
        val deliveryOption = calculateDeliveryOption(input, assumptions, orderValue)
        val freightOption = calculateFreightOption(input, assumptions)

        val options = listOf(selfOption, deliveryOption, freightOption)
        val recommended = options
            .sortedWith(
                compareBy<TransportOptionResult> { it.totalCost }
                    .thenBy { it.timeMinutes }
                    .thenBy { transportPriority(it.type) }
            )
            .first()

        val result = LogisticsCalculation(
            shoppingListId = input.shoppingListId,
            distanceKm = input.distanceKm,
            totalWeightKg = input.totalWeightKg,
            totalVolumeM3 = input.totalVolumeM3,
            options = options,
            recommendedType = recommended.type,
            assumptions = assumptions
        )
        logisticsRepository.saveCalculation(projectId, result)
        return result
    }

    private fun calculateSelfOption(
        input: LogisticsInput,
        assumptions: LogisticsAssumptions
    ): TransportOptionResult {
        val roundTripKm = input.distanceKm * 2.0
        val fuelCost = roundTripKm * assumptions.fuelCostPerKm
        val total = fuelCost + assumptions.trailerRentalCost
        val timeMinutes = (roundTripKm / 60.0 * 60.0 + 60.0).roundToInt()
        return TransportOptionResult(
            type = TransportType.SELF,
            totalCost = total,
            timeMinutes = timeMinutes,
            notes = "Includes trailer rental and fuel."
        )
    }

    private fun calculateDeliveryOption(
        input: LogisticsInput,
        assumptions: LogisticsAssumptions,
        orderValue: Double
    ): TransportOptionResult {
        val fee = if (orderValue >= assumptions.deliveryFreeThreshold) 0.0 else assumptions.deliveryBaseFee
        return TransportOptionResult(
            type = TransportType.STORE_DELIVERY,
            totalCost = fee,
            timeMinutes = 0,
            notes = if (fee == 0.0) "Free delivery threshold reached." else "Standard store delivery fee."
        )
    }

    private fun calculateFreightOption(
        input: LogisticsInput,
        assumptions: LogisticsAssumptions
    ): TransportOptionResult {
        val total = input.totalWeightKg * assumptions.freightRatePerKg
        return TransportOptionResult(
            type = TransportType.FREIGHT,
            totalCost = total,
            timeMinutes = 0,
            notes = "Weight-based freight estimate."
        )
    }

    private fun transportPriority(type: TransportType): Int {
        return when (type) {
            TransportType.STORE_DELIVERY -> 0
            TransportType.FREIGHT -> 1
            TransportType.SELF -> 2
        }
    }
}
