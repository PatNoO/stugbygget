package com.example.stugbygget.domain.repository

import com.example.stugbygget.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun streamAssistantReply(
        projectId: String,
        conversation: List<ChatMessage>
    ): Flow<String>
}
