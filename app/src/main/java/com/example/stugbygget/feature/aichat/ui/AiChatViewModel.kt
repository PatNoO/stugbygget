package com.example.stugbygget.feature.aichat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AiChatViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState(messages = defaultMessages()))
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    fun onDraftChanged(value: String) {
        _uiState.update { it.copy(draftMessage = value, errorMessage = null) }
    }

    fun onSendClicked() {
        val state = _uiState.value
        if (state.isSending) return

        val message = state.draftMessage.trim()
        if (message.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Skriv en fråga först.") }
            return
        }

        val userMessage = ChatMessageUiModel(
            id = UUID.randomUUID().toString(),
            role = ChatRole.USER,
            text = message
        )
        _uiState.update {
            it.copy(
                isSending = true,
                draftMessage = "",
                errorMessage = null,
                messages = it.messages + userMessage
            )
        }

        viewModelScope.launch {
            delay(700L)
            val reply = ChatMessageUiModel(
                id = UUID.randomUUID().toString(),
                role = ChatRole.ASSISTANT,
                text = buildMockAssistantReply(message)
            )
            _uiState.update {
                it.copy(
                    isSending = false,
                    messages = it.messages + reply
                )
            }
        }
    }

    private fun buildMockAssistantReply(question: String): String {
        return """
            **Snabbt råd för din fråga**
            - Fråga: $question
            - Nästa steg: verifiera mått i rummet innan materialköp.
            - Rekommendation: håll 10% svinnmarginal på kritiska material.
        """.trimIndent()
    }

    private fun defaultMessages(): List<ChatMessageUiModel> {
        return listOf(
            ChatMessageUiModel(
                id = "welcome",
                role = ChatRole.ASSISTANT,
                text = """
                    **Hej! Jag är Stugan AI**
                    - Fråga om material, regelverk eller planering.
                    - Jag svarar på svenska med konkreta nästa steg.
                """.trimIndent()
            )
        )
    }
}
