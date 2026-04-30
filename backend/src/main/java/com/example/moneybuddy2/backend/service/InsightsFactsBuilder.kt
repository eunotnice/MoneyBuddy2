package com.example.moneybuddy2.backend.service

import com.example.moneybuddy2.backend.data.UserFinanceSnapshot

data class InsightFact(
    val id: String,
    val title: String,
    val message: String,
    val priority: Int
)
// Priority definition (document this for viva):
// 0 = critical (warnings)
// 1 = important (financial status)
// 2 = supporting (context / education)

class InsightsFactsBuilder {

    fun fromSnapshot(snapshot: UserFinanceSnapshot): List<InsightFact> {
        val facts = mutableListOf<InsightFact>()

        // Total spending
        facts += InsightFact(
            id = "total_spent",
            title = "Total spending",
            message = "User spent RM %.2f in the selected period.".format(snapshot.totalSpent),
            priority = 1
        )

        // Income-related insights
        snapshot.totalIncome?.let { income ->

            val balance = income - snapshot.totalSpent

            facts += InsightFact(
                id = "income",
                title = "Income recorded",
                message = "Recorded income is RM %.2f.".format(income),
                priority = 1
            )

            facts += InsightFact(
                id = "balance",
                title = "Remaining balance",
                message = "Estimated remaining balance is RM %.2f.".format(balance),
                priority = 1
            )

            // Spending ratio
            if (income > 0) {
                val ratio = snapshot.totalSpent / income

                facts += InsightFact(
                    id = "spending_ratio",
                    title = "Spending ratio",
                    message = "User has spent %.0f%% of recorded income.".format(ratio * 100),
                    priority = 1
                )

                // Overspending warning
                if (balance < 0) {
                    facts += InsightFact(
                        id = "overspending",
                        title = "Overspending detected",
                        message = "Spending exceeds income.",
                        priority = 0
                    )
                }

                // High pressure warning
                if (ratio >= 0.8 && balance >= 0) {
                    facts += InsightFact(
                        id = "high_spending_pressure",
                        title = "High spending pressure",
                        message = "Spending is consuming a large portion of income.",
                        priority = 0
                    )
                }
            }

        } ?: run {
            facts += InsightFact(
                id = "income_missing",
                title = "Income unavailable",
                message = "Income data is missing; analysis is approximate.",
                priority = 0
            )
        }

        // Top category
        snapshot.topSpendCategories.firstOrNull()?.let { top ->
            facts += InsightFact(
                id = "top_category",
                title = "Top spending category",
                message = "${top.first} is the highest spending category at RM %.2f.".format(top.second),
                priority = 1
            )
        }

        // Carbon insight
        snapshot.totalCo2eKg?.let { co2e ->
            facts += InsightFact(
                id = "carbon_total",
                title = "Carbon footprint",
                message = "Estimated emissions: %.2f kg CO2e.".format(co2e),
                priority = 2
            )
        }

        // Income confidence
        facts += InsightFact(
            id = "income_confidence",
            title = "Income confidence",
            message = "Income confidence level is ${snapshot.incomeConfidence}.",
            priority = 2
        )

        return facts.sortedBy { it.priority }
    }
}

class RagFactsProvider {

    fun getFacts(): List<InsightFact> = listOf(

        InsightFact(
            id = "budget_rule",
            title = "50/30/20 rule",
            message = "A common guideline allocates about 50% needs, 30% wants, and 20% savings.",
            priority = 2
        ),

        InsightFact(
            id = "budget_flexible",
            title = "Flexible budgeting",
            message = "Budget ratios may vary depending on income, cost of living, and commitments.",
            priority = 2
        ),

        InsightFact(
            id = "emergency_fund",
            title = "Emergency fund",
            message = "Emergency funds typically target 3 to 6 months of essential expenses.",
            priority = 2
        ),

        InsightFact(
            id = "fd_characteristics",
            title = "Fixed deposits",
            message = "Fixed deposits are low-risk with predictable returns but limited liquidity.",
            priority = 2
        ),

        InsightFact(
            id = "carbon_limitations",
            title = "Carbon estimates",
            message = "Carbon values are approximate indicators for awareness, not exact measurements.",
            priority = 2
        )
    )
}