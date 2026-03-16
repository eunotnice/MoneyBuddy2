package com.example.moneybuddy2.backend.service

import com.example.moneybuddy2.backend.data.UserFinanceSnapshot

data class InsightFact(
    val id: String,
    val title: String,
    val message: String,
    val priority: Int
)

class InsightsFactsBuilder {

    fun fromSnapshot(snapshot: UserFinanceSnapshot): List<InsightFact> {
        val facts = mutableListOf<InsightFact>()

        facts += InsightFact(
            id = "total_spent",
            title = "Total spending",
            message = "User spent RM %.2f in the selected period.".format(snapshot.totalSpent),
            priority = 1
        )

        snapshot.totalIncome?.let { income ->
            val balance = income - snapshot.totalSpent

            facts += InsightFact(
                id = "income",
                title = "Income recorded",
                message = "Recorded income for the selected period is RM %.2f.".format(income),
                priority = 1
            )

            facts += InsightFact(
                id = "balance",
                title = "Estimated remaining balance",
                message = "Estimated remaining balance after spending is RM %.2f.".format(balance),
                priority = 1
            )

            if (balance < 0) {
                facts += InsightFact(
                    id = "overspending",
                    title = "Spending exceeds income",
                    message = "Spending is currently higher than recorded income.",
                    priority = 0
                )
            }
        } ?: run {
            facts += InsightFact(
                id = "income_missing",
                title = "Income data missing",
                message = "Income data is unavailable, so recommendations should be treated as estimates.",
                priority = 0
            )
        }

        snapshot.topSpendCategories.firstOrNull()?.let { top ->
            facts += InsightFact(
                id = "top_category",
                title = "Highest spending category",
                message = "${top.first} is the highest spending category at RM %.2f.".format(top.second),
                priority = 1
            )
        }

        snapshot.totalCo2eKg?.let { co2e ->
            facts += InsightFact(
                id = "carbon_total",
                title = "Carbon footprint",
                message = "Estimated carbon footprint for the selected period is %.2f kg CO2e.".format(co2e),
                priority = 2
            )
        }

        return facts.sortedBy { it.priority }
    }
}