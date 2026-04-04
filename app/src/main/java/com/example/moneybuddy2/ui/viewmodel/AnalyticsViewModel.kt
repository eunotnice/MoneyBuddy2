package com.example.moneybuddy2.ui.viewmodel

import androidx.compose.ui.graphics.Color
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
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime

enum class InsightSeverity {
    POSITIVE, NEUTRAL, WARNING
}

data class InsightCardUi(
    val title: String,
    val message: String,
    val severity: InsightSeverity
)

data class CategoryMovementUi(
    val category: String,
    val thisMonth: Double,
    val lastMonth: Double,
    val difference: Double,
    val pctChange: Double?,
    val color: Color
)

data class FinancialHealthUi(
    val incomeTotal: Double = 0.0,
    val expenseTotal: Double = 0.0,
    val savingsAmount: Double = 0.0,
    val expenseToIncomeRatio: Double? = null,
    val savingsRate: Double? = null
)

data class CarbonBreakdownUi(
    val category: String,
    val carbonKg: Double,
    val sharePct: Double
)

data class AnalyticsUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val selectedMonth: YearMonth = YearMonth.now(),

    // raw month summary
    val thisMonthTotal: Double = 0.0,
    val lastMonthTotal: Double = 0.0,
    val pctChangeVsLastMonth: Double? = null,

    // raw source data
    val expensesThisMonth: List<Expense> = emptyList(),
    val incomeThisMonth: List<Income> = emptyList(),

    // current visual summaries
    val categoryTotals: List<CategorySlice> = emptyList(),
    val dailyAverage: Double = 0.0,
    val activeDayAverage: Double = 0.0,

    // financial health
    val financialHealth: FinancialHealthUi = FinancialHealthUi(),

    // interpreted category movement
    val categoryMovements: List<CategoryMovementUi> = emptyList(),
    val topCategory: String? = null,
    val topCategoryAmount: Double = 0.0,
    val topCategoryShare: Double = 0.0,

    // carbon
    val monthlyCarbonKg: Double? = null,
    val treesEquivalent: Double? = null,
    val carbonBreakdown: List<CarbonBreakdownUi> = emptyList(),
    val topCarbonCategory: String? = null,

    // headline insights for UI
    val insights: List<InsightCardUi> = emptyList(),
    val carbonTips: List<String> = emptyList()
)

class AnalyticsViewModel(
    private val repo: MoneyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val ui: StateFlow<AnalyticsUiState> = _uiState

    fun setMonth(month: YearMonth) {
        _uiState.value = _uiState.value.copy(selectedMonth = month)
        load()
    }

    fun load() {
        val user = FirebaseProvider.auth.currentUser
        if (user == null) {
            _uiState.value = _uiState.value.copy(error = "User not logged in")
            return
        }

        _uiState.value = _uiState.value.copy(loading = true, error = null)

        viewModelScope.launch {
            try {
                val zone = ZoneId.systemDefault()

                fun rangeOf(ym: YearMonth): Pair<Long, Long> {
                    val start = ym.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
                    val endExclusive = ym.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
                    return start to endExclusive
                }

                val month = _uiState.value.selectedMonth
                val (start, endExclusive) = rangeOf(month)
                val (prevStart, prevEndExclusive) = rangeOf(month.minusMonths(1))

                val thisMonthExpenses = repo.listExpensesInRangeExclusive(user.uid, start, endExclusive)
                val lastMonthExpenses = repo.listExpensesInRangeExclusive(user.uid, prevStart, prevEndExclusive)
                val thisMonthIncome = repo.listIncomeInRange(user.uid, start, endExclusive)

                val thisTotal = thisMonthExpenses.sumOf { it.amount }
                val lastTotal = lastMonthExpenses.sumOf { it.amount }
                val incomeTotal = thisMonthIncome.sumOf { it.amount }

                val pctChange = if (lastTotal > 0.0) {
                    ((thisTotal - lastTotal) / lastTotal) * 100.0
                } else null

                val slices = buildCategorySlices(thisMonthExpenses, thisTotal)
                val movements = buildCategoryMovements(thisMonthExpenses, lastMonthExpenses)

                val topSlice = slices.maxByOrNull { it.amount }

                val isCurrentMonth = (month == YearMonth.now(zone))
                val calendarDayDivisor = if (isCurrentMonth) {
                    ZonedDateTime.now(zone).dayOfMonth.toDouble().coerceAtLeast(1.0)
                } else {
                    month.lengthOfMonth().toDouble()
                }
                val dailyAverage = if (calendarDayDivisor > 0) thisTotal / calendarDayDivisor else 0.0

                val activeDays = thisMonthExpenses
                    .map { exp: Expense ->
                        Instant.ofEpochMilli(exp.dateMillis)
                            .atZone(zone)
                            .toLocalDate()
                    }
                    .distinct()
                    .size
                    .coerceAtLeast(1)
                val activeDayAverage = if (thisTotal > 0.0) thisTotal / activeDays else 0.0

                val savingsAmount = incomeTotal - thisTotal
                val expenseRatio = if (incomeTotal > 0.0) thisTotal / incomeTotal else null
                val savingsRate = if (incomeTotal > 0.0) savingsAmount / incomeTotal else null

                val financialHealth = FinancialHealthUi(
                    incomeTotal = incomeTotal,
                    expenseTotal = thisTotal,
                    savingsAmount = savingsAmount,
                    expenseToIncomeRatio = expenseRatio,
                    savingsRate = savingsRate
                )

                val carbonTotal = thisMonthExpenses.sumOf { it.co2eKg ?: 0.0 }.takeIf { it > 0.0 }
                val carbonBreakdown = buildCarbonBreakdown(thisMonthExpenses)
                val topCarbonCategory = carbonBreakdown.maxByOrNull { it.carbonKg }?.category
                val treesEq = carbonTotal?.let { it / 21.0 }

                val carbonTips = if (carbonTotal != null && carbonTotal > 0.0)
                    buildCarbonTips(carbonBreakdown, carbonTotal)
                else emptyList()

                val insights = buildInsights(
                    thisTotal = thisTotal,
                    lastTotal = lastTotal,
                    pctChange = pctChange,
                    financialHealth = financialHealth,
                    topSlice = topSlice,
                    categoryMovements = movements,
                    carbonBreakdown = carbonBreakdown,
                    topCarbonCategory = topCarbonCategory
                )

                _uiState.value = AnalyticsUiState(
                    loading = false,
                    error = null,
                    selectedMonth = month,

                    thisMonthTotal = thisTotal,
                    lastMonthTotal = lastTotal,
                    pctChangeVsLastMonth = pctChange,

                    expensesThisMonth = thisMonthExpenses.sortedByDescending { it.dateMillis },
                    incomeThisMonth = thisMonthIncome.sortedByDescending { it.dateMillis },

                    categoryTotals = slices,
                    dailyAverage = dailyAverage,
                    activeDayAverage = activeDayAverage,

                    financialHealth = financialHealth,

                    categoryMovements = movements,
                    topCategory = topSlice?.category,
                    topCategoryAmount = topSlice?.amount ?: 0.0,
                    topCategoryShare = topSlice?.percent ?: 0.0,

                    monthlyCarbonKg = carbonTotal,
                    treesEquivalent = treesEq,
                    carbonBreakdown = carbonBreakdown,
                    topCarbonCategory = topCarbonCategory,

                    insights = insights,
                    carbonTips = carbonTips
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message
                )
            }
        }
    }
}

fun buildCarbonTips(
    breakdown: List<CarbonBreakdownUi>,
    monthlyCarbonKg: Double
): List<String> {
    val tips = mutableListOf<String>()
    val annualKg = monthlyCarbonKg * 12
    val malaysianAvgMonthly = 583.0  // ~7,000 kg/year ÷ 12

    // Personalised per category
    breakdown.forEach { cat ->
        when {
            cat.category.contains("fuel", ignoreCase = true) ||
                    cat.category.contains("transport", ignoreCase = true) -> {
                if (cat.sharePct >= 25)
                    tips += "Transport is ${cat.sharePct.toInt()}% of your footprint. " +
                            "Replacing 2 car trips/week with public transit could save " +
                            "~${"%.1f".format(cat.carbonKg * 0.35)} kgCO₂e/month."
            }
            cat.category.contains("food", ignoreCase = true) -> {
                if (cat.sharePct >= 20)
                    tips += "Cutting red meat 1–2 days/week typically reduces food " +
                            "emissions by 20–30% (~${"%.1f".format(cat.carbonKg * 0.25)} kg saved)."
            }
            cat.category.contains("electric", ignoreCase = true) ||
                    cat.category.contains("bills", ignoreCase = true) -> {
                tips += "Switching to LED bulbs and setting AC to 24–25°C " +
                        "can cut home electricity emissions by 15–20%."
            }
            cat.category.contains("shopping", ignoreCase = true) -> {
                if (cat.sharePct >= 20)
                    tips += "Delaying non-essential purchases by 30 days reduces impulse " +
                            "buying and associated manufacturing emissions."
            }
        }
    }

    // Global benchmark context
    val vsAvg = ((monthlyCarbonKg - malaysianAvgMonthly) / malaysianAvgMonthly) * 100
    val benchmarkMsg = when {
        monthlyCarbonKg < malaysianAvgMonthly * 0.5 ->
            "Your footprint (~${"%.0f".format(monthlyCarbonKg)} kg) is well below the " +
                    "Malaysian average of ~583 kg/month. Great work!"
        monthlyCarbonKg < malaysianAvgMonthly ->
            "Your footprint (~${"%.0f".format(monthlyCarbonKg)} kg) is below the " +
                    "Malaysian average of ~583 kg/month."
        else ->
            "Your footprint (~${"%.0f".format(monthlyCarbonKg)} kg) is " +
                    "${"%.0f".format(vsAvg)}% above the Malaysian average of ~583 kg/month."
    }
    tips += benchmarkMsg

    return tips.take(3)
}
private fun buildCategorySlices(
    expenses: List<Expense>,
    total: Double
): List<CategorySlice> {
    if (total <= 0.0) return emptyList()

    val categoryTotals = expenses
        .groupBy { it.category.ifBlank { "Other" } }
        .mapValues { (_, items) -> items.sumOf { it.amount } }

    return categoryTotals.entries
        .sortedByDescending { it.value }
        .map { (category, amount) ->
            CategorySlice(
                category = category,
                amount = amount,
                percent = (amount / total) * 100.0,
                color = colorForCategory(category)
            )
        }
}

private fun colorForCategory(category: String): Color {
    return when (category.lowercase()) {
        "food", "groceries" -> Color(0xFF66BB6A)
        "transport", "fuel" -> Color(0xFF42A5F5)
        "shopping" -> Color(0xFFAB47BC)
        "bills" -> Color(0xFFFF7043)
        "entertainment" -> Color(0xFFFFCA28)
        else -> Color(0xFF26C6DA)
    }
}

private fun buildCategoryMovements(
    thisMonth: List<Expense>,
    lastMonth: List<Expense>
): List<CategoryMovementUi> {

    val thisMap = thisMonth
        .groupBy { it.category.ifBlank { "Other" } }
        .mapValues { (_, items) -> items.sumOf { it.amount } }

    val lastMap = lastMonth
        .groupBy { it.category.ifBlank { "Other" } }
        .mapValues { (_, items) -> items.sumOf { it.amount } }

    val allCategories = (thisMap.keys + lastMap.keys).distinct()

    return allCategories.map { category ->
        val thisAmt = thisMap[category] ?: 0.0
        val lastAmt = lastMap[category] ?: 0.0
        val diff = thisAmt - lastAmt
        val pct = if (lastAmt > 0.0) ((diff / lastAmt) * 100.0) else null

        CategoryMovementUi(
            category = category,
            thisMonth = thisAmt,
            lastMonth = lastAmt,
            difference = diff,
            pctChange = pct,
            color = colorForCategory(category)
        )
    }.sortedByDescending { kotlin.math.abs(it.difference) }
}

private fun buildCarbonBreakdown(expenses: List<Expense>): List<CarbonBreakdownUi> {
    val carbonByCategory = expenses
        .groupBy { it.category.ifBlank { "Other" } }
        .mapValues { (_, items) -> items.sumOf { it.co2eKg ?: 0.0 } }
        .filterValues { it > 0.0 }

    val totalCarbon = carbonByCategory.values.sum()
    if (totalCarbon <= 0.0) return emptyList()

    return carbonByCategory.entries
        .sortedByDescending { it.value }
        .map { (category, carbonKg) ->
            CarbonBreakdownUi(
                category = category,
                carbonKg = carbonKg,
                sharePct = (carbonKg / totalCarbon) * 100.0
            )
        }
}

private fun buildInsights(
    thisTotal: Double,
    lastTotal: Double,
    pctChange: Double?,
    financialHealth: FinancialHealthUi,
    topSlice: CategorySlice?,
    categoryMovements: List<CategoryMovementUi>,
    carbonBreakdown: List<CarbonBreakdownUi>,
    topCarbonCategory: String?
): List<InsightCardUi> {

    val insights = mutableListOf<InsightCardUi>()

    // 1. Spending change insight
    if (pctChange != null) {
        val mainDriver = categoryMovements
            .filter { it.difference > 0.0 }
            .maxByOrNull { it.difference }

        if (pctChange > 10.0) {
            insights += InsightCardUi(
                title = "Spending increased",
                message = if (mainDriver != null)
                    "You spent ${"%.1f".format(pctChange)}% more than last month, mainly driven by ${mainDriver.category}."
                else
                    "You spent ${"%.1f".format(pctChange)}% more than last month.",
                severity = InsightSeverity.WARNING
            )
        } else if (pctChange < -10.0) {
            insights += InsightCardUi(
                title = "Spending improved",
                message = "You spent ${"%.1f".format(kotlin.math.abs(pctChange))}% less than last month.",
                severity = InsightSeverity.POSITIVE
            )
        }
    }

    // 2. Financial health insight
    val ratio = financialHealth.expenseToIncomeRatio
    if (ratio != null) {
        when {
            ratio >= 1.0 -> insights += InsightCardUi(
                title = "Overspending risk",
                message = "Your expenses exceeded your income this month.",
                severity = InsightSeverity.WARNING
            )
            ratio >= 0.8 -> insights += InsightCardUi(
                title = "Tight spending margin",
                message = "Your expenses used ${"%.0f".format(ratio * 100)}% of this month’s income.",
                severity = InsightSeverity.WARNING
            )
            else -> insights += InsightCardUi(
                title = "Healthy balance",
                message = "Your expenses used ${"%.0f".format(ratio * 100)}% of this month’s income, leaving room for savings.",
                severity = InsightSeverity.POSITIVE
            )
        }
    }

    // 3. Top category concentration insight
    if (topSlice != null) {
        val severity = if (topSlice.percent >= 40.0) InsightSeverity.WARNING else InsightSeverity.NEUTRAL
        insights += InsightCardUi(
            title = "Largest spending category",
            message = "${topSlice.category} accounted for ${"%.0f".format(topSlice.percent)}% of this month’s spending.",
            severity = severity
        )
    }

    // 4. Carbon insight
    if (carbonBreakdown.isNotEmpty() && topCarbonCategory != null) {
        val topCarbon = carbonBreakdown.first()
        insights += InsightCardUi(
            title = "Top carbon source",
            message = "$topCarbonCategory contributed ${"%.0f".format(topCarbon.sharePct)}% of your estimated carbon footprint.",
            severity = InsightSeverity.NEUTRAL
        )
    }

    return insights.take(4)
}

