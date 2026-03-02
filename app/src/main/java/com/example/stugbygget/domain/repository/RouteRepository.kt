package com.example.stugbygget.domain.repository

import com.example.stugbygget.domain.model.RouteMetrics

interface RouteRepository {
    suspend fun getRouteMetrics(origin: String, destination: String): RouteMetrics
}
