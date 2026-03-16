package com.example.stugbygget.data.firebase.firestore

import com.example.stugbygget.domain.model.RenovationPhase
import com.example.stugbygget.domain.repository.PhaseRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestorePhaseRepository(
    private val firestore: FirebaseFirestore
) : PhaseRepository {

    override fun observePhases(projectId: String): Flow<List<RenovationPhase>> = callbackFlow {
        val query = firestore.collection("projects")
            .document(projectId)
            .collection("phases")
            .orderBy("startDate", Query.Direction.ASCENDING)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val phases = snapshot?.documents.orEmpty().mapNotNull { document ->
                val data = document.data ?: return@mapNotNull null
                runCatching { PhaseDocumentMapper.fromMap(document.id, data) }.getOrNull()
            }
            trySend(phases)
        }

        awaitClose { registration.remove() }
    }

    override suspend fun upsertPhase(projectId: String, phase: RenovationPhase) {
        firestore.collection("projects")
            .document(projectId)
            .collection("phases")
            .document(phase.id)
            .set(PhaseDocumentMapper.toMap(phase))
            .await()
    }
}
