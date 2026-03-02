package com.example.stugbygget.feature.aichat.ui

data class AiChatUiState(
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val messages: List<ChatMessageUiModel> = emptyList(),
    val draftMessage: String = "",
    val errorMessage: String? = null
)

data class ChatMessageUiModel(
    val id: String,
    val role: ChatRole,
    val text: String
)

enum class ChatRole {
    USER,
    ASSISTANT
}
