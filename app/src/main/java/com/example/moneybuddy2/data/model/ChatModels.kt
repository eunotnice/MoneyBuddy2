package com.example.moneybuddy2.data.model

data class ChatMessage(
    val role: Role,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class Role { USER, ASSISTANT }

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val quickReplies: List<String> = emptyList(),
    val sending: Boolean = false,
    val error: String? = null
)
