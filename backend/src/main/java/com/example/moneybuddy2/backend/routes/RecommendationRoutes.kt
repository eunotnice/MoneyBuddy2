//package com.example.moneybuddy2.backend.routes
//
//import com.example.moneybuddy2.backend.data.BudgetPreferenceInput
//import com.example.moneybuddy2.backend.data.RecommendationRequest
//import com.example.moneybuddy2.backend.data.RecommendationResponse
//import com.example.moneybuddy2.backend.service.AuthService
//import com.example.moneybuddy2.backend.service.BudgetRecommendationService
//import com.example.moneybuddy2.backend.service.FinanceAnalysisService
//import com.example.moneybuddy2.backend.service.RecommendationEngine
//import io.ktor.http.HttpStatusCode
//import io.ktor.server.application.*
//import io.ktor.server.request.*
//import io.ktor.server.response.*
//import io.ktor.server.routing.*
//
//fun Route.recommendationRoutes(
//    authService: AuthService,
//    financeAnalysisService: FinanceAnalysisService,
//    budgetRecommendationService: BudgetRecommendationService,
//    recommendationEngine: RecommendationEngine
//) {
//    post("/recommendations") {
//        try {
//            val authHeader = call.request.headers["Authorization"]
//            val uid = authService.verifyAndGetUid(authHeader)
//
//            val request = call.receive<RecommendationRequest>()
//
//            val preference = BudgetPreferenceInput(
//                lifestyleNote = request.lifestyleNote,
//                priorities = request.priorities,
//                riskPreference = request.riskPreference,
//                savingGoalNote = request.savingGoalNote
//            )
//
//            val snapshot = financeAnalysisService.buildSnapshot(uid)
//            val budgetPlan = budgetRecommendationService.buildPlan(snapshot, preference)
//            val recommendations = recommendationEngine.buildSmartRecommendations(
//                snapshot = snapshot,
//                budgetPlan = budgetPlan,
//                preference = preference
//            )
//            val summary = recommendationEngine.buildSummary(
//                snapshot = snapshot,
//                plan = budgetPlan,
//                recommendations = recommendations
//            )
//
//            call.respond(
//                RecommendationResponse(
//                    budgetPlan = budgetPlan,
//                    recommendations = recommendations,
//                    summary = summary
//                )
//            )
//        } catch (e: IllegalArgumentException) {
//            e.printStackTrace()
//            call.respond(
//                HttpStatusCode.Unauthorized,
//                RecommendationResponse(
//                    budgetPlan = com.example.moneybuddy2.backend.data.BudgetPlanRecommendation(
//                        incomeUsed = null,
//                        incomeConfidence = "missing",
//                        ruleLabel = "Unavailable",
//                        needsTarget = null,
//                        wantsTarget = null,
//                        savingsTarget = null,
//                        notes = listOf("Authentication failed.")
//                    ),
//                    recommendations = emptyList(),
//                    summary = e.message ?: "Unauthorized"
//                )
//            )
//        } catch (e: Exception) {
//            e.printStackTrace()
//            call.respond(
//                HttpStatusCode.InternalServerError,
//                RecommendationResponse(
//                    budgetPlan = com.example.moneybuddy2.backend.data.BudgetPlanRecommendation(
//                        incomeUsed = null,
//                        incomeConfidence = "missing",
//                        ruleLabel = "Unavailable",
//                        needsTarget = null,
//                        wantsTarget = null,
//                        savingsTarget = null,
//                        notes = listOf("Server error.")
//                    ),
//                    recommendations = emptyList(),
//                    summary = e.message ?: "Server error"
//                )
//            )
//        }
//    }
//}