package com.example.stugbygget.domain.repository

import com.example.stugbygget.domain.model.PhotoItem
import com.example.stugbygget.domain.model.PhotoPhase
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {
    fun observePhotos(
        projectId: String,
        roomName: String? = null,
        phase: PhotoPhase? = null
    ): Flow<List<PhotoItem>>

    suspend fun uploadPhoto(
        projectId: String,
        roomName: String,
        phase: PhotoPhase,
        description: String,
        uploadedBy: String,
        fileName: String,
        contentType: String,
        bytes: ByteArray
    )

    suspend fun deletePhoto(
        projectId: String,
        photoId: String,
        storagePath: String,
        deleteFromStorage: Boolean = true
    )
}
