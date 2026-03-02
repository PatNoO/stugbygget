package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.RouteMetrics
import com.example.stugbygget.domain.repository.RouteRepository

class GetRouteMetricsUseCase(
    private val routeRepository: RouteRepository
) {
    suspend operator fun invoke(origin: String, destination: String): RouteMetrics {
        return routeRepository.getRouteMetrics(origin, destination)
    }
}
