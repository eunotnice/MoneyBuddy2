package com.example.moneybuddy2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneybuddy2.core.chat.ChatPrompts
import com.example.moneybuddy2.core.chat.IntentClassifier
import com.example.moneybuddy2.data.remote.GeminiChatService
import com.example.moneybuddy2.data.repository.MoneyRepository
import com.example.moneybuddy2.data.repository.InsightsFactsBuilder
import com.example.moneybuddy2.data.model.ChatIntent
import com.example.moneybuddy2.data.model.ChatMessage
import com.example.moneybuddy2.data.model.ChatUiState
import com.example.moneybuddy2.data.model.Role
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.moneybuddy2.data.remote.FirebaseProvider
import com.example.moneybuddy2.data.remote.GeminiChatResult

class ChatViewModel(
    private val repo: MoneyRepository,
    private val geminiChat: GeminiChatService = GeminiChatService()
) : ViewModel() {

    private val _ui = MutableStateFlow(ChatUiState())
    val ui: StateFlow<ChatUiState> = _ui

    fun send(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty()) return

        _ui.value = _ui.value.copy(
            messages = _ui.value.messages + ChatMessage(Role.USER, trimmed),
            sending = true,
            error = null
        )

        viewModelScope.launch {
            try {
                val intent = IntentClassifier.classify(trimmed)

                val replyText = when (intent) {
                    ChatIntent.PERSONAL_ADVICE_REQUEST -> {
                        ChatPrompts.PERSONAL_ADVICE_RESPONSE
                    }

                    ChatIntent.FIN_LITERACY_GENERAL, ChatIntent.UNKNOWN -> {
                        withContext(Dispatchers.IO) {
                            geminiChat.generateTextBlocking(
                                system = ChatPrompts.FIN_LITERACY_SYSTEM,
                                user = trimmed
                            ).text
                        }
                    }

                    ChatIntent.APP_INSIGHTS -> {
                        val facts = buildUserFactsOrExplainMissing()
                        withContext(Dispatchers.IO) {
                            geminiChat.generateTextBlocking(
                                system = ChatPrompts.APP_INSIGHTS_SYSTEM,
                                user = "QUESTION:\n$trimmed\n\nFACTSHEET(JSON):\n${facts.json}"
                            ).text
                        }
                    }
                }

                _ui.value = _ui.value.copy(
                    messages = _ui.value.messages + ChatMessage(Role.ASSISTANT, replyText),
                    sending = false
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(
                    sending = false,
                    error = e.message ?: "Chat failed"
                )
            }
        }
    }

    private suspend fun buildUserFactsOrExplainMissing(): com.example.moneybuddy2.data.repository.InsightsFacts {
        val user = FirebaseProvider.auth.currentUser
            ?: return InsightsFactsBuilder.buildFactsheet(
                periodDays = 30,
                totalSpend = 0.0,
                avgDailySpend = 0.0,
                topCategories = emptyList(),
                goalSummary = "User not logged in."
            )

        // Example: last 30 days
        val end = System.currentTimeMillis()
        val start = end - 30L * 24L * 60L * 60L * 1000L

        val expenses = repo.listExpensesInRange(user.uid, start, end)
        val total = expenses.sumOf { it.amount }
        val avgDaily = total / 30.0

        val byCat = expenses.groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        return InsightsFactsBuilder.buildFactsheet(
            periodDays = 30,
            totalSpend = total,
            avgDailySpend = avgDaily,
            topCategories = byCat,
            goalSummary = null // add when you implement goals
        )
    }
}