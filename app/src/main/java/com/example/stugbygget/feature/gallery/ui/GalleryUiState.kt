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
    // Add-photo sheet state
    val showAddSheet: Boolean = false,
    val draftRoomName: String = "",
    val draftPhase: PhotoPhase = PhotoPhase.DURING,
    val draftPhotos: List<Pair<Uri, String>> = emptyList(),
    val isUploading: Boolean = false,
    val uploadError: String? = null,
)
