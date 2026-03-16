//converts firestore expenses into aggregrated facts
package com.example.moneybuddy2.data.repository

import com.example.moneybuddy2.data.model.UserFinanceSnapshot
import org.json.JSONArray
import org.json.JSONObject

class InsightsFactsBuilder {

    fun fromSnapshot(snapshot: UserFinanceSnapshot): List<String> {
        val facts = mutableListOf<String>()

        facts += "Total spent in this period: RM %.2f".format(snapshot.totalSpent)

        snapshot.totalIncome?.let { income ->
            facts += "Total income in this period: RM %.2f".format(income)

            val balance = income - snapshot.totalSpent
            facts += "Estimated balance after spending: RM %.2f".format(balance)

            if (balance < 0) {
                facts += "Spending is currently higher than income."
            } else if (balance < income * 0.20) {
                facts += "Savings room appears limited based on current income and spending."
            } else {
                facts += "There appears to be some room for savings based on current income and spending."
            }
        } ?: run {
            facts += "Income data is unavailable, so savings advice should be treated as an estimate."
        }

        val topCategory = snapshot.topSpendCategories.firstOrNull()
        if (topCategory != null) {
            facts += "Highest spending category is ${topCategory.first} at RM %.2f".format(topCategory.second)
        }

        if (snapshot.totalCo2eKg != null) {
            facts += "Estimated carbon footprint for this period is %.2f kg CO2e".format(snapshot.totalCo2eKg)
        }

        snapshot.treesEquivalent?.let { trees ->
            facts += "This is approximately equivalent to %.2f trees over the selected equivalency basis".format(trees)
        }

        return facts
    }
}