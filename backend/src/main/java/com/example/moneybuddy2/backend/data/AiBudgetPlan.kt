package com.example.moneybuddy2.backend.data

import kotlinx.serialization.Serializable

@Serializable
data class AiBudgetPlan(
    val ruleLabel: String,
    val incomeUsed: Double? = null,
    val incomeConfidence: String,
    val needsTarget: Double? = null,
    val wantsTarget: Double? = null,
    val savingsTarget: Double? = null,
    val rationale: String = ""
)