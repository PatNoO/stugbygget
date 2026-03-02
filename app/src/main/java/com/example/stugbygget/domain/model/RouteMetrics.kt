package com.example.stugbygget.domain.model

data class RouteMetrics(
    val distanceKm: Double,
    val durationMinutes: Int,
    val source: RouteSource
)

enum class RouteSource {
    LIVE_API,
    CACHE,
    DEFAULT
}
