package com.example.moneybuddy2.network

import com.example.moneybuddy2.data.remote.AiRecommendationRequestDto
import com.example.moneybuddy2.data.remote.AiRecommendationResponseDto
import com.example.moneybuddy2.data.remote.ChatRequestDto
import com.example.moneybuddy2.data.remote.ChatResponseDto
//import com.example.moneybuddy2.data.remote.RecommendationRequestDto
//import com.example.moneybuddy2.data.remote.RecommendationResponseDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Header

interface MoneyBuddyApi {

    @POST("chat")
    suspend fun chat(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequestDto
    ): ChatResponseDto

    @POST("recommendations")
    suspend fun recommendations(
        @Header("Authorization") authorization: String,
        @Body request: AiRecommendationRequestDto
    ): AiRecommendationResponseDto
}