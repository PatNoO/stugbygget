package com.example.stugbygget.domain.model

data class PriceQuote(
    val materialName: String,
    val store: String,
    val unitPrice: Double,
    val inStock: Boolean,
    val productUrl: String
)

data class StoreTotal(
    val store: String,
    val totalCost: Double
)

data class BestSplitLine(
    val itemName: String,
    val store: String,
    val unitPrice: Double
)

data class PriceComparisonResult(
    val singleStoreTotals: List<StoreTotal>,
    val cheapestSingleStore: StoreTotal?,
    val bestSplitLines: List<BestSplitLine>,
    val bestSplitTotal: Double
)
