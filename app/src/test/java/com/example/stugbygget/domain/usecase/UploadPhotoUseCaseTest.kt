package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.PhotoItem
import com.example.stugbygget.domain.model.PhotoPhase
import com.example.stugbygget.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class UploadPhotoUseCaseTest {

    @Test
    fun `delegates upload to repository`() = runBlocking {
        val repo = FakePhotoRepository()
        val useCase = UploadPhotoUseCase(repo)

        useCase(
            projectId = "project-1",
            roomName = "Kök",
            phase = PhotoPhase.BEFORE,
            description = "Before demo",
            uploadedBy = "user-1",
            fileName = "img.jpg",
            contentType = "image/jpeg",
            bytes = byteArrayOf(1, 2, 3)
        )

        assertNotNull(repo.lastUpload)
        assertEquals("project-1", repo.lastUpload?.projectId)
        assertEquals("Kök", repo.lastUpload?.roomName)
        assertEquals(PhotoPhase.BEFORE, repo.lastUpload?.phase)
    }

    @Test
    fun `passes all parameters to repository`() = runBlocking {
        val repo = FakePhotoRepository()
        val useCase = UploadPhotoUseCase(repo)
        val bytes = byteArrayOf(10, 20, 30)

        useCase(
            projectId = "proj-x",
            roomName = "Badrum",
            phase = PhotoPhase.AFTER,
            description = "Done",
            uploadedBy = "user-2",
            fileName = "photo.png",
            contentType = "image/png",
            bytes = bytes
        )

        val upload = repo.lastUpload!!
        assertEquals("proj-x", upload.projectId)
        assertEquals("Badrum", upload.roomName)
        assertEquals(PhotoPhase.AFTER, upload.phase)
        assertEquals("Done", upload.description)
        assertEquals("user-2", upload.uploadedBy)
        assertEquals("photo.png", upload.fileName)
        assertEquals("image/png", upload.contentType)
        assertEquals(bytes.toList(), upload.bytes.toList())
    }

    // ── Fake ──────────────────────────────────────────────────────────────────

    data class CapturedUpload(
        val projectId: String,
        val roomName: String,
        val phase: PhotoPhase,
        val description: String,
        val uploadedBy: String,
        val fileName: String,
        val contentType: String,
        val bytes: ByteArray
    )

    private class FakePhotoRepository : PhotoRepository {
        var lastUpload: CapturedUpload? = null

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
        ) {
            lastUpload = CapturedUpload(projectId, roomName, phase, description, uploadedBy, fileName, contentType, bytes)
        }

        override suspend fun deletePhoto(
            projectId: String,
            photoId: String,
            storagePath: String,
            deleteFromStorage: Boolean
        ) = Unit
    }
}
