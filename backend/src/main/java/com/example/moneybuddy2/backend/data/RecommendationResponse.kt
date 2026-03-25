package com.example.moneybuddy2.backend.data

import kotlinx.serialization.Serializable

@Serializable
data class RecommendationResponse(
    val budgetPlan: BudgetPlanRecommendation,
    val recommendations: List<SmartRecommendation>,
    val summary: String
)