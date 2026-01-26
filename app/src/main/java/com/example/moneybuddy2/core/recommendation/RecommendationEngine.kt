package com.example.moneybuddy2.core.recommendation

object RecommendationEngine {
    fun generate(
        monthlyIncome: Double?,
        expenseByCategory: Map<String, Double>,
        goalGap: Double? //positive=shortfall
    ): List<Recommendation> {
        val recs = mutableListOf<Recommendation>()

        val totalSpend = expenseByCategory.values.sum()
        if(totalSpend<=0) return emptyList()

        //rule 1: dominant category
        expenseByCategory
            .toList()
            .sortedByDescending { it.second }
            .take(1)
            .forEach { (category, amount) ->
                val reduction = amount * 0.15 // 15% cut
                if (reduction >= 50) {
                    recs += Recommendation(
                        type = RecommendationType.REDUCE_CATEGORY,
                        category = category,
                        monthlyImpact = reduction,
                        priority = 1,
                        rationale = "$category accounts for a large share of spending."
                    )
                }
            }

        //rule2: goal shortfall
        if (goalGap != null && goalGap > 0) {
            recs += Recommendation(
                type = RecommendationType.INCREASE_SAVING,
                category = null,
                monthlyImpact = goalGap,
                priority = 0,
                rationale = "Current saving rate is below goal requirement."
            )
        }

        return recs.sortedBy { it.priority }

    }
}

//add more rules later:
//
//overspend frequency
//
//volatility
//
//discretionary ratio
//
//subscriptions