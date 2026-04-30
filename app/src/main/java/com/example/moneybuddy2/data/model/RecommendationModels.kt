package com.example.moneybuddy2.data.model

data class BudgetPlan(
    val income: Double,
    val needsLimit: Double,
    val savingsLimit: Double,
    val wantsLimit: Double,
    val ruleLabel: String = "50/30/20"
)

data class SpendSummary(
    val periodLabel: String,
    val totalSpent: Double,
    val byCategory: Map<String, Double>,
    val topCategories: List<Pair<String, Double>>
)

data class RecommendationCard(
    val id: String,
    val title: String,
    val message: String,
    val priority: Int,
    val sources: List<SourceRef>
)

data class SourceRef(
    val label: String,
    val url: String
)