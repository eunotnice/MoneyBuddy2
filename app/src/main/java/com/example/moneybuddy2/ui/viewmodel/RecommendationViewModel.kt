package com.example.moneybuddy2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneybuddy2.data.model.RecommendationCard
import com.example.moneybuddy2.data.model.BudgetPlan
import com.example.moneybuddy2.core.recommendation.RecommendationsEngine
import com.example.moneybuddy2.data.remote.FirebaseProvider
import com.example.moneybuddy2.data.repository.MoneyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.exp

data class RecoUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val plan: BudgetPlan? = null,
    val cards: List<RecommendationCard> = emptyList()
)

class RecommendationViewModel(
    private val repo: MoneyRepository
) : ViewModel() {

    private val engine = RecommendationsEngine(zoneId = ZoneId.systemDefault())

    private val _ui = MutableStateFlow(RecoUiState())
    val ui: StateFlow<RecoUiState> = _ui

    fun loadCurrentMonth() {
        val user = FirebaseProvider.auth.currentUser
        if (user == null) {
            _ui.value = _ui.value.copy(error = "User not logged in")
            return
        }

        _ui.value = _ui.value.copy(loading = true, error = null)

        viewModelScope.launch {
            try {
                val profile = repo.getUserProfile(user.uid)
                if (profile == null) {
                    _ui.value = _ui.value.copy(loading = false, error = "Profile not found")
                    return@launch
                }

                val (start, end) = currentMonthRangeMillis()
                val expenses = repo.listExpensesInRange(user.uid, start, end)
                val income = repo.listIncomeInRange(user.uid, start, end)
                val (plan, cards) = engine.generate(profile, income, expenses)

                _ui.value = _ui.value.copy(
                    loading = false,
                    plan = plan,
                    cards = cards,
                    error = null
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(loading = false, error = e.message ?: "Failed to load recommendations")
            }
        }
    }

    private fun currentMonthRangeMillis(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val now = ZonedDateTime.now(zone)
        val start = now.withDayOfMonth(1).toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli()
        val end = now.plusMonths(1).withDayOfMonth(1).toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli()
        return start to end
    }
}