package com.example.moneybuddy2.backend.service

import com.example.moneybuddy2.backend.data.BudgetPlanRecommendation
import com.example.moneybuddy2.backend.data.BudgetPreferenceInput
import com.example.moneybuddy2.backend.data.SmartRecommendation
import com.example.moneybuddy2.backend.data.UserFinanceSnapshot

class RecommendationEngine {

    fun buildSmartRecommendations(
        snapshot: UserFinanceSnapshot,
        budgetPlan: BudgetPlanRecommendation,
        preference: BudgetPreferenceInput?
    ): List<SmartRecommendation> {

        val recommendations = mutableListOf<SmartRecommendation>()

        if (snapshot.totalIncome == null) {
            recommendations += SmartRecommendation(
                id = "income_missing",
                title = "Add income for a more accurate budget",
                message = "Your current budget plan is based on incomplete income information. Recording your income will allow the app to generate more precise targets.",
                priority = 0,
                category = "budget",
                actionType = "add_income"
            )
        }

        snapshot.topSpendCategories.firstOrNull()?.let { top ->
            recommendations += SmartRecommendation(
                id = "top_spending_category",
                title = "Review your highest spending category",
                message = "${top.first} is currently your highest spending category at RM %.2f.".format(top.second),
                priority = 1,
                category = "spending",
                actionType = "review_category"
            )
        }

        if (snapshot.totalIncome != null) {
            val balance = snapshot.totalIncome - snapshot.totalSpent

            if (balance < 0) {
                recommendations += SmartRecommendation(
                    id = "overspending",
                    title = "Spending is higher than income",
                    message = "Your current recorded spending exceeds your recorded income for the selected period. Reducing discretionary spending should be prioritised.",
                    priority = 0,
                    category = "budget",
                    actionType = "reduce_spending"
                )
            } else if (balance < snapshot.totalIncome * 0.10) {
                recommendations += SmartRecommendation(
                    id = "low_savings_room",
                    title = "Strengthen your savings buffer",
                    message = "Your current spending leaves limited room for savings. Consider reviewing discretionary categories and prioritising savings.",
                    priority = 0,
                    category = "savings",
                    actionType = "increase_savings"
                )
            }
        }

        if (budgetPlan.savingsTarget != null && budgetPlan.savingsTarget > 0.0) {
            recommendations += SmartRecommendation(
                id = "emergency_fund",
                title = "Prioritise your emergency fund",
                message = "A practical first use of planned savings is to build or strengthen an emergency fund for unexpected expenses.",
                priority = 1,
                category = "emergency_fund",
                actionType = "build_emergency_fund"
            )
        }

        val preferenceText = buildString {
            append(preference?.lifestyleNote.orEmpty().lowercase())
            append(" ")
            append(preference?.priorities?.joinToString(" ")?.lowercase().orEmpty())
            append(" ")
            append(preference?.savingGoalNote.orEmpty().lowercase())
            append(" ")
            append(preference?.riskPreference.orEmpty().lowercase())
        }

        val lowRisk = "low" in preferenceText || "safe" in preferenceText || "conservative" in preferenceText
        val mentionsFd = "fixed deposit" in preferenceText || "fd" in preferenceText

        if ((lowRisk || mentionsFd) && budgetPlan.savingsTarget != null && budgetPlan.savingsTarget > 0.0) {
            recommendations += SmartRecommendation(
                id = "fixed_deposit_suitability",
                title = "Consider fixed deposit for stable savings",
                message = "If your priority is low-risk and more predictable savings storage, a fixed deposit may be suitable for part of your planned savings, while keeping enough money liquid for emergencies.",
                priority = 2,
                category = "fixed_deposit",
                actionType = "learn_fixed_deposit"
            )
        }

        snapshot.totalCo2eKg?.let { totalCo2e ->
            if (totalCo2e > 0.0) {
                recommendations += SmartRecommendation(
                    id = "carbon_awareness",
                    title = "Review your carbon-related spending patterns",
                    message = "Your recorded spending also contributes to an estimated carbon footprint. Reviewing high-impact categories may support both financial and sustainability goals.",
                    priority = 3,
                    category = "carbon",
                    actionType = "review_carbon"
                )
            }
        }

        return recommendations.sortedBy { it.priority }
    }

    fun buildSummary(
        snapshot: UserFinanceSnapshot,
        plan: BudgetPlanRecommendation,
        recommendations: List<SmartRecommendation>
    ): String {
        val topCategoryText = snapshot.topSpendCategories.firstOrNull()?.let {
            "${it.first} is currently the highest spending category."
        } ?: "No dominant spending category was identified."

        val planText = if (plan.incomeUsed != null) {
            "The suggested budget is based on ${plan.ruleLabel.lowercase()}, with needs at RM %.2f, wants at RM %.2f, and savings at RM %.2f."
                .format(
                    plan.needsTarget ?: 0.0,
                    plan.wantsTarget ?: 0.0,
                    plan.savingsTarget ?: 0.0
                )
        } else {
            "A percentage-based budget approach is suggested, but exact targets require income data."
        }

        val topRecommendation = recommendations.firstOrNull()?.message
            ?: "No specific recommendation is available yet."

        return "$planText $topCategoryText $topRecommendation"
    }
}