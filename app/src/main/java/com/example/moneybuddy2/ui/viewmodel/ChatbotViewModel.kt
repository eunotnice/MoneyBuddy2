package com.example.moneybuddy2.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.example.moneybuddy2.core.chat.BotAction
import com.example.moneybuddy2.core.chat.BotRouter
import com.example.moneybuddy2.data.repository.FaqRepository
import com.example.moneybuddy2.data.model.ChatMessage
import com.example.moneybuddy2.data.model.ChatUiState
import com.example .moneybuddy2.data.model.Role
import com.example.moneybuddy2.data.model.FaqItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatbotViewModel (
    private val faqRepo: FaqRepository
) : ViewModel() {

    private val faqs = faqRepo.getFaqs()
    private val categories = faqRepo.categories()

    private val _ui = MutableStateFlow(
        ChatUiState(
            messages = listOf(
                ChatMessage(
                    Role.ASSISTANT,
                    if (faqs.isEmpty())
                        "Hi! FAQ database is not available. Please check company_faq.json in assets."
                    else
                        "Hi! I can help with SBH FAQ and booking. Choose a topic or type your question."
                )
            )
        )
    )
    val ui: StateFlow<ChatUiState> = _ui

    fun send(userText: String): BotAction {
        val trimmed = userText.trim()
        if (trimmed.isEmpty()) return BotAction.Reply("")

        _ui.value = _ui.value.copy(
            messages = _ui.value.messages + ChatMessage(Role.USER, trimmed),
            error = null,
            quickReplies = emptyList()
        )

        val action = BotRouter.route(trimmed, faqs, categories)

        when (action) {
            is BotAction.Reply -> {
                _ui.value = _ui.value.copy(
                    messages = _ui.value.messages + ChatMessage(Role.ASSISTANT, action.text),
                    quickReplies = action.quickReplies
                )
            }
            is BotAction.ShowFaqCategory -> {
                val qs = faqs.filter { it.category == action.category }
                    .joinToString("\n") { "• ${it.question}" }

                _ui.value = _ui.value.copy(
                    messages = _ui.value.messages + ChatMessage(
                        Role.ASSISTANT,
                        "Here are FAQs under ${action.category}:\n$qs\n\nType one of the questions or ask in your own words."
                    ),
                    quickReplies = action.questions + "Book consultation"
                )
            }
            is BotAction.OpenWhatsapp -> {
                _ui.value = _ui.value.copy(
                    messages = _ui.value.messages + ChatMessage(
                        Role.ASSISTANT,
                        "Sure — tap the button below to book a consultation via WhatsApp."
                    )
                )
            }
        }

        return action
    }

    fun getQuickReplies(): List<String> = categories + "Book consultation"
}