package com.example.moneybuddy2.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneybuddy2.core.util.DateUtils
import com.example.moneybuddy2.core.util.DateUtils.monthRangeMillis
import com.example.moneybuddy2.data.model.Expense
import com.example.moneybuddy2.data.model.Income
import com.example.moneybuddy2.data.model.UserProfile
import com.example.moneybuddy2.data.remote.FirebaseProvider
import com.example.moneybuddy2.data.repository.MoneyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth
import kotlinx.coroutines.flow.update

data class HomeUiState (
    val loading: Boolean = false,
    val error: String? = null,
    val profile: UserProfile? = null,
    val incomeTotal: Double = 0.0,
    val monthTotal: Double = 0.0,
    val latestIncome: List<Income> = emptyList(),
    val latestExpenses: List<Expense> = emptyList(),
    val monthCategoryTotals: List<Pair<String, Double>> = emptyList(),
    val budgetUsedRatio: Double = 0.0,
    val showBudgetWarning: Boolean = false,
    val needsProfileSetup: Boolean = false,
    val monthlyCarbonKg: Double? = null,
    val carbonLoading: Boolean = false,
    val selectedMonth: YearMonth = YearMonth.now()
)

class HomeViewModel (
    private val repo: MoneyRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadMonthlyCarbon()
    }

    fun loadHome(month: YearMonth = _uiState.value.selectedMonth){
        refreshHomeForMonth(month)
    }

    fun deleteExpense(expenseId: String) {
        val user = FirebaseProvider.auth.currentUser ?: return
        viewModelScope.launch {
            val ok = repo.deleteExpense(user.uid, expenseId)
            if (!ok) {
                _uiState.value = _uiState.value.copy(error = "Failed to delete expense")
            }
            refreshHomeForMonth(_uiState.value.selectedMonth)
        }
    }

    fun loadMonthlyCarbon() {
        Log.d("CARBON", "loadMonthlyCarbon() called")

        val user = FirebaseProvider.auth.currentUser
        if (user == null) {
            Log.e("CARBON", "No user logged in - abort")
            _uiState.value = _uiState.value.copy(
                carbonLoading = false,
                monthlyCarbonKg = 0.0
            )
            return
        }

        _uiState.value = _uiState.value.copy(carbonLoading = true)

        viewModelScope.launch {
            try {
                val total = repo.getMonthlyCarbonTotalKg(user.uid)
                Log.d("CARBON", "Monthly carbon total = $total")
                _uiState.value = _uiState.value.copy(
                    monthlyCarbonKg = total,
                    carbonLoading = false
                )
            } catch (e: Exception) {
                Log.e("CARBON", "Failed to load monthly carbon", e)
                _uiState.value = _uiState.value.copy(
                    monthlyCarbonKg = 0.0,
                    carbonLoading = false
                )
            }
        }
    }

    fun setMonth(month: YearMonth) {
        refreshHomeForMonth(month)
    }

    private fun refreshHomeForMonth(month: YearMonth){
        val user = FirebaseProvider.auth.currentUser
        if (user == null) {
            _uiState.value = _uiState.value.copy(
                loading = false,
                error = "User not logged in"
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            loading = true,
            error = null
        )

        viewModelScope.launch {
            try {
                val profile = repo.getUserProfile(user.uid)
                val (start, end) = monthRangeMillis(month)
                val monthIncome = repo.listIncomeInRange(user.uid, start, end)
                val incomeTotal = monthIncome.sumOf { it.amount }
                val monthExpenses = repo.listExpensesInRange(user.uid, start, end)
                val monthTotal = monthExpenses.sumOf { it.amount }
                val categoryTotals = monthExpenses
                    .groupBy { it.category }
                    .mapValues { (_, items) -> items.sumOf { it.amount } }
                    .toList()
                    .sortedByDescending { it.second }
                val latestIncome = monthIncome
                    .sortedByDescending { it.dateMillis }
                    .take(100)
                val latestExpenses = monthExpenses
                    .sortedByDescending { it.dateMillis }
                    .take(100)
                val carbon = repo.getCarbonTotalKgInRange(user.uid, start, end)
                val needsSetup = (profile == null) ||
                        profile.displayName.isBlank() ||
                        profile.monthlyBudget <= 0.0
                val budget = profile?.monthlyBudget ?: 0.0
                val ratio = if (budget > 0.0) (monthTotal / budget) else 0.0
                val warn = (budget > 0.0) && (ratio >= 0.90)


                _uiState.update {
                    it.copy(
                        incomeTotal = incomeTotal,
                        selectedMonth = month,
                        loading = false,
                        error = null,
                        profile = profile,
                        monthTotal = monthTotal,
                        latestIncome = latestIncome,
                        latestExpenses = latestExpenses,
                        monthCategoryTotals = categoryTotals,
                        budgetUsedRatio = ratio,
                        showBudgetWarning = warn,
                        needsProfileSetup = needsSetup,
                        monthlyCarbonKg = carbon,
                        carbonLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message
                )
            }
        }
    }

}