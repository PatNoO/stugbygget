package com.example.stugbygget.data.firebase.firestore

import com.example.stugbygget.domain.model.PhotoItem
import com.example.stugbygget.domain.model.PhotoPhase
import com.example.stugbygget.domain.repository.PhotoRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.util.UUID

class FirestorePhotoRepository(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : PhotoRepository {

    override fun observePhotos(
        projectId: String,
        roomName: String?,
        phase: PhotoPhase?
    ): Flow<List<PhotoItem>> = callbackFlow {
        val query = firestore.collection("projects")
            .document(projectId)
            .collection("photos")

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val photos = snapshot?.documents.orEmpty()
                .mapNotNull { doc ->
                    val map = doc.data ?: return@mapNotNull null
                    runCatching { PhotoDocumentMapper.fromMap(doc.id, map) }.getOrNull()
                }
                .filter { item -> roomName == null || item.roomName == roomName }
                .filter { item -> phase == null || item.phase == phase }
                .sortedByDescending { it.takenAt }

            trySend(photos)
        }

        awaitClose { registration.remove() }
    }

    override suspend fun uploadPhoto(
        projectId: String,
        roomName: String,
        phase: PhotoPhase,
        description: String,
        uploadedBy: String,
        fileName: String,
        contentType: String,
        bytes: ByteArray
    ) {
        require(roomName.isNotBlank()) { "roomName must not be blank" }
        require(fileName.isNotBlank()) { "fileName must not be blank" }
        require(bytes.isNotEmpty()) { "photo bytes must not be empty" }

        val photoId = UUID.randomUUID().toString()
        val storagePath = "projects/$projectId/photos/$photoId-$fileName"
        val ref = storage.reference.child(storagePath)

        val metadata = com.google.firebase.storage.StorageMetadata.Builder()
            .setContentType(contentType)
            .build()

        ref.putBytes(bytes, metadata).await()
        val downloadUrl = ref.downloadUrl.await().toString()

        val photo = PhotoItem(
            id = photoId,
            roomName = roomName,
            phase = phase,
            description = description,
            storagePath = storagePath,
            downloadUrl = downloadUrl,
            takenAt = Instant.now(),
            uploadedBy = uploadedBy
        )

        firestore.collection("projects")
            .document(projectId)
            .collection("photos")
            .document(photoId)
            .set(PhotoDocumentMapper.toMap(photo))
            .await()
    }

    override suspend fun deletePhoto(
        projectId: String,
        photoId: String,
        storagePath: String,
        deleteFromStorage: Boolean
    ) {
        firestore.collection("projects")
            .document(projectId)
            .collection("photos")
            .document(photoId)
            .delete()
            .await()

        if (deleteFromStorage && storagePath.isNotBlank()) {
            storage.reference.child(storagePath).delete().await()
        }
    }
}
