package com.example.stugbygget.domain.repository

import com.example.stugbygget.domain.model.MaterialSpec
import com.example.stugbygget.domain.model.PriceQuote
import kotlinx.coroutines.flow.Flow

interface MaterialRepository {
    fun observeMaterials(projectId: String): Flow<List<MaterialSpec>>
    fun observePriceQuotes(projectId: String, materialId: String): Flow<List<PriceQuote>>
}
