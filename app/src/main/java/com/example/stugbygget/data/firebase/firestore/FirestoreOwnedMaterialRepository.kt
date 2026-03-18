package com.example.stugbygget.data.firebase.firestore

import com.example.stugbygget.domain.model.OwnedMaterial
import com.example.stugbygget.domain.repository.OwnedMaterialRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreOwnedMaterialRepository(
    private val firestore: FirebaseFirestore,
) : OwnedMaterialRepository {

    private fun collection(projectId: String) = firestore
        .collection("projects")
        .document(projectId)
        .collection("owned_materials")

    override fun observeOwnedMaterials(projectId: String): Flow<List<OwnedMaterial>> = callbackFlow {
        val registration = collection(projectId).addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val items = snapshot?.documents.orEmpty().mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                runCatching {
                    OwnedMaterial(
                        id = doc.id,
                        name = data["name"] as? String ?: return@runCatching null,
                        quantity = (data["quantity"] as? Number)?.toDouble() ?: 0.0,
                        unit = data["unit"] as? String ?: "",
                        notes = data["notes"] as? String ?: "",
                    )
                }.getOrNull()
            }.filterNotNull()
            trySend(items)
        }
        awaitClose { registration.remove() }
    }

    override suspend fun upsertOwnedMaterial(projectId: String, material: OwnedMaterial) {
        val data = mapOf(
            "name" to material.name,
            "quantity" to material.quantity,
            "unit" to material.unit,
            "notes" to material.notes,
        )
        if (material.id.isBlank()) {
            collection(projectId).add(data).await()
        } else {
            collection(projectId).document(material.id).set(data).await()
        }
    }

    override suspend fun deleteOwnedMaterial(projectId: String, id: String) {
        collection(projectId).document(id).delete().await()
    }
}
