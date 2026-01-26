package com.example.moneybuddy2.core.chat

import com.example.moneybuddy2.data.model.ChatIntent

object IntentClassifier {
    fun classify(userText: String): ChatIntent{
        val t = userText.lowercase()

        val personalAdviceSignals = listOf(
            "should i", "what should i do", "for me", "my situation", "my income",
            "invest in", "buy stock", "which stock", "crypto", "loan", "borrow",
            "credit card", "hire purchase", "tax", "legal"
        )

        if(personalAdviceSignals.any { t.contains(it) }) {
            return ChatIntent.PERSONAL_ADVICE_REQUEST
        }

        val appInsightSignals = listOf(
            "my spending", "this month", "last month", "how much did i spend",
            "am i overspending", "budget", "goal", "saving goal", "category", "top category"
        )
        if(appInsightSignals.any { t.contains(it)}) {
            return ChatIntent.APP_INSIGHTS
        }

        val literacySignals = listOf(
            "what is", "explain", "difference between", "how does", "why", "tips",
            "emergency fund", "compound interest", "budgeting", "50/30/20", "needs and wants"
        )
        if(literacySignals.any { t.contains(it)}){
            return ChatIntent.FIN_LITERACY_GENERAL
        }

        return ChatIntent.UNKNOWN
    }
}