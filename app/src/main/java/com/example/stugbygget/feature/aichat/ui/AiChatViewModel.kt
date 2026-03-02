package com.example.stugbygget.feature.aichat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stugbygget.domain.model.ChatMessage
import com.example.stugbygget.domain.model.ChatRole as DomainChatRole
import com.example.stugbygget.domain.usecase.StreamAssistantReplyUseCase
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AiChatViewModel(
    private val streamAssistantReplyUseCase: StreamAssistantReplyUseCase
) : ViewModel() {

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
            val assistantMessageId = UUID.randomUUID().toString()
            _uiState.update {
                it.copy(
                    messages = it.messages + ChatMessageUiModel(
                        id = assistantMessageId,
                        role = ChatRole.ASSISTANT,
                        text = ""
                    )
                )
            }
            streamAssistantReplyUseCase(
                projectId = DEFAULT_PROJECT_ID,
                conversation = _uiState.value.messages
                    .filter { chat -> chat.text.isNotBlank() }
                    .map { chat ->
                        ChatMessage(
                            role = if (chat.role == ChatRole.USER) DomainChatRole.USER else DomainChatRole.ASSISTANT,
                            text = chat.text
                        )
                    }
            ).catch { throwable ->
                _uiState.update {
                    it.copy(
                        isSending = false,
                        errorMessage = throwable.message ?: "Kunde inte hämta AI-svar"
                    )
                }
            }.onCompletion {
                _uiState.update { it.copy(isSending = false) }
            }.collect { chunk ->
                _uiState.update { state ->
                    state.copy(
                        messages = state.messages.map { messageItem ->
                            if (messageItem.id == assistantMessageId) {
                                messageItem.copy(text = chunk)
                            } else {
                                messageItem
                            }
                        }
                    )
                }
            }
        }
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

    companion object {
        private const val DEFAULT_PROJECT_ID = "default-project"
    }
}
