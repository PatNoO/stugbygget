package com.example.stugbygget.data.remote.claude

import com.google.gson.annotations.SerializedName

data class ClaudeMessageRequest(
    val model: String,
    @SerializedName("max_tokens")
    val maxTokens: Int,
    val system: String,
    val messages: List<ClaudeInputMessage>
)

data class ClaudeInputMessage(
    val role: String,
    val content: String
)

data class ClaudeMessageResponse(
    val content: List<ClaudeContentBlock> = emptyList()
)

data class ClaudeContentBlock(
    val type: String = "",
    val text: String = ""
)
