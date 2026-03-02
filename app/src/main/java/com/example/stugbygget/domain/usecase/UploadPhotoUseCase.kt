package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.PhotoPhase
import com.example.stugbygget.domain.repository.PhotoRepository

class UploadPhotoUseCase(
    private val repository: PhotoRepository
) {
    suspend operator fun invoke(
        projectId: String,
        roomName: String,
        phase: PhotoPhase,
        description: String,
        uploadedBy: String,
        fileName: String,
        contentType: String,
        bytes: ByteArray
    ) = repository.uploadPhoto(
        projectId,
        roomName,
        phase,
        description,
        uploadedBy,
        fileName,
        contentType,
        bytes
    )
}
