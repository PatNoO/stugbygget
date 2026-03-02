package com.example.stugbygget.data.remote.claude

import com.example.stugbygget.BuildConfig
import com.example.stugbygget.data.firebase.firestore.FirestoreProjectContextProvider
import com.example.stugbygget.domain.model.ChatMessage
import com.example.stugbygget.domain.model.ChatRole
import com.example.stugbygget.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ClaudeChatRepository(
    private val apiService: ClaudeApiService,
    private val contextProvider: FirestoreProjectContextProvider
) : ChatRepository {

    override fun streamAssistantReply(
        projectId: String,
        conversation: List<ChatMessage>
    ): Flow<String> = flow {
        val apiKey = BuildConfig.CLAUDE_API_KEY
        if (apiKey.isBlank()) {
            throw IllegalStateException("CLAUDE_API_KEY saknas i local.properties")
        }

        val context = contextProvider.load(projectId)
        val request = ClaudeMessageRequest(
            model = "claude-3-5-sonnet-latest",
            maxTokens = 1024,
            system = ClaudePromptBuilder.buildSystemPrompt(context),
            messages = conversation.map { message ->
                ClaudeInputMessage(
                    role = if (message.role == ChatRole.USER) "user" else "assistant",
                    content = message.text
                )
            }
        )
        val response = apiService.createMessage(apiKey = apiKey, request = request)
        val fullText = response.content
            .filter { it.type == "text" }
            .joinToString(separator = "") { it.text }
            .ifBlank { "Jag kunde inte generera ett svar just nu." }

        // Simulerar tokenvis uppdatering tills SSE läggs till i nästa iteration.
        val words = fullText.split(" ")
        val partial = StringBuilder()
        words.forEachIndexed { index, word ->
            if (index > 0) partial.append(" ")
            partial.append(word)
            emit(partial.toString())
        }
    }
}
