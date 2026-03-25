package com.example.moneybuddy2.backend.data

import kotlinx.serialization.Serializable

@Serializable
data class AiRecommendationResponse(
    val budgetPlan: AiBudgetPlan,
    val summary: String,
    val recommendations: List<AiSavingRecommendation>
)