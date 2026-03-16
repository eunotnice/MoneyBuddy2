package com.example.moneybuddy2.data.model

data class BudgetBucketSummary(
    val needsTotal: Double,
    val wantsTotal: Double,
    val uncategorisedTotal: Double,
    val byBucket: Map<String, Double>
)