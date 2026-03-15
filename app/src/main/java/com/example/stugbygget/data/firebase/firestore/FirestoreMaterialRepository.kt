package com.example.stugbygget.data.firebase.firestore

import com.example.stugbygget.domain.model.MaterialCategory
import com.example.stugbygget.domain.model.MaterialSpec
import com.example.stugbygget.domain.model.PriceQuote
import com.example.stugbygget.domain.model.UnitType
import com.example.stugbygget.domain.repository.MaterialRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirestoreMaterialRepository(
    private val firestore: FirebaseFirestore,
) : MaterialRepository {

    override fun observeMaterials(projectId: String): Flow<List<MaterialSpec>> = callbackFlow {
        val ref = firestore.collection("projects")
            .document(projectId)
            .collection("materials")

        val registration = ref.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val materials = snapshot?.documents.orEmpty().mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                runCatching {
                    MaterialSpec(
                        id = doc.id,
                        name = data["name"] as? String ?: return@runCatching null,
                        category = MaterialCategory.valueOf(
                            (data["category"] as? String ?: "PAINT").uppercase()
                        ),
                        unitType = UnitType.valueOf(
                            (data["unitType"] as? String ?: "LITER").uppercase()
                        ),
                        coveragePerUnit = (data["coveragePerUnit"] as? Number)?.toDouble() ?: 1.0,
                        wasteMargin = (data["wasteMargin"] as? Number)?.toDouble() ?: 0.1,
                    )
                }.getOrNull()
            }.filterNotNull()
            trySend(materials)
        }

        awaitClose { registration.remove() }
    }

    override fun observePriceQuotes(projectId: String, materialId: String): Flow<List<PriceQuote>> =
        callbackFlow {
            val ref = firestore.collection("projects")
                .document(projectId)
                .collection("prices")
                .whereEqualTo("materialId", materialId)

            val registration = ref.addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val quotes = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    runCatching {
                        PriceQuote(
                            materialName = data["materialName"] as? String ?: "",
                            store = data["store"] as? String ?: "",
                            unitPrice = (data["unitPrice"] as? Number)?.toDouble() ?: 0.0,
                            inStock = data["inStock"] as? Boolean ?: false,
                            productUrl = data["productUrl"] as? String ?: "",
                        )
                    }.getOrNull()
                }.filterNotNull()
                trySend(quotes)
            }

            awaitClose { registration.remove() }
        }
}
