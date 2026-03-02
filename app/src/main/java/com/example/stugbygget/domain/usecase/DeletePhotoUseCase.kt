package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.repository.PhotoRepository

class DeletePhotoUseCase(
    private val repository: PhotoRepository
) {
    suspend operator fun invoke(
        projectId: String,
        photoId: String,
        storagePath: String,
        deleteFromStorage: Boolean = true
    ) = repository.deletePhoto(projectId, photoId, storagePath, deleteFromStorage)
}
