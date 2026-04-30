package com.example.moneybuddy2.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequestDto(
    val message: String,
    val systemPrompt: String = ""
)

@Serializable
data class ChatResponseDto(
    val answer: String
)