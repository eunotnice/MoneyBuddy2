package com.example.moneybuddy2.backend.service

import com.example.moneybuddy2.backend.data.*
import com.example.moneybuddy2.backend.repository.FinanceRepository
import com.example.moneybuddy2.backend.util.DateUtils
import com.example.moneybuddy2.backend.util.categoryToBucket
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class FinanceAnalysisService(
    private val repository: FinanceRepository
) {
    fun median(values: List<Double>): Double? {
        if (values.isEmpty()) return null
        val sorted = values.sorted()
        val mid = sorted.size / 2
        return if (sorted.size % 2 == 0) {
            (sorted[mid - 1] + sorted[mid]) / 2.0
        } else {
            sorted[mid]
        }
    }

    fun computeBudgetBucketSummary(expenses: List<Expense>): BudgetBucketSummary {
        var needs = 0.0
        var wants = 0.0
        var uncategorised = 0.0

        for (expense in expenses) {
            when (categoryToBucket(expense.category)) {
                BudgetBucket.NEEDS -> needs += expense.amount
                BudgetBucket.WANTS -> wants += expense.amount
                BudgetBucket.UNCATEGORISED -> uncategorised += expense.amount
            }
        }

        return BudgetBucketSummary(
            needsTotal = needs,
            wantsTotal = wants,
            uncategorisedTotal = uncategorised,
            byBucket = mapOf(
                "needs" to needs,
                "wants" to wants,
                "uncategorised" to uncategorised
            )
        )
    }

    suspend fun assessIncome(
        uid: String,
        periodStartMillis: Long,
        periodEndMillis: Long,
        zoneId: ZoneId = ZoneId.of("Asia/Kuala_Lumpur")
    ): IncomeAssessment {
        val currentIncomes = repository.getIncomesInRange(uid, periodStartMillis, periodEndMillis)
        val currentTotal = currentIncomes.sumOf { it.amount }

        if (currentTotal > 0.0) {
            return IncomeAssessment(
                totalIncome = currentTotal,
                confidence = "known",
                estimatedFrom = "current_period"
            )
        }

        val historicStart = Instant.ofEpochMilli(periodStartMillis)
            .minus(90, ChronoUnit.DAYS)
            .toEpochMilli()

        val historicIncomes = repository.getIncomesInRange(uid, historicStart, periodEndMillis)

        if (historicIncomes.isNotEmpty()) {
            val monthlyTotals = historicIncomes
                .groupBy { DateUtils.monthKeyFromMillis(it.dateMillis, zoneId) }
                .mapValues { (_, incomes) -> incomes.sumOf { it.amount } }
                .values
                .filter { it > 0.0 }
                .toList()

            val monthlyMedian = median(monthlyTotals)
            if (monthlyMedian != null && monthlyMedian > 0.0) {
                return IncomeAssessment(
                    totalIncome = monthlyMedian,
                    confidence = "estimated",
                    estimatedFrom = "median_last_3_months"
                )
            }
        }

        return IncomeAssessment(
            totalIncome = null,
            confidence = "missing",
            estimatedFrom = null
        )
    }

    suspend fun buildSnapshot(
        uid: String,
        periodDays: Int = 30,
        nowMillis: Long = System.currentTimeMillis()
    ): UserFinanceSnapshot {
        val (start, end) = DateUtils.currentMonthRange(nowMillis)

        val expenses = repository.getExpensesInRange(uid, start, end)
        val incomeAssessment = assessIncome(uid, start, end)

        val spendByCategory = expenses.groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.amount } }

        val topCategories = spendByCategory.toList()
            .sortedByDescending { it.second }
            .take(5)

        val bucketSummary = computeBudgetBucketSummary(expenses)

        val totalCo2e = expenses.sumOf { it.co2eKg ?: 0.0 }.takeIf { it > 0.0 }

        val co2eByCategory = expenses
            .groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.co2eKg ?: 0.0 } }

        return UserFinanceSnapshot(
            userId = uid,
            nowMillis = nowMillis,
            periodStartMillis = start,
            periodEndMillis = end,
            periodLabel = DateUtils.currentMonthLabel(nowMillis),
            totalSpent = expenses.sumOf { it.amount },
            totalIncome = incomeAssessment.totalIncome,
            incomeConfidence = incomeAssessment.confidence,
            spendByCategory = spendByCategory,
            topSpendCategories = topCategories,
            needsSpent = bucketSummary.needsTotal,
            wantsSpent = bucketSummary.wantsTotal,
            uncategorisedSpent = bucketSummary.uncategorisedTotal,
            totalCo2eKg = totalCo2e,
            co2eByCategory = co2eByCategory,
            treesEquivalent = null,
            treesFactorLabel = null
        )
    }

}

