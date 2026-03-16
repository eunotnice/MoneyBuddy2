package com.example.moneybuddy2.backend.data

import kotlinx.serialization.Serializable

@Serializable
data class BudgetPreferenceInput(
    val lifestyleNote: String? = null,
    val priorities: List<String> = emptyList(),
    val riskPreference: String? = null,   // "low", "moderate", "high"
    val savingGoalNote: String? = null
)