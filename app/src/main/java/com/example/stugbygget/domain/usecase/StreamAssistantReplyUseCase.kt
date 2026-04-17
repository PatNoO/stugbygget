package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.ChatMessage
import com.example.stugbygget.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

/**
 * Streams a Claude AI assistant reply for the given conversation history.
 *
 * Delegates to [ChatRepository] which calls a Firebase Cloud Function that proxies
 * the request to the Claude API. The reply is emitted token-by-token as a [Flow]
 * of partial strings, enabling real-time streaming in the chat UI.
 *
 * @param projectId The active renovation project ID (for conversation scoping).
 * @param conversation The full chat history including the latest user message.
 * @return A cold [Flow] that emits partial reply tokens until the response is complete.
 */
class StreamAssistantReplyUseCase(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(projectId: String, conversation: List<ChatMessage>): Flow<String> {
        return chatRepository.streamAssistantReply(projectId, conversation)
    }
}
