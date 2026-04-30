package com.example.moneybuddy2.data.repository

import com.example.moneybuddy2.data.remote.ChatRequestDto
import com.example.moneybuddy2.network.ApiClient

class ChatRepository(
    private val authRepository: AuthRepository
) {
    suspend fun sendChatMessage(message: String, systemPrompt: String = ""): String {
        val idToken = authRepository.getIdToken()

        val response = ApiClient.api.chat(
            authorization = "Bearer $idToken",
            request = ChatRequestDto(message = message, systemPrompt = systemPrompt)  // ← added
        )

        return response.answer
    }
}