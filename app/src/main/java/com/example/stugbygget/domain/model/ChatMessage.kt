package com.example.stugbygget.domain.model

data class ChatMessage(
    val role: ChatRole,
    val text: String
)

enum class ChatRole {
    USER,
    ASSISTANT
}

data class ProjectChatContext(
    val projectName: String,
    val phaseNames: List<String>,
    val budgetSummary: String
)
