package com.example.moneybuddy2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneybuddy2.core.util.DateUtils
import com.example.moneybuddy2.data.model.Expense
import com.example.moneybuddy2.data.model.UserProfile
import com.example.moneybuddy2.data.remote.FirebaseProvider
import com.example.moneybuddy2.data.repository.MoneyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState (
    val loading: Boolean = false,
    val error: String? = null,
    val profile: UserProfile? = null,
    val monthTotal: Double = 0.0,
    val latestExpenses: List<Expense> = emptyList(),
    val monthCategoryTotals: List<Pair<String, Double>> = emptyList(),
    val budgetUsedRatio: Double = 0.0,
    val showBudgetWarning: Boolean = false,
    val needsProfileSetup: Boolean = false
)

class HomeViewModel (
    private val repo: MoneyRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState(loading=true))
    val uiState: StateFlow<HomeUiState> = _uiState

    fun loadHome(){
        val user = FirebaseProvider.auth.currentUser
        if (user==null){
            _uiState.value = HomeUiState(error = "User not logged in")
            return
        }

        _uiState.value = HomeUiState(loading = true)

        viewModelScope.launch {
            try {
                val profile = repo.getUserProfile(user.uid)

                val start = DateUtils.startOfCurrentMonthMillis()
                val end = DateUtils.endOfCurrentMonthMillis()
                android.util.Log.d("HomeVM", "Month window start=$start end=$end")

                val monthExpenses = repo.listExpensesInRange(user.uid, start, end)
                val monthTotal = monthExpenses.sumOf { it.amount }
                android.util.Log.d("HomeVM", "Month expenses count=${monthExpenses.size}")
                val categoryTotals = monthExpenses
                    .groupBy { it.category }
                    .mapValues { (_, items) -> items.sumOf { it.amount } }
                    .toList()
                    .sortedByDescending { it.second }
                val needsSetup = (profile == null) ||
                        profile.displayName.isBlank() ||
                        profile.monthlyBudget <= 0.0

                val latest = repo.listLatestExpenses(user.uid, limit = 10)

                val budget = profile?.monthlyBudget ?: 0.0
                val ratio = if (budget > 0.0) (monthTotal / budget) else 0.0
                val warn = (budget > 0.0) && (ratio >= 0.90)

                _uiState.value = HomeUiState(
                    loading = false,
                    profile = profile,
                    monthTotal = monthTotal,
                    latestExpenses = latest,
                    monthCategoryTotals = categoryTotals,
                    budgetUsedRatio = ratio,
                    showBudgetWarning = warn,
                    needsProfileSetup = needsSetup
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState(error = e.message)
            }
        }
    }

    fun deleteExpense(expenseId: String) {
        val user = FirebaseProvider.auth.currentUser ?: return
        viewModelScope.launch {
            val ok = repo.deleteExpense(user.uid, expenseId)
            if (!ok) {
                _uiState.value = _uiState.value.copy(error = "Failed to delete expense")
            }
            loadHome()
        }
    }
}