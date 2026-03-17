package com.example.stugbygget.feature.gallery.ui

import com.example.stugbygget.domain.model.PhotoItem
import com.example.stugbygget.domain.model.PhotoPhase
import com.example.stugbygget.domain.repository.PhotoRepository
import com.example.stugbygget.domain.usecase.ObservePhotosUseCase
import com.example.stugbygget.domain.usecase.mockPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GalleryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial state has isLoading false after flow emits`() = runTest {
        val vm = buildViewModel(photos = emptyList())

        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.errorMessage)
    }

    // ── Photo loading ─────────────────────────────────────────────────────────

    @Test
    fun `photos are loaded into state`() = runTest {
        val photos = listOf(mockPhoto("p1"), mockPhoto("p2"))
        val vm = buildViewModel(photos = photos)

        assertEquals(photos, vm.uiState.value.photos)
    }

    @Test
    fun `availableRooms derived from loaded photos sorted alphabetically`() = runTest {
        val photos = listOf(
            mockPhoto("p1", roomName = "Sovrum"),
            mockPhoto("p2", roomName = "Kök"),
            mockPhoto("p3", roomName = "Kök"),
        )
        val vm = buildViewModel(photos = photos)

        assertEquals(listOf("Kök", "Sovrum"), vm.uiState.value.availableRooms)
    }

    @Test
    fun `error from repository sets errorMessage`() = runTest {
        val vm = buildViewModel(throwError = true)

        assertNotNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isLoading)
    }

    // ── Room filter ───────────────────────────────────────────────────────────

    @Test
    fun `onRoomFilterSelected updates selectedRoom`() = runTest {
        val vm = buildViewModel()

        vm.onRoomFilterSelected("Kök")

        assertEquals("Kök", vm.uiState.value.selectedRoom)
    }

    @Test
    fun `onRoomFilterSelected with null clears filter`() = runTest {
        val vm = buildViewModel()
        vm.onRoomFilterSelected("Kök")

        vm.onRoomFilterSelected(null)

        assertNull(vm.uiState.value.selectedRoom)
    }

    // ── Phase filter ──────────────────────────────────────────────────────────

    @Test
    fun `onPhaseFilterSelected updates selectedPhase`() = runTest {
        val vm = buildViewModel()

        vm.onPhaseFilterSelected(PhotoPhase.BEFORE)

        assertEquals(PhotoPhase.BEFORE, vm.uiState.value.selectedPhase)
    }

    @Test
    fun `onPhaseFilterSelected with null clears filter`() = runTest {
        val vm = buildViewModel()
        vm.onPhaseFilterSelected(PhotoPhase.AFTER)

        vm.onPhaseFilterSelected(null)

        assertNull(vm.uiState.value.selectedPhase)
    }

    @Test
    fun `both filters can be set independently`() = runTest {
        val vm = buildViewModel()

        vm.onRoomFilterSelected("Badrum")
        vm.onPhaseFilterSelected(PhotoPhase.AFTER)

        assertEquals("Badrum", vm.uiState.value.selectedRoom)
        assertEquals(PhotoPhase.AFTER, vm.uiState.value.selectedPhase)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildViewModel(
        photos: List<PhotoItem> = emptyList(),
        throwError: Boolean = false,
        repo: FakePhotoRepository = FakePhotoRepository(photos = photos, throwOnObserve = throwError)
    ): GalleryViewModel = GalleryViewModel(
        observePhotosUseCase = ObservePhotosUseCase(repo),
        projectId = "project-1"
    )

    private class FakePhotoRepository(
        private val photos: List<PhotoItem> = emptyList(),
        private val throwOnObserve: Boolean = false
    ) : PhotoRepository {
        override fun observePhotos(
            projectId: String,
            roomName: String?,
            phase: PhotoPhase?
        ): Flow<List<PhotoItem>> {
            if (throwOnObserve) throw RuntimeException("Firestore unavailable")
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
