package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.LogisticsAssumptions
import com.example.stugbygget.domain.model.LogisticsCalculation
import com.example.stugbygget.domain.model.LogisticsInput

class PlanLogisticsWithRouteUseCase(
    private val getRouteMetricsUseCase: GetRouteMetricsUseCase,
    private val calculateLogisticsRecommendationUseCase: CalculateLogisticsRecommendationUseCase
) {
    suspend operator fun invoke(
        projectId: String,
        shoppingListId: String,
        origin: String,
        destination: String,
        totalWeightKg: Double,
        totalVolumeM3: Double,
        orderValue: Double,
        assumptions: LogisticsAssumptions
    ): LogisticsCalculation {
        val route = getRouteMetricsUseCase(origin, destination)
        val input = LogisticsInput(
            shoppingListId = shoppingListId,
            distanceKm = route.distanceKm,
            totalWeightKg = totalWeightKg,
            totalVolumeM3 = totalVolumeM3
        )
        return calculateLogisticsRecommendationUseCase(
            projectId = projectId,
            input = input,
            assumptions = assumptions,
            orderValue = orderValue
        )
    }
}
