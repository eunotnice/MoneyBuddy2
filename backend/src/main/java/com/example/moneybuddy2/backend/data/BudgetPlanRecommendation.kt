package com.example.moneybuddy2.backend.data

import kotlinx.serialization.Serializable

@Serializable
data class BudgetPlanRecommendation(
    val incomeUsed: Double?,
    val incomeConfidence: String,
    val ruleLabel: String,
    val needsTarget: Double?,
    val wantsTarget: Double?,
    val savingsTarget: Double?,
    val notes: List<String> = emptyList()
)