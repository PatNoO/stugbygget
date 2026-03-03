package com.example.stugbygget.feature.gallery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stugbygget.domain.model.PhotoPhase
import com.example.stugbygget.domain.usecase.ObservePhotosUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GalleryViewModel(
    private val observePhotosUseCase: ObservePhotosUseCase,
    private val projectId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    init {
        observePhotos()
    }

    fun onRoomFilterSelected(room: String?) {
        _uiState.update { it.copy(selectedRoom = room) }
        observePhotos()
    }

    fun onPhaseFilterSelected(phase: PhotoPhase?) {
        _uiState.update { it.copy(selectedPhase = phase) }
        observePhotos()
    }

    private fun observePhotos() {
        viewModelScope.launch {
            observePhotosUseCase(
                projectId = projectId,
                roomName = _uiState.value.selectedRoom,
                phase = _uiState.value.selectedPhase
            ).catch { throwable ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = throwable.message ?: "Kunde inte ladda bilder")
                }
            }.collect { photos ->
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
