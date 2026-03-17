package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.PhotoItem
import com.example.stugbygget.domain.model.PhotoPhase
import com.example.stugbygget.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeletePhotoUseCaseTest {

    @Test
    fun `delegates delete to repository`() = runBlocking {
        val repo = FakePhotoRepository()
        val useCase = DeletePhotoUseCase(repo)

        useCase("project-1", "photo-abc", "projects/p1/photos/photo-abc.jpg")

        assertEquals("project-1", repo.lastProjectId)
        assertEquals("photo-abc", repo.lastPhotoId)
        assertEquals("projects/p1/photos/photo-abc.jpg", repo.lastStoragePath)
    }

    @Test
    fun `deleteFromStorage defaults to true`() = runBlocking {
        val repo = FakePhotoRepository()
        val useCase = DeletePhotoUseCase(repo)

        useCase("project-1", "photo-1", "some/path.jpg")

        assertTrue(repo.lastDeleteFromStorage == true)
    }

    @Test
    fun `deleteFromStorage can be set to false`() = runBlocking {
        val repo = FakePhotoRepository()
        val useCase = DeletePhotoUseCase(repo)

        useCase("project-1", "photo-1", "some/path.jpg", deleteFromStorage = false)

        assertEquals(false, repo.lastDeleteFromStorage)
    }

    // ── Fake ──────────────────────────────────────────────────────────────────

    private class FakePhotoRepository : PhotoRepository {
        var lastProjectId: String? = null
        var lastPhotoId: String? = null
        var lastStoragePath: String? = null
        var lastDeleteFromStorage: Boolean? = null

        override fun observePhotos(
            projectId: String,
            roomName: String?,
            phase: PhotoPhase?
        ): Flow<List<PhotoItem>> = flowOf(emptyList())

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
        ) {
            lastProjectId = projectId
            lastPhotoId = photoId
            lastStoragePath = storagePath
            lastDeleteFromStorage = deleteFromStorage
        }
    }
}
