package com.example.moneybuddy2.core.chat

import com.example.moneybuddy2.data.model.FaqItem
import kotlin.math.max

sealed class BotAction{
    data class Reply(val text: String, val quickReplies: List<String> = emptyList()) : BotAction()
    data class OpenWhatsapp(val prefillMessage: String) : BotAction()
    data class ShowFaqCategory(val category: String) : BotAction()
}

object BotRouter {
    private val bookingSignals = listOf(
        "book", "booking", "appointment", "consult", "consultation", "schedule",
        "whatsapp", "talk to", "call", "meet"
    )

    fun route(
        userText: String,
        faqs: List<FaqItem>,
        categories: List<String>
    ): BotAction {
        val t = userText.trim().lowercase()
        //booking+whatsapp
        if(bookingSignals.any { t.contains(it)}){
            return BotAction.OpenWhatsapp(
                prefillMessage = "Hi, I would like to book a financial consultation appointment."
            )
        }

        //category shortcut
        val matchedCategory = categories.firstOrNull{ t.contains(it.lowercase())}
        if(matchedCategory != null){
            return BotAction.ShowFaqCategory(matchedCategory)
        }

        //best faq matched by keyword scoring
        val best = bestFaqMatch(t, faqs)
        if(best != null){
            return BotAction.Reply(
                text = "${best.question}\n\n${best.answer}",
                quickReplies = listOf("Book consultation", "Show FAQ categories")
            )
        }

        //fallback menu
        return BotAction.Reply(
            text = "I can help with SBH FAQ and booking. Choose a topic below, or type “book consultation”.",
            quickReplies = categories + "Book consultation"
        )
    }

    private fun bestFaqMatch(user: String, faqs: List<FaqItem>): FaqItem? {
        var bestScore = 0
        var best: FaqItem? = null

        for (f in faqs) {
            var score = 0
            for (kw in f.keywords) {
                val k = kw.lowercase()
                if (user.contains(k)) score += 2
            }
            // also reward question substring overlap (very light)
            val q = f.question.lowercase()
            if (user.length >= 4 && q.contains(user)) score += 1

            if (score > bestScore) {
                bestScore = score
                best = f
            }
        }

        // Require at least 2 points to avoid wrong answers
        return if (bestScore >= 2) best else null
    }
}