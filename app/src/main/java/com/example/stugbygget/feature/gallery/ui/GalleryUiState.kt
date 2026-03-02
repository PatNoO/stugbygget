package com.example.stugbygget.feature.gallery.ui

import com.example.stugbygget.domain.model.PhotoItem
import com.example.stugbygget.domain.model.PhotoPhase

data class GalleryUiState(
    val isLoading: Boolean = true,
    val photos: List<PhotoItem> = emptyList(),
    val selectedRoom: String? = null,
    val selectedPhase: PhotoPhase? = null,
    val availableRooms: List<String> = emptyList(),
    val errorMessage: String? = null
)
