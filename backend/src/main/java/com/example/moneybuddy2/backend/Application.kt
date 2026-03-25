package com.example.moneybuddy2.backend

import com.example.moneybuddy2.backend.config.FirebaseConfig
import com.example.moneybuddy2.backend.repository.FinanceRepository
import com.example.moneybuddy2.backend.routes.aiRecommendationRoutes
import com.example.moneybuddy2.backend.routes.chatRoutes
//import com.example.moneybuddy2.backend.routes.recommendationRoutes
import com.example.moneybuddy2.backend.service.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.respondText
import io.ktor.server.routing.*

fun main() {
    FirebaseConfig.init()

    val db = FirebaseConfig.firestore()
    val financeRepository = FinanceRepository(db)
    val financeAnalysisService = FinanceAnalysisService(financeRepository)
    val authService = AuthService()
    val insightsFactsBuilder = InsightsFactsBuilder()
    val geminiService = GeminiService()
    val recommendationEngine = RecommendationEngine()
    val budgetRecommendationService = BudgetRecommendationService()
    val geminiRecommendationService = GeminiRecommendationService()


    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }

        routing {
            get("/") {
                call.respondText("MoneyBuddy backend running")
            }
            chatRoutes(
                authService = authService,
                financeAnalysisService = financeAnalysisService,
                insightsFactsBuilder = insightsFactsBuilder,
                geminiService = geminiService,
            )
//            recommendationRoutes(
//                authService = authService,
//                financeAnalysisService = financeAnalysisService,
//                recommendationEngine = recommendationEngine,
//                budgetRecommendationService = budgetRecommendationService
//            )

            aiRecommendationRoutes(
                authService = authService,
                financeAnalysisService = financeAnalysisService,
                geminiRecommendationService = geminiRecommendationService
            )
        }
    }.start(wait = true)
}