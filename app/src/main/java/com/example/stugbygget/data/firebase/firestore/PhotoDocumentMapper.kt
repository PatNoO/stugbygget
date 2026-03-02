package com.example.stugbygget.data.firebase.firestore

import com.example.stugbygget.domain.model.PhotoItem
import com.example.stugbygget.domain.model.PhotoPhase
import com.google.firebase.Timestamp

object PhotoDocumentMapper {

    fun fromMap(id: String, map: Map<String, Any?>): PhotoItem {
        return PhotoItem(
            id = id,
            roomName = map["roomName"] as? String ?: "",
            phase = parsePhase(map["phase"] as? String),
            description = map["description"] as? String ?: "",
            storagePath = map["storagePath"] as? String ?: "",
            downloadUrl = map["downloadUrl"] as? String ?: "",
            takenAt = (map["takenAt"] as? Timestamp)?.toDate()?.toInstant() ?: java.time.Instant.now(),
            uploadedBy = map["uploadedBy"] as? String ?: ""
        )
    }

    fun toMap(photo: PhotoItem): Map<String, Any> = mapOf(
        "roomName" to photo.roomName,
        "phase" to photo.phase.name,
        "description" to photo.description,
        "storagePath" to photo.storagePath,
        "downloadUrl" to photo.downloadUrl,
        "takenAt" to Timestamp(photo.takenAt.epochSecond, 0),
        "uploadedBy" to photo.uploadedBy
    )

    private fun parsePhase(raw: String?): PhotoPhase = when (raw) {
        PhotoPhase.BEFORE.name -> PhotoPhase.BEFORE
        PhotoPhase.AFTER.name -> PhotoPhase.AFTER
        else -> PhotoPhase.DURING
    }
}
