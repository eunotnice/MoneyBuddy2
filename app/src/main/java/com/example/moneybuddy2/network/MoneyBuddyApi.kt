package com.example.moneybuddy2.network

import com.example.moneybuddy2.data.remote.ChatRequestDto
import com.example.moneybuddy2.data.remote.ChatResponseDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Header

data class ChatRequest(
    val message: String
)

data class ChatResponse(
    val answer: String
)

interface MoneyBuddyApi {

    @POST("chat")
    suspend fun chat(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequestDto
    ): ChatResponseDto
}