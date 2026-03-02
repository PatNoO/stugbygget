package com.example.stugbygget.domain.model

import java.time.Instant

enum class TodoPriority {
    HIGH,
    MEDIUM,
    LOW
}

data class TodoItem(
    val id: String,
    val text: String,
    val done: Boolean,
    val phaseId: String,
    val assignee: String,
    val priority: TodoPriority,
    val createdAt: Instant,
    val updatedAt: Instant
)
