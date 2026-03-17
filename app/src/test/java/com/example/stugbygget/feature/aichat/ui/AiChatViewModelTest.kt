package com.example.stugbygget.feature.aichat.ui

import com.example.stugbygget.domain.model.ChatMessage
import com.example.stugbygget.domain.repository.ChatRepository
import com.example.stugbygget.domain.usecase.StreamAssistantReplyUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AiChatViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial state has welcome message`() = runTest {
        val vm = buildViewModel()

        assertTrue(vm.uiState.value.messages.isNotEmpty())
        assertEquals(ChatRole.ASSISTANT, vm.uiState.value.messages.first().role)
    }

    @Test
    fun `initial state has empty draft and no error`() = runTest {
        val vm = buildViewModel()

        assertEquals("", vm.uiState.value.draftMessage)
        assertNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isSending)
    }

    // ── Draft ─────────────────────────────────────────────────────────────────

    @Test
    fun `onDraftChanged updates draftMessage`() = runTest {
        val vm = buildViewModel()

        vm.onDraftChanged("Hur lägger jag tegel?")

        assertEquals("Hur lägger jag tegel?", vm.uiState.value.draftMessage)
    }

    @Test
    fun `onDraftChanged clears errorMessage`() = runTest {
        val vm = buildViewModel()
        vm.onSendClicked() // triggers blank error

        vm.onDraftChanged("Något")

        assertNull(vm.uiState.value.errorMessage)
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Test
    fun `send with blank draft sets errorMessage`() = runTest {
        val vm = buildViewModel()

        vm.onSendClicked()

        assertNotNull(vm.uiState.value.errorMessage)
        assertEquals("Skriv en fråga först.", vm.uiState.value.errorMessage)
    }

    @Test
    fun `send with whitespace-only draft sets errorMessage`() = runTest {
        val vm = buildViewModel()
        vm.onDraftChanged("   ")

        vm.onSendClicked()

        assertNotNull(vm.uiState.value.errorMessage)
    }

    // ── Successful send ───────────────────────────────────────────────────────

    @Test
    fun `valid send adds user message to list`() = runTest {
        val vm = buildViewModel(chunks = listOf("Svar"))
        vm.onDraftChanged("Vad kostar tegel?")

        vm.onSendClicked()

        val userMessages = vm.uiState.value.messages.filter { it.role == ChatRole.USER }
        assertEquals(1, userMessages.size)
        assertEquals("Vad kostar tegel?", userMessages.first().text)
    }

    @Test
    fun `valid send clears draft`() = runTest {
        val vm = buildViewModel(chunks = listOf("Svar"))
        vm.onDraftChanged("Vad kostar tegel?")

        vm.onSendClicked()

        assertEquals("", vm.uiState.value.draftMessage)
    }

    @Test
    fun `valid send adds assistant message with streamed text`() = runTest {
        val vm = buildViewModel(chunks = listOf("Del 1", "Del 1 Del 2"))
        vm.onDraftChanged("Fråga")

        vm.onSendClicked()

        val assistantMessages = vm.uiState.value.messages.filter { it.role == ChatRole.ASSISTANT }
        // Last assistant message should have the final chunk
        val lastAssistant = assistantMessages.last()
        assertEquals("Del 1 Del 2", lastAssistant.text)
    }

    @Test
    fun `isSending is false after stream completes`() = runTest {
        val vm = buildViewModel(chunks = listOf("Svar"))
        vm.onDraftChanged("Fråga")

        vm.onSendClicked()

        assertFalse(vm.uiState.value.isSending)
    }

    @Test
    fun `send trims whitespace from draft`() = runTest {
        val vm = buildViewModel(chunks = listOf("OK"))
        vm.onDraftChanged("  Fråga med mellanslag  ")

        vm.onSendClicked()

        val userMessages = vm.uiState.value.messages.filter { it.role == ChatRole.USER }
        assertEquals("Fråga med mellanslag", userMessages.first().text)
    }

    // ── Error handling ────────────────────────────────────────────────────────

    @Test
    fun `stream error sets errorMessage`() = runTest {
        val vm = buildViewModel(throwOnStream = true)
        vm.onDraftChanged("Fråga")

        vm.onSendClicked()

        assertNotNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `stream error sets isSending to false`() = runTest {
        val vm = buildViewModel(throwOnStream = true)
        vm.onDraftChanged("Fråga")

        vm.onSendClicked()

        assertFalse(vm.uiState.value.isSending)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildViewModel(
        chunks: List<String> = listOf("Svar"),
        throwOnStream: Boolean = false
    ): AiChatViewModel {
        val repo = FakeChatRepository(chunks = chunks, throwOnStream = throwOnStream)
        return AiChatViewModel(
            streamAssistantReplyUseCase = StreamAssistantReplyUseCase(repo),
            projectId = "project-1"
        )
    }

    private class FakeChatRepository(
        private val chunks: List<String> = listOf("chunk"),
        private val throwOnStream: Boolean = false
    ) : ChatRepository {
        override fun streamAssistantReply(
            projectId: String,
            conversation: List<ChatMessage>
        ): Flow<String> {
            if (throwOnStream) return flow { throw RuntimeException("Stream failed") }
            return flowOf(*chunks.toTypedArray())
        }
    }
}
