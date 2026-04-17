package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.PriceQuote
import com.example.stugbygget.domain.repository.MaterialRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observes live price quotes for a specific material from all available suppliers.
 *
 * Emits a new list of [PriceQuote] objects whenever the underlying Firestore
 * `prices` subcollection changes. Each quote contains the supplier name, unit
 * price, currency, and last-updated timestamp — enabling real-time price
 * comparison in the material detail screen.
 *
 * @param projectId The active renovation project ID.
 * @param materialId The ID of the material to observe quotes for.
 * @return A cold [Flow] that emits on every price update.
 */
class ObservePriceQuotesUseCase(private val repository: MaterialRepository) {
    operator fun invoke(projectId: String, materialId: String): Flow<List<PriceQuote>> =
        repository.observePriceQuotes(projectId, materialId)
}
