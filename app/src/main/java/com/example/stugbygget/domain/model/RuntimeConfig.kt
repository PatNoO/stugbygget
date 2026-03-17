package com.example.stugbygget.domain.model

data class RuntimeConfig(
    val enableAiChat: Boolean,
    val enableLivePriceIngestion: Boolean,
    val logisticsFuelCostPerKm: Double,
    val logisticsFreightRatePerKg: Double,
    val shoppingOverspendWarningThreshold: Double
)
