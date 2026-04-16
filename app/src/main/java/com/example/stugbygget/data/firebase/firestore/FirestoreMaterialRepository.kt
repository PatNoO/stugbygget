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
import kotlinx.coroutines.tasks.await

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

    override suspend fun seedDefaultMaterials(projectId: String) {
        val col = firestore.collection("projects").document(projectId).collection("materials")
        val batch = firestore.batch()
        defaultMaterials.forEach { m ->
            val doc = col.document()
            batch.set(doc, mapOf(
                "name" to m.name,
                "category" to m.category.name,
                "unitType" to m.unitType.name,
                "coveragePerUnit" to m.coveragePerUnit,
                "wasteMargin" to m.wasteMargin,
            ))
        }
        batch.commit().await()
    }

    companion object {
        private val defaultMaterials = listOf(
            MaterialSpec("", "Fasadfärg",     MaterialCategory.PAINT,      UnitType.LITER,        8.0,  0.10),
            MaterialSpec("", "Träolja",        MaterialCategory.PAINT,      UnitType.LITER,        6.0,  0.10),
            MaterialSpec("", "Innerfärg",      MaterialCategory.PAINT,      UnitType.LITER,        10.0, 0.10),
            MaterialSpec("", "Grundfärg",      MaterialCategory.PAINT,      UnitType.LITER,        12.0, 0.10),
            MaterialSpec("", "Spackel",        MaterialCategory.PAINT,      UnitType.KG,           5.0,  0.15),
            MaterialSpec("", "Terrassbräda",   MaterialCategory.WOOD,       UnitType.METER,        0.12, 0.15),
            MaterialSpec("", "Panel",          MaterialCategory.WOOD,       UnitType.METER,        0.10, 0.15),
            MaterialSpec("", "Reglar 45x70",   MaterialCategory.WOOD,       UnitType.METER,        1.0,  0.10),
            MaterialSpec("", "Rockwool skiva", MaterialCategory.INSULATION, UnitType.SQUARE_METER, 1.0,  0.10),
            MaterialSpec("", "XPS-skiva",      MaterialCategory.INSULATION, UnitType.SQUARE_METER, 1.0,  0.05),
            MaterialSpec("", "Klinker",        MaterialCategory.TILE,       UnitType.SQUARE_METER, 1.0,  0.15),
            MaterialSpec("", "Väggkakel",      MaterialCategory.TILE,       UnitType.SQUARE_METER, 1.0,  0.15),
            MaterialSpec("", "Fogmassa",       MaterialCategory.TILE,       UnitType.KG,           3.0,  0.10),
        )
    }
}
