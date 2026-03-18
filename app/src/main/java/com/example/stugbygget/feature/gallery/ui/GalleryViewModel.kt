package com.example.stugbygget.feature.gallery.ui

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stugbygget.domain.model.PhotoPhase
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
    private val contentResolver: ContentResolver,
    private val currentUserEmail: String,
    private val projectId: String,
) : ViewModel() {

    private data class GalleryFilters(
        val roomName: String?,
        val phase: PhotoPhase?
    )

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    init {
        observePhotos()
    }

    fun onRoomFilterSelected(room: String?) {
        _uiState.update { it.copy(selectedRoom = room) }
    }

    fun onPhaseFilterSelected(phase: PhotoPhase?) {
        _uiState.update { it.copy(selectedPhase = phase) }
    }

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
                draftRoomName = "",
                draftPhase = PhotoPhase.DURING,
                uploadError = null,
            )
        }
    }

    fun onDismissUploadSheet() {
        _uiState.update {
            it.copy(showUploadSheet = false, capturedUri = null, uploadError = null)
        }
    }

    fun onDraftRoomChanged(room: String) = _uiState.update { it.copy(draftRoomName = room, uploadError = null) }
    fun onDraftPhaseChanged(phase: PhotoPhase) = _uiState.update { it.copy(draftPhase = phase) }

    fun onSubmitCapturedPhoto() {
        val state = _uiState.value
        val uri = state.capturedUri ?: return
        if (state.draftRoomName.isBlank()) {
            _uiState.update { it.copy(uploadError = "Room name is required.") }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isUploading = true, uploadError = null) }
            runCatching {
                val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IllegalStateException("Could not read captured image.")
                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                val fileName = uri.lastPathSegment ?: "photo_${System.currentTimeMillis()}"
                uploadPhotoUseCase(
                    projectId = projectId,
                    roomName = state.draftRoomName.trim(),
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePhotos() {
        viewModelScope.launch {
            _uiState
                .map { state ->
                    GalleryFilters(
                        roomName = state.selectedRoom,
                        phase = state.selectedPhase
                    )
                }
                .distinctUntilChanged()
                .flatMapLatest { filters ->
                    observePhotosUseCase(
                        projectId = projectId,
                        roomName = filters.roomName,
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
                            availableRooms = photos.map { photo -> photo.roomName }.distinct().sorted(),
                            errorMessage = null
                        )
                    }
                }
        }
    }
}
