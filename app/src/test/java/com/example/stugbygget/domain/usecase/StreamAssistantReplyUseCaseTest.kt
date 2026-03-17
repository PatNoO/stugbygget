package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.ChatMessage
import com.example.stugbygget.domain.model.ChatRole
import com.example.stugbygget.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class StreamAssistantReplyUseCaseTest {

    @Test
    fun `delegates to repository and returns its flow`() = runBlocking {
        val repo = FakeChatRepository(chunks = listOf("Hello", "Hello world"))
        val useCase = StreamAssistantReplyUseCase(repo)
        val conversation = listOf(ChatMessage(ChatRole.USER, "Test question"))

        val first = useCase("project-1", conversation).first()

        assertEquals("Hello", first)
    }

    @Test
    fun `passes projectId to repository`() = runBlocking {
        val repo = FakeChatRepository()
        val useCase = StreamAssistantReplyUseCase(repo)

        useCase("my-project", emptyList()).first()

        assertEquals("my-project", repo.lastProjectId)
    }

    @Test
    fun `passes conversation to repository`() = runBlocking {
        val repo = FakeChatRepository()
        val useCase = StreamAssistantReplyUseCase(repo)
        val conversation = listOf(
            ChatMessage(ChatRole.USER, "Fråga 1"),
            ChatMessage(ChatRole.ASSISTANT, "Svar 1")
        )

        useCase("project-1", conversation).first()

        assertEquals(conversation, repo.lastConversation)
    }

    // ── Fake ──────────────────────────────────────────────────────────────────

    private class FakeChatRepository(
        private val chunks: List<String> = listOf("chunk")
    ) : ChatRepository {
        var lastProjectId: String? = null
        var lastConversation: List<ChatMessage>? = null

        override fun streamAssistantReply(
            projectId: String,
            conversation: List<ChatMessage>
        ): Flow<String> {
            lastProjectId = projectId
            lastConversation = conversation
            return flowOf(*chunks.toTypedArray())
        }
    }
}
