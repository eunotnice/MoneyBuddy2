package com.example.moneybuddy2.backend.service

import com.example.moneybuddy2.backend.data.BudgetPlanRecommendation
import com.example.moneybuddy2.backend.data.BudgetPreferenceInput
import com.example.moneybuddy2.backend.data.BudgetRatio
import com.example.moneybuddy2.backend.data.UserFinanceSnapshot

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

    fun buildPlan(
        snapshot: UserFinanceSnapshot,
        input: BudgetPreferenceInput?
    ): BudgetPlanRecommendation {
        val ratio = chooseBudgetRatio(input)
        val notes = mutableListOf<String>()

        val incomeUsed = snapshot.totalIncome
        val incomeConfidence = snapshot.incomeConfidence

        if (incomeUsed == null) {
            notes += "No income record is available, so exact budget amounts cannot be calculated."
            notes += "The budget guidance should be treated as an estimate until income is added."

            return BudgetPlanRecommendation(
                incomeUsed = null,
                incomeConfidence = incomeConfidence,
                ruleLabel = ratio.label,
                needsTarget = null,
                wantsTarget = null,
                savingsTarget = null,
                notes = notes
            )
        }

        val needsTarget = incomeUsed * ratio.needsRatio
        val wantsTarget = incomeUsed * ratio.wantsRatio
        val savingsTarget = incomeUsed * ratio.savingsRatio

        if (incomeConfidence == "estimated") {
            notes += "Income is estimated from recent records, so targets are approximate."
        }

        notes += "This plan uses ${ratio.label.lowercase()}."

        return BudgetPlanRecommendation(
            incomeUsed = incomeUsed,
            incomeConfidence = incomeConfidence,
            ruleLabel = ratio.label,
            needsTarget = needsTarget,
            wantsTarget = wantsTarget,
            savingsTarget = savingsTarget,
            notes = notes
        )
    }
}