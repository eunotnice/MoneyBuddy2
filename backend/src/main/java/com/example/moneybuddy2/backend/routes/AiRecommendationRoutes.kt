package com.example.moneybuddy2.backend.routes

import com.example.moneybuddy2.backend.data.AiRecommendationRequest
import com.example.moneybuddy2.backend.data.AiRecommendationResponse
import com.example.moneybuddy2.backend.data.AiBudgetPlan
import com.example.moneybuddy2.backend.service.AuthService
import com.example.moneybuddy2.backend.service.FinanceAnalysisService
import com.example.moneybuddy2.backend.service.GeminiRecommendationService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.aiRecommendationRoutes(
    authService: AuthService,
    financeAnalysisService: FinanceAnalysisService,
    geminiRecommendationService: GeminiRecommendationService
) {
    post("/recommendations") {
        try {
            val authHeader = call.request.headers["Authorization"]
            val uid = authService.verifyAndGetUid(authHeader)

            val request = call.receive<AiRecommendationRequest>()
            val snapshot = financeAnalysisService.buildSnapshot(uid)

            val result = geminiRecommendationService.generateRecommendation(
                requestInput = request,
                snapshot = snapshot
            )
            call.respond(result)

        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            call.respond(
                HttpStatusCode.Unauthorized,
                AiRecommendationResponse(
                    budgetPlan = AiBudgetPlan(
                        ruleLabel = "Unavailable",
                        incomeUsed = null,
                        incomeConfidence = "missing",
                        needsTarget = null,
                        wantsTarget = null,
                        savingsTarget = null,
                        rationale = "Authentication failed."
                    ),
                    summary = e.message ?: "Unauthorized",
                    recommendations = emptyList()
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                AiRecommendationResponse(
                    budgetPlan = AiBudgetPlan(
                        ruleLabel = "Unavailable",
                        incomeUsed = null,
                        incomeConfidence = "missing",
                        needsTarget = null,
                        wantsTarget = null,
                        savingsTarget = null,
                        rationale = "Server error."
                    ),
                    summary = e.message ?: "Server error",
                    recommendations = emptyList()
                )
            )
        }
    }
}