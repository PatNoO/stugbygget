package com.example.stugbygget.data.firebase.firestore

import com.example.stugbygget.domain.model.PriceComparisonResult
import com.example.stugbygget.domain.repository.PriceRecommendationRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestorePriceRecommendationRepository(
    private val firestore: FirebaseFirestore
) : PriceRecommendationRepository {
    override suspend fun saveSnapshot(
        projectId: String,
        shoppingListId: String,
        result: PriceComparisonResult
    ) {
        val payload = mapOf(
            "shoppingListId" to shoppingListId,
            "singleStoreTotals" to result.singleStoreTotals.map {
                mapOf("store" to it.store, "totalCost" to it.totalCost)
            },
            "cheapestSingleStore" to result.cheapestSingleStore?.let {
                mapOf("store" to it.store, "totalCost" to it.totalCost)
            },
            "bestSplitLines" to result.bestSplitLines.map {
                mapOf("itemName" to it.itemName, "store" to it.store, "unitPrice" to it.unitPrice)
            },
            "bestSplitTotal" to result.bestSplitTotal,
            "createdAt" to Timestamp.now()
        )

        firestore.collection("projects")
            .document(projectId)
            .collection("shopping_recommendations")
            .add(payload)
            .await()
    }
}
