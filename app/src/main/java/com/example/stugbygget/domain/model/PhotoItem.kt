package com.example.stugbygget.domain.model

import java.time.Instant

enum class PhotoPhase {
    BEFORE,
    DURING,
    AFTER
}

data class PhotoItem(
    val id: String,
    val roomName: String,
    val phase: PhotoPhase,
    val description: String,
    val storagePath: String,
    val downloadUrl: String,
    val takenAt: Instant,
    val uploadedBy: String
)
