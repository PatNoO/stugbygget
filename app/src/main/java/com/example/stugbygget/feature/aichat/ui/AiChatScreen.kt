package com.example.stugbygget.feature.aichat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AiChatScreen() {
    val viewModel: AiChatViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Stugan AI",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        Text(
            text = "Praktiska råd för renovering, material och planering.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.messages, key = { it.id }) { message ->
                ChatBubble(message = message)
            }
            if (uiState.isSending) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.width(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Stugan AI skriver...")
                    }
                }
            }
        }

        uiState.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.draftMessage,
                onValueChange = viewModel::onDraftChanged,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Skriv din fråga...") },
                enabled = !uiState.isSending,
                maxLines = 4
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = viewModel::onSendClicked,
                enabled = !uiState.isSending && uiState.draftMessage.isNotBlank()
            ) {
                Text("Skicka")
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessageUiModel) {
    val isUser = message.role == ChatRole.USER
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val background = if (isUser) Color(0xFFE8F1FF) else Color(0xFFF8F2E6)

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(background)
                    .padding(12.dp)
            ) {
                Text(
                    text = if (isUser) "Du" else "Stugan AI",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isUser) AnnotatedString(message.text) else markdownToAnnotatedString(message.text),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
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
