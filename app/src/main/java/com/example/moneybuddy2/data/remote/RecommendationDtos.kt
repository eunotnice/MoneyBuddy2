package com.example.moneybuddy2.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class AiRecommendationRequestDto(
    val lifestyleNote: String? = null,
    val priorities: List<String> = emptyList(),
    val riskPreference: String? = null,
    val savingGoalNote: String? = null
)

@Serializable
data class AiBudgetPlanDto(
    val ruleLabel: String,
    val incomeUsed: Double? = null,
    val incomeConfidence: String,
    val needsTarget: Double? = null,
    val wantsTarget: Double? = null,
    val savingsTarget: Double? = null,
    val rationale: String = ""
)

@Serializable
data class AiSavingRecommendationDto(
    val title: String,
    val message: String,
    val priority: Int,
    val category: String
)

@Serializable
data class AiRecommendationResponseDto(
    val budgetPlan: AiBudgetPlanDto,
    val summary: String,
    val recommendations: List<AiSavingRecommendationDto>
)

//@Serializable
//data class RecommendationRequestDto(
//    val lifestyleNote: String? = null,
//    val priorities: List<String> = emptyList(),
//    val riskPreference: String? = null,
//    val savingGoalNote: String? = null
//)
//
//@Serializable
//data class BudgetPlanRecommendationDto(
//    val incomeUsed: Double? = null,
//    val incomeConfidence: String,
//    val ruleLabel: String,
//    val needsTarget: Double? = null,
//    val wantsTarget: Double? = null,
//    val savingsTarget: Double? = null,
//    val notes: List<String> = emptyList()
//)
//
//@Serializable
//data class SmartRecommendationDto(
//    val id: String,
//    val title: String,
//    val message: String,
//    val priority: Int,
//    val category: String,
//    val actionType: String? = null
//)
//
//@Serializable
//data class RecommendationResponseDto(
//    val budgetPlan: BudgetPlanRecommendationDto,
//    val recommendations: List<SmartRecommendationDto>,
//    val summary: String
//)