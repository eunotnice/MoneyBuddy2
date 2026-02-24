package com.example.moneybuddy2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneybuddy2.core.util.DateUtils
import com.example.moneybuddy2.data.model.CategorySlice
import com.example.moneybuddy2.data.model.Expense
import com.example.moneybuddy2.data.model.Income
import com.example.moneybuddy2.data.remote.FirebaseProvider
import com.example.moneybuddy2.data.repository.MoneyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime

data class AnalyticsUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val selectedMonth: YearMonth = YearMonth.now(),

    val thisMonthTotal: Double = 0.0,
    val lastMonthTotal: Double = 0.0,
    val pctChangeVsLastMonth: Double? = null, // null = not computable

    val categoryTotals: List<CategorySlice> = emptyList(), // includes % and amount
    val expensesThisMonth: List<Expense> = emptyList(),
    val incomeThisMonth: List<Income> = emptyList(),

    val dailyAverage: Double = 0.0,

    val monthlyCarbonKg: Double? = null,
    val treesEquivalent: Double? = null
)

class AnalyticsViewModel(
    private val repo: MoneyRepository
): ViewModel(){
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val ui: StateFlow<AnalyticsUiState> = _uiState

    fun setMonth(m: YearMonth){
        _uiState.value = _uiState.value.copy(selectedMonth = m)
        load()
    }
    fun load(){
        val user = FirebaseProvider.auth.currentUser
        if (user == null) {
            _uiState.value = _uiState.value.copy(error = "User not logged in")
            return
        }

        val month = _uiState.value.selectedMonth

        _uiState.value = _uiState.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val zone = ZoneId.systemDefault()

                fun rangeOf(ym: YearMonth): Pair<Long, Long> {
                    val start = ym.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
                    val endExclusive = ym.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
                    return start to endExclusive // use exclusive end (recommended)
                }

                val month = _uiState.value.selectedMonth

                val (start, endExclusive) = rangeOf(month)
                val (prevStart, prevEndExclusive) = rangeOf(month.minusMonths(1))

                val thisMonth = repo.listExpensesInRangeExclusive(user.uid, start, endExclusive)
                val lastMonth = repo.listExpensesInRangeExclusive(user.uid, prevStart, prevEndExclusive)
                val income = repo.listIncomeInRange(user.uid, start, endExclusive)

                val thisTotal = thisMonth.sumOf { it.amount }
                val lastTotal = lastMonth.sumOf { it.amount }

                val pctChange = if (lastTotal > 0.0) {
                    ((thisTotal - lastTotal) / lastTotal) * 100.0
                } else null

                val catMap = thisMonth
                    .groupBy { it.category.ifBlank { "Other" } }
                    .mapValues { (_, items) -> items.sumOf { it.amount } }

                val slices = if (thisTotal > 0.0) {
                    catMap.entries
                        .map { (cat, amt) -> CategorySlice(cat, amt, (amt / thisTotal) * 100.0) }
                        .sortedByDescending { it.amount }
                } else emptyList()

// Daily average: for a selected historical month, use full month length;
// for current month, use days elapsed
                val isCurrentMonth = (month == YearMonth.now(zone))
                val daysDivisor = if (isCurrentMonth) {
                    ZonedDateTime.now(zone).dayOfMonth.toDouble().coerceAtLeast(1.0)
                } else {
                    month.lengthOfMonth().toDouble()
                }
                val dailyAvg = if (daysDivisor > 0) thisTotal / daysDivisor else 0.0

// Carbon total from the selected month's expenses
                val carbonTotal = thisMonth.sumOf { it.co2eKg ?: 0.0 }.takeIf { it > 0.0 }

// Tree equivalent (assumption-based; keep configurable later)
                val kgCo2PerTreePerYear = 21.0
                val treesEq = carbonTotal?.let { kg -> kg / kgCo2PerTreePerYear }


                _uiState.value = _uiState.value.copy(
                    loading = false,
                    thisMonthTotal = thisTotal,
                    lastMonthTotal = lastTotal,
                    pctChangeVsLastMonth = pctChange,
                    categoryTotals = slices,
                    expensesThisMonth = thisMonth.sortedByDescending { it.dateMillis },
                    dailyAverage = dailyAvg,
                    monthlyCarbonKg = carbonTotal,
                    treesEquivalent = treesEq
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(loading = false, error = e.message)
            }
        }
    }
}


