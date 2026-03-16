package com.example.moneybuddy2.backend.data

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val message: String
)