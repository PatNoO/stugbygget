package com.example.stugbygget.domain.repository

import com.example.stugbygget.domain.model.PriceComparisonResult

interface PriceRecommendationRepository {
    suspend fun saveSnapshot(
        projectId: String,
        shoppingListId: String,
        result: PriceComparisonResult
    )
}
