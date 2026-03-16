package com.example.moneybuddy2.backend.data

import kotlinx.serialization.Serializable

@Serializable
data class ChatResponse(
    val answer: String,
    //val recommendations: List<RecommendationCard> = emptyList()
)