package com.example.stugbygget.feature.gallery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stugbygget.domain.model.PhotoPhase
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
    private val observePhotosUseCase: ObservePhotosUseCase
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
                        projectId = DEFAULT_PROJECT_ID,
                        roomName = filters.roomName,
                        phase = filters.phase
                    )
                }
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { throwable ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = throwable.message ?: "Failed to load photos.")
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

    companion object {
        private const val DEFAULT_PROJECT_ID = "default-project"
    }
}
