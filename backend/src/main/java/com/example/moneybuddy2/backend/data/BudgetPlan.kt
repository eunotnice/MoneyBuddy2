package com.example.moneybuddy2.backend.data

data class BudgetPlan(
    val income: Double,
    val needsLimit: Double,
    val savingsLimit: Double,
    val wantsLimit: Double,
    val ruleLabel: String = "50/30/20"
)