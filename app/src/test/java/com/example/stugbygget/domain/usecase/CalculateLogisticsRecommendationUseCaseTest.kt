package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.LogisticsAssumptions
import com.example.stugbygget.domain.model.LogisticsCalculation
import com.example.stugbygget.domain.model.LogisticsInput
import com.example.stugbygget.domain.model.TransportType
import com.example.stugbygget.domain.repository.LogisticsRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CalculateLogisticsRecommendationUseCaseTest {

    @Test
    fun `selects cheapest option and persists snapshot`() = runBlocking {
        val repository = FakeLogisticsRepository()
        val useCase = CalculateLogisticsRecommendationUseCase(repository)
        val result = useCase(
            projectId = "default-project",
            input = LogisticsInput(
                shoppingListId = "list-1",
                distanceKm = 125.0,
                totalWeightKg = 850.0,
                totalVolumeM3 = 3.2
            ),
            assumptions = LogisticsAssumptions(
                fuelCostPerKm = 1.52,
                trailerRentalCost = 450.0,
                deliveryBaseFee = 1200.0,
                deliveryFreeThreshold = 10000.0,
                freightRatePerKg = 1.1
            ),
            orderValue = 6500.0
        )

        assertEquals(TransportType.SELF, result.recommendedType)
        assertNotNull(repository.lastSaved)
    }

    private class FakeLogisticsRepository : LogisticsRepository {
        var lastSaved: LogisticsCalculation? = null
        override suspend fun saveCalculation(projectId: String, calculation: LogisticsCalculation) {
            lastSaved = calculation
        }
    }
}
