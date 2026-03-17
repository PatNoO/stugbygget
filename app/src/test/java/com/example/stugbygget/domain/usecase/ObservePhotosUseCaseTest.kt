package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.PhotoItem
import com.example.stugbygget.domain.model.PhotoPhase
import com.example.stugbygget.domain.repository.PhotoRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ObservePhotosUseCaseTest {

    @Test
    fun `returns photos from repository`() = runBlocking {
        val expected = listOf(mockPhoto("p1"))
        val repo = FakePhotoRepository(photos = expected)
        val useCase = ObservePhotosUseCase(repo)

        val actual = useCase("project-1").first()

        assertEquals(expected, actual)
    }

    @Test
    fun `forwards roomName filter to repository`() = runBlocking {
        val repo = FakePhotoRepository()
        val useCase = ObservePhotosUseCase(repo)

        useCase("project-1", roomName = "Kök").first()

        assertEquals("Kök", repo.lastRoomName)
    }

    @Test
    fun `forwards phase filter to repository`() = runBlocking {
        val repo = FakePhotoRepository()
        val useCase = ObservePhotosUseCase(repo)

        useCase("project-1", phase = PhotoPhase.AFTER).first()

        assertEquals(PhotoPhase.AFTER, repo.lastPhase)
    }

    @Test
    fun `returns empty list when repository has no photos`() = runBlocking {
        val repo = FakePhotoRepository(photos = emptyList())
        val useCase = ObservePhotosUseCase(repo)

        val actual = useCase("project-1").first()

        assertEquals(emptyList<PhotoItem>(), actual)
    }

    // ── Fake ──────────────────────────────────────────────────────────────────

    private class FakePhotoRepository(
        private val photos: List<PhotoItem> = emptyList()
    ) : PhotoRepository {
        var lastRoomName: String? = null
        var lastPhase: PhotoPhase? = null

        override fun observePhotos(
            projectId: String,
            roomName: String?,
            phase: PhotoPhase?
        ): Flow<List<PhotoItem>> {
            lastRoomName = roomName
            lastPhase = phase
            return flowOf(photos)
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
        ) = Unit

        override suspend fun deletePhoto(
            projectId: String,
            photoId: String,
            storagePath: String,
            deleteFromStorage: Boolean
        ) = Unit
    }
}

internal fun mockPhoto(
    id: String = "photo-1",
    roomName: String = "Kök",
    phase: PhotoPhase = PhotoPhase.DURING,
    description: String = "Progress photo",
    storagePath: String = "projects/p1/photos/photo-1-img.jpg",
    downloadUrl: String = "https://example.com/photo-1.jpg",
    uploadedBy: String = "user-1"
): PhotoItem = PhotoItem(
    id = id,
    roomName = roomName,
    phase = phase,
    description = description,
    storagePath = storagePath,
    downloadUrl = downloadUrl,
    takenAt = Instant.parse("2026-06-01T00:00:00Z"),
    uploadedBy = uploadedBy
)
