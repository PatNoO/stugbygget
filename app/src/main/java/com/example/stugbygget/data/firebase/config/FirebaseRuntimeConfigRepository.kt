package com.example.stugbygget.data.firebase.config

import com.example.stugbygget.domain.model.RuntimeConfig
import com.example.stugbygget.domain.repository.RuntimeConfigRepository
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.tasks.await

class FirebaseRuntimeConfigRepository(
    private val remoteConfig: FirebaseRemoteConfig
) : RuntimeConfigRepository {

    init {
        val settings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        remoteConfig.setConfigSettingsAsync(settings)
        remoteConfig.setDefaultsAsync(defaults)
    }

    override suspend fun fetchAndActivate(): RuntimeConfig {
        runCatching { remoteConfig.fetchAndActivate().await() }
        return mapConfig()
    }

    override fun getCached(): RuntimeConfig = mapConfig()

    private fun mapConfig(): RuntimeConfig {
        return RuntimeConfig(
            enableAiChat = remoteConfig.getBoolean(KEY_ENABLE_AI_CHAT),
            enableLivePriceIngestion = remoteConfig.getBoolean(KEY_ENABLE_LIVE_PRICE_INGESTION),
            logisticsFuelCostPerKm = remoteConfig.getDouble(KEY_LOGISTICS_FUEL_COST_PER_KM),
            logisticsFreightRatePerKg = remoteConfig.getDouble(KEY_LOGISTICS_FREIGHT_RATE_PER_KG),
            shoppingOverspendWarningThreshold = remoteConfig.getDouble(KEY_SHOPPING_OVERSPEND_WARNING_THRESHOLD)
        )
    }

    companion object {
        const val KEY_ENABLE_AI_CHAT = "feature_enable_ai_chat"
        const val KEY_ENABLE_LIVE_PRICE_INGESTION = "feature_enable_live_price_ingestion"
        const val KEY_LOGISTICS_FUEL_COST_PER_KM = "calc_logistics_fuel_cost_per_km"
        const val KEY_LOGISTICS_FREIGHT_RATE_PER_KG = "calc_logistics_freight_rate_per_kg"
        const val KEY_SHOPPING_OVERSPEND_WARNING_THRESHOLD = "calc_shopping_overspend_warning_threshold"

        private val defaults = mapOf(
            KEY_ENABLE_AI_CHAT to true,
            KEY_ENABLE_LIVE_PRICE_INGESTION to false,
            KEY_LOGISTICS_FUEL_COST_PER_KM to 1.52,
            KEY_LOGISTICS_FREIGHT_RATE_PER_KG to 1.10,
            KEY_SHOPPING_OVERSPEND_WARNING_THRESHOLD to 1.0
        )
    }
}
