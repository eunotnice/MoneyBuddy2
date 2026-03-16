package com.example.moneybuddy2.data.model

data class UserFinanceSnapshot (
    val userId: String,
    val nowMillis: Long,
    val currency: String = "MYR",

    val periodStartMillis: Long,
    val periodEndMillis: Long,

    val totalSpent: Double,
    val totalIncome: Double?,
    val incomeCondifence: String,

    val spendByCategory: Map<String, Double>,
    val topSpendCategories: List<Pair<String, Double>>,

    val totalCo2eKg: Double?,
    val co2eByCategory: Map<String, Double> = emptyMap(),
    val treesEquivalent: Double? = null,
    val treesFactorLabel: String? = null,
)

enum class ActionId {
    SET_BUDGET_LIMITS,
    CREATE_SAVINGS_GOAL_PLAN,
    REDUCE_TOP_CATEGORY,
    SUBSCRIPTION_CHECK,
    EMERGENCY_FUND_PLAN,
    FIXED_DEPOSIT_INFO,
    CARBON_INSIGHT
}

