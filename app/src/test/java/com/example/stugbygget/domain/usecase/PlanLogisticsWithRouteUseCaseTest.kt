package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.LogisticsAssumptions
import com.example.stugbygget.domain.model.LogisticsCalculation
import com.example.stugbygget.domain.model.RouteMetrics
import com.example.stugbygget.domain.model.RouteSource
import com.example.stugbygget.domain.repository.LogisticsRepository
import com.example.stugbygget.domain.repository.RouteRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class PlanLogisticsWithRouteUseCaseTest {

    @Test
    fun `uses route distance in logistics calculation`() = runBlocking {
        val routeRepository = object : RouteRepository {
            override suspend fun getRouteMetrics(origin: String, destination: String): RouteMetrics {
                return RouteMetrics(distanceKm = 42.0, durationMinutes = 55, source = RouteSource.DEFAULT)
            }
        }
        val logisticsRepository = FakeLogisticsRepository()
        val calculateUseCase = CalculateLogisticsRecommendationUseCase(logisticsRepository)
        val useCase = PlanLogisticsWithRouteUseCase(
            getRouteMetricsUseCase = GetRouteMetricsUseCase(routeRepository),
            calculateLogisticsRecommendationUseCase = calculateUseCase
        )

        val result = useCase(
            projectId = "default-project",
            shoppingListId = "list-1",
            origin = "Store A",
            destination = "Cottage",
            totalWeightKg = 400.0,
            totalVolumeM3 = 2.0,
            orderValue = 3000.0,
            assumptions = LogisticsAssumptions(
                fuelCostPerKm = 1.0,
                trailerRentalCost = 200.0,
                deliveryBaseFee = 1000.0,
                deliveryFreeThreshold = 9000.0,
                freightRatePerKg = 3.0
            )
        )

        assertEquals(42.0, result.distanceKm, 0.0001)
    }

    private class FakeLogisticsRepository : LogisticsRepository {
        override suspend fun saveCalculation(projectId: String, calculation: LogisticsCalculation) = Unit
    }
}
