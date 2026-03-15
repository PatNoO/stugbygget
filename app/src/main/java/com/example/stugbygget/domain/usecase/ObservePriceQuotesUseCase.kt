package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.PriceQuote
import com.example.stugbygget.domain.repository.MaterialRepository
import kotlinx.coroutines.flow.Flow

class ObservePriceQuotesUseCase(private val repository: MaterialRepository) {
    operator fun invoke(projectId: String, materialId: String): Flow<List<PriceQuote>> =
        repository.observePriceQuotes(projectId, materialId)
}
