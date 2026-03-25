package com.example.moneybuddy2.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.moneybuddy2.data.remote.AiBudgetPlanDto
import com.example.moneybuddy2.data.remote.AiSavingRecommendationDto
import com.example.moneybuddy2.data.repository.AiRecommendationRepository

data class AiRecommendationUiState(
    val loading: Boolean = false,
    val budgetPlan: AiBudgetPlanDto? = null,
    val summary: String = "",
    val recommendations: List<AiSavingRecommendationDto> = emptyList(),
    val error: String? = null
)

class AiRecommendationViewModel(
    private val repository: AiRecommendationRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(AiRecommendationUiState())
    val ui: StateFlow<AiRecommendationUiState> = _ui

    fun generate(
        lifestyleNote: String,
        priorities: List<String>,
        riskPreference: String,
        savingGoalNote: String
    ) {
        _ui.value = _ui.value.copy(
            loading = true,
            error = null
        )

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    repository.getRecommendations(
                        lifestyleNote = lifestyleNote.takeIf { it.isNotBlank() },
                        priorities = priorities,
                        riskPreference = riskPreference.takeIf { it.isNotBlank() },
                        savingGoalNote = savingGoalNote.takeIf { it.isNotBlank() }
                    )
                }

                _ui.value = AiRecommendationUiState(
                    loading = false,
                    budgetPlan = result.budgetPlan,
                    summary = result.summary,
                    recommendations = result.recommendations,
                    error = null
                )
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("AI_RECOMMEND", "Recommendation request failed", e)

                _ui.value = AiRecommendationUiState(
                    loading = false,
                    error = "${e::class.simpleName}: ${e.message ?: "Failed to generate recommendations"}"
                )
            }
        }
    }
}

//data class RecommendationUiState(
//    val loading: Boolean = false,
//    val summary: String = "",
//    val budgetPlan: BudgetPlanRecommendationDto? = null,
//    val recommendations: List<SmartRecommendationDto> = emptyList(),
//    val error: String? = null
//)
//
//class RecommendationViewModel(
//    private val repository: RecommendationRepository
//) : ViewModel() {
//
//    private val _ui = MutableStateFlow(RecommendationUiState())
//    val ui: StateFlow<RecommendationUiState> = _ui
//
//    fun generateRecommendations(
//        lifestyleNote: String,
//        priorities: List<String>,
//        riskPreference: String,
//        savingGoalNote: String
//    ) {
//        _ui.value = _ui.value.copy(
//            loading = true,
//            error = null
//        )
//
//        viewModelScope.launch {
//            try {
//                val response = withContext(Dispatchers.IO) {
//                    repository.getRecommendations(
//                        lifestyleNote = lifestyleNote.takeIf { it.isNotBlank() },
//                        priorities = priorities,
//                        riskPreference = riskPreference.takeIf { it.isNotBlank() },
//                        savingGoalNote = savingGoalNote.takeIf { it.isNotBlank() }
//                    )
//                }
//
//                _ui.value = RecommendationUiState(
//                    loading = false,
//                    summary = response.summary,
//                    budgetPlan = response.budgetPlan,
//                    recommendations = response.recommendations,
//                    error = null
//                )
//            } catch (e: Exception) {
//                _ui.value = RecommendationUiState(
//                    loading = false,
//                    error = e.message ?: "Failed to load recommendations"
//                )
//            }
//        }
//    }
//}

//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.moneybuddy2.data.model.RecommendationCard
//import com.example.moneybuddy2.data.model.BudgetPlan
//import com.example.moneybuddy2.core.recommendation.RecommendationsEngine
//import com.example.moneybuddy2.data.remote.FirebaseProvider
//import com.example.moneybuddy2.data.repository.MoneyRepository
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//import java.time.Instant
//import java.time.ZoneId
//import java.time.ZonedDateTime
//import kotlin.math.exp
//
//data class RecoUiState(
//    val loading: Boolean = false,
//    val error: String? = null,
//    val plan: BudgetPlan? = null,
//    val cards: List<RecommendationCard> = emptyList()
//)
//
//class RecommendationViewModel(
//    private val repo: MoneyRepository
//) : ViewModel() {
//
//    private val engine = RecommendationsEngine(zoneId = ZoneId.systemDefault())
//
//    private val _ui = MutableStateFlow(RecoUiState())
//    val ui: StateFlow<RecoUiState> = _ui
//
//    fun loadCurrentMonth() {
//        val user = FirebaseProvider.auth.currentUser
//        if (user == null) {
//            _ui.value = _ui.value.copy(error = "User not logged in")
//            return
//        }
//
//        _ui.value = _ui.value.copy(loading = true, error = null)
//
//        viewModelScope.launch {
//            try {
//                val profile = repo.getUserProfile(user.uid)
//                if (profile == null) {
//                    _ui.value = _ui.value.copy(loading = false, error = "Profile not found")
//                    return@launch
//                }
//
//                val (start, end) = currentMonthRangeMillis()
//                val expenses = repo.listExpensesInRange(user.uid, start, end)
//                val income = repo.listIncomeInRange(user.uid, start, end)
//                val (plan, cards) = engine.generate(profile, income, expenses)
//
//                _ui.value = _ui.value.copy(
//                    loading = false,
//                    plan = plan,
//                    cards = cards,
//                    error = null
//                )
//            } catch (e: Exception) {
//                _ui.value = _ui.value.copy(loading = false, error = e.message ?: "Failed to load recommendations")
//            }
//        }
//    }
//
//    private fun currentMonthRangeMillis(): Pair<Long, Long> {
//        val zone = ZoneId.systemDefault()
//        val now = ZonedDateTime.now(zone)
//        val start = now.withDayOfMonth(1).toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli()
//        val end = now.plusMonths(1).withDayOfMonth(1).toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli()
//        return start to end
//    }
//}