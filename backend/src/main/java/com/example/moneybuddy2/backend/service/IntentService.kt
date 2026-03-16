package com.example.moneybuddy2.backend.service

enum class ChatIntent {
    FAQ,
    GOAL_PLAN,
    BUDGET_REQUEST,
    SPEND_INSIGHT,
    GENERAL_GUIDANCE
}

class IntentService {
    fun detect(message: String): ChatIntent {
        val text = message.lowercase()

        return when {
            "save" in text && ("by" in text || "date" in text) -> ChatIntent.GOAL_PLAN
            "budget" in text -> ChatIntent.BUDGET_REQUEST
            "spending" in text || "spent" in text || "insight" in text -> ChatIntent.SPEND_INSIGHT
            else -> ChatIntent.GENERAL_GUIDANCE
        }
    }
}