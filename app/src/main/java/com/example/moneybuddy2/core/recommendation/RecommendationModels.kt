package com.example.moneybuddy2.core.recommendation

enum class RecommendationType {
    REDUCE_CATEGORY,
    INCREASE_SAVING,
}

data class Recommendation(
    val type: RecommendationType,
    val category: String?,
    val monthlyImpact: Double, //rm saved per month
     val priority: Int, //1=highest
    val rationale: String //system generated reason
)