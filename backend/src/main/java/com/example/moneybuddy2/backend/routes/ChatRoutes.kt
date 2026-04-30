package com.example.moneybuddy2.backend.routes

import com.example.moneybuddy2.backend.data.ChatRequest
import com.example.moneybuddy2.backend.data.ChatResponse
import com.example.moneybuddy2.backend.service.AuthService
import com.example.moneybuddy2.backend.service.FinanceAnalysisService
import com.example.moneybuddy2.backend.service.GeminiService
import com.example.moneybuddy2.backend.service.InsightsFactsBuilder
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.chatRoutes(
    authService: AuthService,
    financeAnalysisService: FinanceAnalysisService,
    insightsFactsBuilder: InsightsFactsBuilder,
    geminiService: GeminiService
) {
    post("/chat") {
        try {
            val authHeader = call.request.headers["Authorization"]
            val uid = authService.verifyAndGetUid(authHeader)

            val request = call.receive<ChatRequest>()
            val snapshot = financeAnalysisService.buildSnapshot(uid)
            val facts = insightsFactsBuilder.fromSnapshot(snapshot)

            val answer = geminiService.generateAnswer(
                userMessage = request.message,
                snapshot = snapshot,
                facts = facts,
                systemPrompt = request.systemPrompt  // ← added
            )

            call.respond(ChatResponse(answer = answer))
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                ChatResponse(answer = e.message ?: "Server error")
            )
        }
    }
}