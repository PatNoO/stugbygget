package com.example.stugbygget.feature.aichat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stugbygget.R
import com.example.stugbygget.di.AppContainer
import com.example.stugbygget.ui.components.SommarButton
import com.example.stugbygget.ui.components.SommarHeaderCard
import com.example.stugbygget.ui.components.SommarInfoBox
import com.example.stugbygget.ui.theme.Border
import com.example.stugbygget.ui.theme.FaluRed
import com.example.stugbygget.ui.theme.MidsummerGold
import com.example.stugbygget.ui.theme.MonoStyles
import com.example.stugbygget.ui.theme.SommarGradients
import com.example.stugbygget.ui.theme.SommarShapes
import com.example.stugbygget.ui.theme.StugbyggetShapes
import kotlinx.coroutines.launch

@Composable
fun AiChatScreen(container: AppContainer) {
    val viewModel: AiChatViewModel = viewModel(factory = AiChatViewModelFactory(container))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll to bottom when a new message arrives or thinking indicator appears
    LaunchedEffect(uiState.messages.size, uiState.isSending) {
        val targetIndex = uiState.messages.size + (if (uiState.isSending) 1 else 0)
        if (targetIndex > 0) {
            scope.launch { listState.animateScrollToItem(targetIndex) }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Header ──
        SommarHeaderCard(
            gradient = SommarGradients.midsummerGold,
            title = stringResource(R.string.chat_header_title),
            subtitle = stringResource(R.string.chat_header_subtitle),
            emoji = "🤖",
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
        )

        // ── Message list ──
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Empty state
            if (uiState.messages.isEmpty() && !uiState.isSending) {
                item {
                    SommarInfoBox(
                        emoji = "🤖",
                        title = stringResource(R.string.chat_empty_title),
                        text = stringResource(R.string.chat_empty_message),
                        accentColor = MidsummerGold,
                    )
                }
            }

            items(uiState.messages, key = { it.id }) { message ->
                ChatBubble(message = message)
            }

            // Thinking indicator
            if (uiState.isSending) {
                item { ThinkingBubble() }
            }
        }

        // ── Inline error ──
        uiState.errorMessage?.let { error ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 4.dp)
                    .clip(StugbyggetShapes.small)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.08f))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("⚠️ ", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.error,
                    ),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // ── Input bar ──
        InputBar(
            draft = uiState.draftMessage,
            onDraftChanged = viewModel::onDraftChanged,
            onSend = viewModel::onSendClicked,
            isSending = uiState.isSending,
        )
    }
}

@Composable
private fun ChatBubble(message: ChatMessageUiModel) {
    val isUser = message.role == ChatRole.USER

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        if (isUser) {
            // User bubble: FaluRed gradient, white text, right-aligned shape
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .clip(SommarShapes.chatBubbleUser)
                    .background(SommarGradients.faluRed)
                    .padding(12.dp),
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.chat_label_you),
                        style = MonoStyles.dataSmall.copy(color = Color.White.copy(alpha = 0.7f)),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    )
                }
            }
        } else {
            // AI bubble: surface background, border, left-aligned shape
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(SommarShapes.chatBubbleAi)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, Border, SommarShapes.chatBubbleAi)
                    .padding(12.dp),
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.chat_label_ai),
                        style = MonoStyles.dataSmall.copy(color = MidsummerGold),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = markdownToAnnotatedString(message.text),
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    )
                }
            }
        }
    }
}

@Composable
private fun ThinkingBubble() {
    Row(
        modifier = Modifier
            .clip(SommarShapes.chatBubbleAi)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Border, SommarShapes.chatBubbleAi)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = MidsummerGold,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.chat_thinking),
            style = MonoStyles.dataSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        )
    }
}

@Composable
private fun InputBar(
    draft: String,
    onDraftChanged: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean,
) {
    val canSend = draft.isNotBlank() && !isSending

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 1.dp, color = Border)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        // Text input
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(StugbyggetShapes.small)
                .background(MaterialTheme.colorScheme.background)
                .border(1.dp, Border, StugbyggetShapes.small)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            if (draft.isEmpty()) {
                Text(
                    text = stringResource(R.string.chat_input_placeholder),
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                )
            }
            BasicTextField(
                value = draft,
                onValueChange = onDraftChanged,
                enabled = !isSending,
                maxLines = 4,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(FaluRed),
            )
        }

        Spacer(Modifier.width(10.dp))

        // Send button — SommarButton shows grey gradient automatically when disabled
        SommarButton(
            text = stringResource(R.string.chat_button_send),
            onClick = onSend,
            enabled = canSend,
        )
    }
}

private fun markdownToAnnotatedString(value: String): AnnotatedString {
    val boldRegex = "\\*\\*(.+?)\\*\\*".toRegex()
    return buildAnnotatedString {
        val lines = value.lines()
        lines.forEachIndexed { index, line ->
            val normalizedLine = if (line.trimStart().startsWith("- ")) {
                "• ${line.trimStart().removePrefix("- ").trim()}"
            } else {
                line
            }
            var currentIndex = 0
            boldRegex.findAll(normalizedLine).forEach { match ->
                append(normalizedLine.substring(currentIndex, match.range.first))
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                append(match.groupValues[1])
                pop()
                currentIndex = match.range.last + 1
            }
            if (currentIndex < normalizedLine.length) {
                append(normalizedLine.substring(currentIndex))
            }
            if (index != lines.lastIndex) append("\n")
        }
    }
}
