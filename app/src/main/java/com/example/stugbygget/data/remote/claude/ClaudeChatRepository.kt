package com.example.stugbygget.data.remote.claude

import com.example.stugbygget.data.firebase.firestore.FirestoreProjectContextProvider
import com.example.stugbygget.domain.model.ChatMessage
import com.example.stugbygget.domain.model.ChatRole
import com.example.stugbygget.domain.repository.ChatRepository
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class ClaudeChatRepository(
    private val functions: FirebaseFunctions,
    private val contextProvider: FirestoreProjectContextProvider
) : ChatRepository {

    override fun streamAssistantReply(
        projectId: String,
        conversation: List<ChatMessage>
    ): Flow<String> = flow {
        val context = contextProvider.load(projectId)
        val payload = mapOf(
            "projectId" to projectId,
            "system" to ClaudePromptBuilder.buildSystemPrompt(context),
            "messages" to conversation.map { message ->
                mapOf(
                    "role" to if (message.role == ChatRole.USER) "user" else "assistant",
                    "content" to message.text
                )
            }
        )
        val response = functions
            .getHttpsCallable("generateAssistantReply")
            .call(payload)
            .await()

        val rawText = (response.data as? Map<*, *>)
            ?.get("text")
            ?.toString()
        val fullText = if (rawText.isNullOrBlank()) {
            "Jag kunde inte generera ett svar just nu."
        } else {
            rawText
        }

        // Simulate streaming chunks until server-side streaming is introduced.
        val words = fullText.split(" ")
        val partial = StringBuilder()
        words.forEachIndexed { index, word ->
            if (index > 0) partial.append(" ")
            partial.append(word)
            emit(partial.toString())
        }
    }
}
