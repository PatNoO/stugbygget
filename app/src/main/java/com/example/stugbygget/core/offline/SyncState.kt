package com.example.stugbygget.core.offline

data class SyncState(
    val isOnline: Boolean = true,
    val pendingWrites: Int = 0,
    val isSyncing: Boolean = false,
    val lastError: String? = null
)
