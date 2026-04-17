package com.example.stugbygget.feature.gallery.ui

import com.example.stugbygget.domain.model.PhotoItem
import com.example.stugbygget.domain.model.PhotoPhase

data class GalleryUiState(
    val isLoading: Boolean = true,
    val photos: List<PhotoItem> = emptyList(),
    val availableRooms: List<String> = emptyList(),
    val selectedRoom: String? = null,
    val selectedPhase: PhotoPhase? = null,
    val errorMessage: String? = null,
    // Camera capture state
    val showCameraCapture: Boolean = false,
    val capturedUri: String? = null,
    val showUploadSheet: Boolean = false,
    // Gallery picker sheet state
    val showAddSheet: Boolean = false,
    // Shared draft state (used by both upload sheet and add sheet)
    val draftRoomName: String = "",
    val draftPhase: PhotoPhase = PhotoPhase.DURING,
    val draftPhotos: List<Pair<String, String>> = emptyList(),
    val isUploading: Boolean = false,
    val uploadError: String? = null,
    // Viewer / delete state
    val viewingPhoto: PhotoItem? = null,
    val pendingDeleteId: String? = null,
    val isDeleting: Boolean = false,
)
