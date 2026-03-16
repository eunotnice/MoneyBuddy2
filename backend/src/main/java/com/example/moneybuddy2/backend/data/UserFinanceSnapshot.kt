package com.example.moneybuddy2.backend.data

data class UserFinanceSnapshot(
    val userId: String,
    val currency: String = "MYR",
    val nowMillis: Long,
    val periodStartMillis: Long,
    val periodEndMillis: Long,
    val periodLabel: String,
    val totalSpent: Double,
    val totalIncome: Double?,
    val incomeConfidence: String,
    val spendByCategory: Map<String, Double>,
    val topSpendCategories: List<Pair<String, Double>>,
    val needsSpent: Double,
    val wantsSpent: Double,
    val uncategorisedSpent: Double,
    val totalCo2eKg: Double?,
    val co2eByCategory: Map<String, Double> = emptyMap(),
    val treesEquivalent: Double? = null,
    val treesFactorLabel: String? = null
)