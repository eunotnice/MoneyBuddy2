package com.example.moneybuddy2.backend.data

data class BudgetRatio(
    val needsRatio: Double,
    val wantsRatio: Double,
    val savingsRatio: Double,
    val label: String
)