package com.example.stugbygget.data.remote.maps

import com.example.stugbygget.BuildConfig
import com.example.stugbygget.data.local.RouteCacheDataSource
import com.example.stugbygget.domain.model.RouteMetrics
import com.example.stugbygget.domain.model.RouteSource
import com.example.stugbygget.domain.repository.RouteRepository
import kotlin.math.roundToInt

class GoogleRouteRepository(
    private val service: GoogleDirectionsService,
    private val cacheDataSource: RouteCacheDataSource
) : RouteRepository {
    override suspend fun getRouteMetrics(origin: String, destination: String): RouteMetrics {
        val apiKey = BuildConfig.MAPS_API_KEY
        if (apiKey.isBlank()) {
            return cacheDataSource.read(origin, destination)
                ?: RouteMetrics(distanceKm = DEFAULT_DISTANCE_KM, durationMinutes = DEFAULT_DURATION_MIN, source = RouteSource.DEFAULT)
        }

        return runCatching {
            val response = service.getDirections(origin = origin, destination = destination, apiKey = apiKey)
            val leg = response.routes.firstOrNull()?.legs?.firstOrNull()
                ?: error("No route leg returned.")
            val distanceKm = leg.distance.value / 1000.0
            val durationMinutes = (leg.duration.value / 60.0).roundToInt()
            cacheDataSource.write(origin, destination, distanceKm, durationMinutes)
            RouteMetrics(
                distanceKm = distanceKm,
                durationMinutes = durationMinutes,
                source = RouteSource.LIVE_API
            )
        }.getOrElse {
            cacheDataSource.read(origin, destination)
                ?: RouteMetrics(distanceKm = DEFAULT_DISTANCE_KM, durationMinutes = DEFAULT_DURATION_MIN, source = RouteSource.DEFAULT)
        }
    }

    companion object {
        private const val DEFAULT_DISTANCE_KM = 25.0
        private const val DEFAULT_DURATION_MIN = 35
    }
}
