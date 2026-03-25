package com.example.moneybuddy2.backend.data

import kotlinx.serialization.Serializable

@Serializable
data class AiRecommendationRequest(
    val lifestyleNote: String? = null,
    val priorities: List<String> = emptyList(),
    val riskPreference: String? = null,
    val savingGoalNote: String? = null
)