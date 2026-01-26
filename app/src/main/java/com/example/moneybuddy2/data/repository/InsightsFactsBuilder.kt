//converts firestore expenses into aggregrated facts
package com.example.moneybuddy2.data.repository

import org.json.JSONArray
import org.json.JSONObject

class InsightsFacts (
    val json: String
)

object InsightsFactsBuilder {

    fun buildFactsheet(
        periodDays: Int,
        totalSpend: Double,
        avgDailySpend: Double,
        topCategories: List<Pair<String, Double>>,
        goalSummary: String?
    ): InsightsFacts {
        val cats = JSONArray().apply {
            topCategories.forEach { (cat, amt) ->
                put(JSONObject().apply {
                    put("category", cat)
                    put("amount", amt)
                })
            }
        }

        val root = JSONObject().apply {
            put("periodDays", periodDays)
            put("totalSpend", totalSpend)
            put("avgDailySpend", avgDailySpend)
            put("topCategories", cats)
            put("goalSummary", goalSummary ?: JSONObject.NULL)
        }

        return InsightsFacts(json = root.toString(2))
    }
}