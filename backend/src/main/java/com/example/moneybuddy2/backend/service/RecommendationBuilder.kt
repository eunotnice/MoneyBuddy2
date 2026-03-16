package com.example.moneybuddy2.backend.service

import com.example.moneybuddy2.backend.data.RecommendationCard
import com.example.moneybuddy2.backend.data.SourceRef
import com.example.moneybuddy2.backend.data.UserFinanceSnapshot

class RecommendationBuilder {
    fun build(snapshot: UserFinanceSnapshot): List<RecommendationCard> {
        val cards = mutableListOf<RecommendationCard>()

        if (snapshot.uncategorisedSpent > snapshot.totalSpent * 0.15) {
            cards += RecommendationCard(
                id = "recategorise",
                title = "Improve category accuracy",
                message = "A relatively large share of your spending is uncategorised. Reclassifying those transactions will improve budget accuracy.",
                priority = 1,
                sources = emptyList()
            )
        }

        if (snapshot.totalIncome != null) {
            val budgetNeeds = snapshot.totalIncome * 0.50
            val budgetWants = snapshot.totalIncome * 0.30

            if (snapshot.wantsSpent > budgetWants) {
                cards += RecommendationCard(
                    id = "reduce-wants",
                    title = "Reduce discretionary spending",
                    message = "Your wants spending appears higher than a 50/30/20 benchmark for this period.",
                    priority = 1,
                    sources = listOf(
                        SourceRef("50/30/20 budgeting rule", "https://www.investopedia.com/ask/answers/022916/what-502030-budget-rule.asp")
                    )
                )
            }

            if (snapshot.needsSpent > budgetNeeds) {
                cards += RecommendationCard(
                    id = "review-needs",
                    title = "Review high essential spending",
                    message = "Your essential spending is relatively high. It may help to review transport, food, utilities, or service costs.",
                    priority = 2,
                    sources = listOf(
                        SourceRef("50/30/20 budgeting rule", "https://www.investopedia.com/ask/answers/022916/what-502030-budget-rule.asp")
                    )
                )
            }
        }

        val topCarbon = snapshot.co2eByCategory.maxByOrNull { it.value }
        if (topCarbon != null && topCarbon.value > 0.0) {
            cards += RecommendationCard(
                id = "carbon-hotspot",
                title = "Check your highest carbon category",
                message = "${topCarbon.key} currently contributes the most to your recorded carbon footprint.",
                priority = 3,
                sources = listOf(
                    SourceRef("EPA equivalencies", "https://www.epa.gov/energy/greenhouse-gas-equivalencies-calculator")
                )
            )
        }

        return cards.sortedBy { it.priority }
    }
}