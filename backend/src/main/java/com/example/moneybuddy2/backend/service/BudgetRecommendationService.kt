package com.example.moneybuddy2.backend.service

import com.example.moneybuddy2.backend.data.BudgetPreferenceInput
import com.example.moneybuddy2.backend.data.BudgetRatio

class BudgetRecommendationService {

    fun chooseBudgetRatio(input: BudgetPreferenceInput?): BudgetRatio {
        val text = buildString {
            append(input?.lifestyleNote.orEmpty().lowercase())
            append(" ")
            append(input?.priorities?.joinToString(" ")?.lowercase().orEmpty())
            append(" ")
            append(input?.savingGoalNote.orEmpty().lowercase())
            append(" ")
            append(input?.riskPreference.orEmpty().lowercase())
        }
        return when {
            "save aggressively" in text ||
                    "save more" in text ||
                    "emergency fund" in text ||
                    "build savings" in text ||
                    "prioritise savings" in text -> {
                BudgetRatio(
                    needsRatio = 0.45,
                    wantsRatio = 0.20,
                    savingsRatio = 0.35,
                    label = "Adjusted for aggressive saving"
                )
            }

            "family" in text ||
                    "children" in text ||
                    "essentials" in text ||
                    "needs" in text ||
                    "transport" in text ||
                    "medical" in text ||
                    "health" in text -> {
                BudgetRatio(
                    needsRatio = 0.60,
                    wantsRatio = 0.20,
                    savingsRatio = 0.20,
                    label = "Adjusted for essential priorities"
                )
            }

            "balanced" in text ||
                    "normal" in text ||
                    "moderate" in text -> {
                BudgetRatio(
                    needsRatio = 0.50,
                    wantsRatio = 0.30,
                    savingsRatio = 0.20,
                    label = "50/30/20 baseline"
                )
            }

            else -> {
                BudgetRatio(
                    needsRatio = 0.50,
                    wantsRatio = 0.30,
                    savingsRatio = 0.20,
                    label = "50/30/20 baseline"
                )
            }
        }
    }

}