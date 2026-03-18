package com.example.stugbygget.feature.gallery.ui

import android.net.Uri
import com.example.stugbygget.domain.model.PhotoItem
import com.example.stugbygget.domain.model.PhotoPhase

data class GalleryUiState(
    val isLoading: Boolean = true,
    val photos: List<PhotoItem> = emptyList(),
    val selectedRoom: String? = null,
    val selectedPhase: PhotoPhase? = null,
    val availableRooms: List<String> = emptyList(),
    val errorMessage: String? = null,
    // Camera capture state
    val showCameraCapture: Boolean = false,
    val capturedUri: Uri? = null,
    // Upload sheet state (shown after capture)
    val showUploadSheet: Boolean = false,
    val draftRoomName: String = "",
    val draftPhase: PhotoPhase = PhotoPhase.DURING,
    val isUploading: Boolean = false,
    val uploadError: String? = null,
)
