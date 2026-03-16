package com.example.moneybuddy2.backend.routes

import com.example.moneybuddy2.backend.data.RecommendationRequest
import com.example.moneybuddy2.backend.data.RecommendationResponse
import com.example.moneybuddy2.backend.service.FinanceAnalysisService
import com.example.moneybuddy2.backend.service.RecommendationBuilder
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.recommendationRoutes(
    financeAnalysisService: FinanceAnalysisService,
    recommendationBuilder: RecommendationBuilder
) {
    post("/recommendations") {
        val uid = call.request.headers["X-User-Id"] ?: return@post call.respondText("Missing user", status = io.ktor.http.HttpStatusCode.Unauthorized)
        val request = call.receive<RecommendationRequest>()

        val snapshot = financeAnalysisService.buildSnapshot(
            uid = uid,
            periodDays = request.periodDays
        )

        val cards = recommendationBuilder.build(snapshot)
        call.respond(RecommendationResponse(cards))
    }
}