package com.example.stugbygget.data.firebase.firestore

import com.example.stugbygget.domain.model.LogisticsCalculation
import com.example.stugbygget.domain.repository.LogisticsRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreLogisticsRepository(
    private val firestore: FirebaseFirestore
) : LogisticsRepository {
    override suspend fun saveCalculation(projectId: String, calculation: LogisticsCalculation) {
        val payload = mapOf(
            "shoppingListId" to calculation.shoppingListId,
            "distanceKm" to calculation.distanceKm,
            "totalWeightKg" to calculation.totalWeightKg,
            "totalVolumeM3" to calculation.totalVolumeM3,
            "options" to calculation.options.map { option ->
                mapOf(
                    "type" to option.type.name,
                    "totalCost" to option.totalCost,
                    "timeMinutes" to option.timeMinutes,
                    "notes" to option.notes
                )
            },
            "recommendedType" to calculation.recommendedType.name,
            "assumptions" to mapOf(
                "fuelCostPerKm" to calculation.assumptions.fuelCostPerKm,
                "trailerRentalCost" to calculation.assumptions.trailerRentalCost,
                "deliveryBaseFee" to calculation.assumptions.deliveryBaseFee,
                "deliveryFreeThreshold" to calculation.assumptions.deliveryFreeThreshold,
                "freightRatePerKg" to calculation.assumptions.freightRatePerKg
            ),
            "calculatedAt" to Timestamp.now()
        )

        firestore.collection("projects")
            .document(projectId)
            .collection("transport")
            .add(payload)
            .await()
    }
}
