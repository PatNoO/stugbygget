package com.example.stugbygget.feature.gallery.ui

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stugbygget.domain.model.PhotoItem
import com.example.stugbygget.domain.model.PhotoPhase
import com.example.stugbygget.domain.usecase.DeletePhotoUseCase
import com.example.stugbygget.domain.usecase.ObservePhotosUseCase
import com.example.stugbygget.domain.usecase.UploadPhotoUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GalleryViewModel(
    private val observePhotosUseCase: ObservePhotosUseCase,
    private val uploadPhotoUseCase: UploadPhotoUseCase,
    private val deletePhotoUseCase: DeletePhotoUseCase,
    private val contentResolver: ContentResolver,
    private val currentUserEmail: String,
    private val projectId: String,
) : ViewModel() {

    private data class GalleryFilters(
        val phase: PhotoPhase?
    )

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    init {
        observePhotos()
    }

    fun onPhaseFilterSelected(phase: PhotoPhase?) {
        _uiState.update { it.copy(selectedPhase = phase) }
    }

    // ── Camera capture flow ──

    fun onShowCameraCapture() {
        _uiState.update { it.copy(showCameraCapture = true) }
    }

    fun onDismissCameraCapture() {
        _uiState.update { it.copy(showCameraCapture = false) }
    }

    fun onCameraImageCaptured(uri: Uri) {
        _uiState.update {
            it.copy(
                showCameraCapture = false,
                capturedUri = uri,
                showUploadSheet = true,
                draftPhase = PhotoPhase.DURING,
            )
        }
    }

    fun onViewPhoto(photo: PhotoItem) {
        _uiState.update { it.copy(viewingPhoto = photo) }
    }

    fun onDismissViewer() {
        _uiState.update { it.copy(viewingPhoto = null) }
    }

    fun onRequestDelete(photoId: String) {
        _uiState.update { it.copy(pendingDeleteId = photoId) }
    }

    fun onCancelDelete() {
        _uiState.update { it.copy(pendingDeleteId = null) }
    }

    fun onConfirmDelete() {
        val state = _uiState.value
        val photoId = state.pendingDeleteId ?: return
        val photo = state.photos.firstOrNull { it.id == photoId } ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            runCatching {
                deletePhotoUseCase(projectId, photoId, photo.storagePath, deleteFromStorage = true)
            }.onSuccess {
                _uiState.update { it.copy(isDeleting = false, pendingDeleteId = null, viewingPhoto = null) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        pendingDeleteId = null,
                        errorMessage = throwable.message ?: "Failed to delete photo.",
                    )
                }
            }
        }
    }

    fun onShowAddSheet() {
        _uiState.update {
            it.copy(
                showAddSheet = true,
                draftRoomName = "",
                draftPhase = PhotoPhase.DURING,
                draftPhotos = emptyList(),
                uploadError = null,
            )
        }
    }

    fun onDismissUploadSheet() {
        _uiState.update { it.copy(showUploadSheet = false, capturedUri = null, uploadError = null) }
    }

    fun onDismissAddSheet() {
        _uiState.update { it.copy(showAddSheet = false, uploadError = null) }
    }

    fun onDraftRoomChanged(room: String) = _uiState.update { it.copy(draftRoomName = room, uploadError = null) }
    fun onDraftPhaseChanged(phase: PhotoPhase) = _uiState.update { it.copy(draftPhase = phase) }
    fun onPhotosChanged(photos: List<Pair<Uri, String>>) = _uiState.update { it.copy(draftPhotos = photos) }

    fun onSubmitCapturedPhoto() {
        val state = _uiState.value
        val uri = state.capturedUri ?: return
        if (state.draftRoomName.isBlank()) {
            _uiState.update { it.copy(uploadError = "Room name is required.") }
            return
        }
    }

    fun onSubmitCapturedPhoto() {
        val state = _uiState.value
        val uri = state.capturedUri ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isUploading = true, uploadError = null) }
            runCatching {
                val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IllegalStateException("Could not read captured image.")
                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                val fileName = uri.lastPathSegment ?: "photo_${System.currentTimeMillis()}"
                uploadPhotoUseCase(
                    projectId = projectId,
                    roomName = "",
                    phase = state.draftPhase,
                    description = "",
                    uploadedBy = currentUserEmail,
                    fileName = fileName,
                    contentType = mimeType,
                    bytes = bytes,
                )
            }.onSuccess {
                _uiState.update { it.copy(isUploading = false, showUploadSheet = false, capturedUri = null) }
            }.onFailure { e ->
                _uiState.update { it.copy(isUploading = false, uploadError = e.message ?: "Upload failed.") }
            }
        }
    }

    fun onSubmitPhotos() {
        val state = _uiState.value
        if (state.draftRoomName.isBlank()) {
            _uiState.update { it.copy(uploadError = "Room name is required.") }
            return
        }
    // ── Gallery picker (add-photo) flow ──

    fun onShowAddSheet() {
        _uiState.update {
            it.copy(
                showAddSheet = true,
                draftPhase = PhotoPhase.DURING,
                draftPhotos = emptyList(),
                uploadError = null,
            )
        }
    }

    fun onDismissAddSheet() {
        _uiState.update { it.copy(showAddSheet = false, uploadError = null) }
    }

    fun onDraftPhaseChanged(phase: PhotoPhase) = _uiState.update { it.copy(draftPhase = phase) }
    fun onPhotosChanged(photos: List<Pair<Uri, String>>) = _uiState.update { it.copy(draftPhotos = photos) }

    fun onSubmitPhotos() {
        val state = _uiState.value
        if (state.draftPhotos.isEmpty()) {
            _uiState.update { it.copy(uploadError = "Select at least one photo.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, uploadError = null) }
            var firstError: String? = null
            for ((uri, description) in state.draftPhotos) {
                runCatching {
                    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: throw IllegalStateException("Could not read image data.")
                    val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                    val fileName = uri.lastPathSegment ?: "photo_${System.currentTimeMillis()}"
                    uploadPhotoUseCase(
                        projectId = projectId,
                        roomName = "",
                        phase = state.draftPhase,
                        description = description.trim(),
                        uploadedBy = currentUserEmail,
                        fileName = fileName,
                        contentType = mimeType,
                        bytes = bytes,
                    )
                }.onFailure { e ->
                    if (firstError == null) firstError = e.message ?: "Upload failed."
                }
            }
            _uiState.update {
                it.copy(
                    isUploading = false,
                    showAddSheet = if (firstError == null) false else it.showAddSheet,
                    uploadError = firstError,
                )
            }
        }
    }

    // ── Photo viewer / delete flow ──

    fun onViewPhoto(photo: PhotoItem) {
        _uiState.update { it.copy(viewingPhoto = photo) }
    }

    fun onDismissViewer() {
        _uiState.update { it.copy(viewingPhoto = null) }
    }

    fun onRequestDelete(photoId: String) {
        _uiState.update { it.copy(pendingDeleteId = photoId) }
    }

    fun onCancelDelete() {
        _uiState.update { it.copy(pendingDeleteId = null) }
    }

    fun onConfirmDelete() {
        val state = _uiState.value
        val photoId = state.pendingDeleteId ?: return
        val photo = state.photos.firstOrNull { it.id == photoId } ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            runCatching {
                deletePhotoUseCase(projectId, photoId, photo.storagePath, deleteFromStorage = true)
            }.onSuccess {
                _uiState.update { it.copy(isDeleting = false, pendingDeleteId = null, viewingPhoto = null) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        pendingDeleteId = null,
                        errorMessage = throwable.message ?: "Failed to delete photo.",
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePhotos() {
        viewModelScope.launch {
            _uiState
                .map { state -> GalleryFilters(phase = state.selectedPhase) }
                .distinctUntilChanged()
                .flatMapLatest { filters ->
                    observePhotosUseCase(
                        projectId = projectId,
                        roomName = null,
                        phase = filters.phase
                    )
                }
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Failed to load photos."
                        )
                    }
                }
                .collect { photos ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            photos = photos,
                            errorMessage = null
                        )
                    }
                }
        }
    }
}
