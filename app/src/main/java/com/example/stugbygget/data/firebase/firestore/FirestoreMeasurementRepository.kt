package com.example.stugbygget.data.firebase.firestore

import com.example.stugbygget.core.offline.OfflineSyncCoordinator
import com.example.stugbygget.domain.model.MeasurementRecord
import com.example.stugbygget.domain.repository.MeasurementRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreMeasurementRepository(
    private val firestore: FirebaseFirestore,
    private val offlineSyncCoordinator: OfflineSyncCoordinator
) : MeasurementRepository {

    override suspend fun saveMeasurement(projectId: String, measurement: MeasurementRecord) {
        val payload = hashMapOf(
            "label" to measurement.label.trim(),
            "valueCm" to measurement.valueCm,
            "type" to measurement.type.name,
            "createdAt" to Timestamp.now()
        )
        offlineSyncCoordinator.runOrQueue {
            firestore.collection("projects")
                .document(projectId)
                .collection("measurements")
                .add(payload)
                .await()
        }
    }
}
