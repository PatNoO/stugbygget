package com.example.stugbygget.feature.gallery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stugbygget.domain.model.PhotoItem
import com.example.stugbygget.domain.model.PhotoPhase
import com.example.stugbygget.domain.usecase.DeletePhotoUseCase
import com.example.stugbygget.domain.usecase.ObservePhotosUseCase
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
    private val deletePhotoUseCase: DeletePhotoUseCase,
    private val projectId: String
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
                _uiState.update { it.copy(isDeleting = false, pendingDeleteId = null, errorMessage = throwable.message ?: "Failed to delete photo.") }
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
