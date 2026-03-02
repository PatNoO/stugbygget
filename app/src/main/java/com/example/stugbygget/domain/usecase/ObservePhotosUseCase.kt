package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.PhotoPhase
import com.example.stugbygget.domain.repository.PhotoRepository

class ObservePhotosUseCase(
    private val repository: PhotoRepository
) {
    operator fun invoke(
        projectId: String,
        roomName: String? = null,
        phase: PhotoPhase? = null
    ) = repository.observePhotos(projectId, roomName, phase)
}
