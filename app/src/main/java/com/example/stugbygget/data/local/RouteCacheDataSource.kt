package com.example.stugbygget.data.local

import android.content.Context
import com.example.stugbygget.domain.model.RouteMetrics
import com.example.stugbygget.domain.model.RouteSource

class RouteCacheDataSource(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun write(origin: String, destination: String, distanceKm: Double, durationMinutes: Int) {
        val keyPrefix = key(origin, destination)
        prefs.edit()
            .putFloat("${keyPrefix}_distance", distanceKm.toFloat())
            .putInt("${keyPrefix}_duration", durationMinutes)
            .apply()
    }

    fun read(origin: String, destination: String): RouteMetrics? {
        val keyPrefix = key(origin, destination)
        if (!prefs.contains("${keyPrefix}_distance")) return null
        val distance = prefs.getFloat("${keyPrefix}_distance", 0f).toDouble()
        val duration = prefs.getInt("${keyPrefix}_duration", 0)
        return RouteMetrics(distanceKm = distance, durationMinutes = duration, source = RouteSource.CACHE)
    }

    private fun key(origin: String, destination: String): String {
        return "${origin.trim().lowercase()}_${destination.trim().lowercase()}"
            .replace(" ", "_")
    }

    companion object {
        private const val PREFS_NAME = "route_cache"
    }
}
