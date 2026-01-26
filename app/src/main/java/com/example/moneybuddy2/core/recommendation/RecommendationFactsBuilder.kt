package com.example.moneybuddy2.core.recommendation

object RecommendationFactsBuilder {
    fun build(
        recommendations: List<Recommendation>,
        period: String
    ): String {
        return buildString {
            appendLine("PERIOD: $period")
            appendLine("RECOMMENDATIONS: ")
            recommendations.forEachIndexed { idx, r ->
                appendLine("${idx + 1}. TYPE=${r.type}")
                appendLine("   CATEGORY=${r.category ?: "N/A"}")
                appendLine("   MONTHLY_IMPACT=${"%.2f".format(r.monthlyImpact)}")
                appendLine("   RATIONALE=${r.rationale}")
            }
        }
    }
}