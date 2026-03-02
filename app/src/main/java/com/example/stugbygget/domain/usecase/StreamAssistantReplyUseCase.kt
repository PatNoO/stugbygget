package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.ChatMessage
import com.example.stugbygget.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class StreamAssistantReplyUseCase(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(projectId: String, conversation: List<ChatMessage>): Flow<String> {
        return chatRepository.streamAssistantReply(projectId, conversation)
    }
}
