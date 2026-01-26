package com.example.moneybuddy2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneybuddy2.core.chat.ChatPrompts
import com.example.moneybuddy2.core.recommendation.*
import com.example.moneybuddy2.data.remote.FirebaseProvider
import com.example.moneybuddy2.data.remote.GeminiChatService
import com.example.moneybuddy2.data.repository.MoneyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class RecommendationUiState(
    val loading: Boolean = false,
    val recommendations: List<Recommendation> = emptyList(),
    val aiExplanation: String? = null,
    val error: String? = null
)

class RecommendationViewModel(
    private val repo: MoneyRepository,
    private val geminiChat: GeminiChatService = GeminiChatService()
) : ViewModel() {

    private val _ui = MutableStateFlow(RecommendationUiState())
    val ui: StateFlow<RecommendationUiState> = _ui

    fun loadRecommendations(
        goalGap: Double? // pass null if goals not implemented yet
    ) {
        val user = FirebaseProvider.auth.currentUser
            ?: run {
                _ui.value = RecommendationUiState(error = "User not logged in")
                return
            }

        _ui.value = RecommendationUiState(loading = true)

        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val start = now - 30L * 24L * 60L * 60L * 1000L

                val expenses = repo.listExpensesInRange(user.uid, start, now)

                val byCategory = expenses
                    .groupBy { it.category }
                    .mapValues { (_, list) -> list.sumOf { it.amount } }
                val profile = repo.getUserProfile(user.uid)
                val monthlyIncome = profile?.monthlyIncome
                val recs = RecommendationEngine.generate(
                    expenseByCategory = byCategory,
                    monthlyIncome = monthlyIncome,
                    goalGap = goalGap
                )

                if (recs.isEmpty()) {
                    _ui.value = RecommendationUiState(
                        recommendations = emptyList(),
                        aiExplanation = "You're doing well — no major adjustments needed right now."
                    )
                    return@launch
                }

                val facts = RecommendationFactsBuilder.build(
                    recommendations = recs,
                    period = "Last 30 days"
                )

                val explanation = withContext(Dispatchers.IO) {
                    geminiChat.generateTextBlocking(
                        system = ChatPrompts.AI_RECOMMENDATION_SYSTEM,
                        user = facts
                    ).text
                }

                _ui.value = RecommendationUiState(
                    recommendations = recs,
                    aiExplanation = explanation
                )

            } catch (e: Exception) {
                _ui.value = RecommendationUiState(
                    error = e.message ?: "Failed to generate recommendations"
                )
            }
        }
    }

    fun clear() {
        _ui.value = RecommendationUiState()
    }
}
