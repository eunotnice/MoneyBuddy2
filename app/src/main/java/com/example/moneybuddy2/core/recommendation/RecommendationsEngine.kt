package com.example.moneybuddy2.core.recommendation

import com.example.moneybuddy2.data.model.BudgetPlan
import com.example.moneybuddy2.data.model.Expense
import com.example.moneybuddy2.data.model.RecommendationCard
import com.example.moneybuddy2.data.model.SourceRef
import com.example.moneybuddy2.data.model.SpendSummary
import com.example.moneybuddy2.data.model.UserProfile
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import kotlin.math.max
import kotlin.math.roundToInt

class RecommendationsEngine(
    private val zoneId: ZoneId = ZoneId.systemDefault()
) {
    private val src503020 = SourceRef(
        label = "KWSP/EPF - 50/30/20 budgeting rule (general guidance)",
        url = "https://www.kwsp.gov.my/en/w/article/50-30-20-rule"
    )
    private val srcEmergencyFund = SourceRef(
        label = "PIDM - Emergency funds guidance",
        url = "https://www.pidm.gov.my/my/general/info-corner/editorials/article/how-to-start-an-emergency-fund"
    )
    private val srcFixedDeposit = SourceRef(
        label = "HLB - Fixed deposit basics",
        url = "https://www.hlb.com.my/en/personal-banking/blog/hlb-a-guide-to-fixed-deposit-in-malaysia-and-how-they-work.html"
    )
    private val srcFdReference = SourceRef(
        label = "FD rate reference",
        url = "https://ringgitplus.com/en/fixed-deposit/"
    )

    fun buildBudgetPlan(profile: UserProfile): BudgetPlan {
        val income = max(0.0, profile.monthlyIncome)
        val needs = income * 0.50
        val wants = income * 0.30
        val savings = income * 0.20

        return BudgetPlan(
            income = income,
            needsLimit = needs,
            savingsLimit = savings,
            wantsLimit = wants
        )
    }

    fun summarizeSpending(expenses: List<Expense>, periodLabel: String = "This month"): SpendSummary {
        val total = expenses.sumOf{ it.amount.coerceAtLeast(0.0)}

        val byCategory = expenses
            .groupBy { it.category.ifBlank { "Other" }}
            .mapValues { (_,list) -> list.sumOf { it.amount.coerceAtLeast(0.0) } }

        val top = byCategory.entries
            .sortedByDescending{ it.value }
            .take(5)
            .map {it.key to it.value}

        return SpendSummary(
            periodLabel = periodLabel,
            totalSpent = total,
            byCategory = byCategory,
            topCategories = top
        )
    }

    fun generate(
        profile: UserProfile,
        monthExpense: List<Expense>
    ): Pair<BudgetPlan, List<RecommendationCard>> {
        val plan = buildBudgetPlan(profile)
        val spend = summarizeSpending(monthExpense)
        val cards = mutableListOf<RecommendationCard>()

        //budget overview
        cards += RecommendationCard(
            id = "budget_plan",
            title = "Suggested monthly budget plan (${plan.ruleLabel})",
            message =
                "Based on your monthly income of ${fmt(plan.income, profile.currency)}, a starting plan is:\n" +
                        "• Needs (≤ 50%): ${fmt(plan.needsLimit, profile.currency)}\n" +
                        "• Wants (≤ 30%): ${fmt(plan.wantsLimit, profile.currency)}\n" +
                        "• Savings/Debt (≥ 20%): ${fmt(plan.savingsLimit, profile.currency)}\n\n" +
                        "This is a general heuristic and may be adjusted to your actual fixed commitments.",
            priority = 2,
            sources = listOf(src503020)
        )

        //overspending detection
        if (plan.income > 0.0) {
            val overspend = spend.totalSpent - plan.income
            if (overspend > 0.0) {
                cards += RecommendationCard(
                    id = "overspending_total",
                    title = "Spending exceeds income",
                    message =
                        "Your spending for ${spend.periodLabel} is ${fmt(spend.totalSpent, profile.currency)}, " +
                                "which is above your monthly income (${fmt(plan.income, profile.currency)}) by " +
                                "${fmt(overspend, profile.currency)}.\n\n" +
                                "Consider prioritising essential expenses and reducing discretionary categories. " +
                                "Top categories: ${spend.topCategories.joinToString { "${it.first} (${fmt(it.second, profile.currency)})" }}.",
                    priority = 1,
                    sources = emptyList()
                )
            }
        }

        //wants control
        val wantsCategories = setOf("Food", "Dining", "Shopping", "Entertainment", "Transport", "Others", "Other")
        val wantsSpend = spend.byCategory
            .filterKeys { wantsCategories.contains(it) }
            .values.sum()

        if (wantsSpend > plan.wantsLimit && plan.income > 0.0) {
            val reduce = wantsSpend - plan.wantsLimit
            val perWeek = reduce / 4.0
            cards += RecommendationCard(
                id = "wants_reduce",
                title = "Reduce discretionary spending",
                message =
                    "Your estimated discretionary spending is ${fmt(wantsSpend, profile.currency)}, above the suggested wants limit " +
                            "(${fmt(plan.wantsLimit, profile.currency)}) by ${fmt(reduce, profile.currency)}.\n\n" +
                            "A practical target is to reduce discretionary spending by about ${fmt(perWeek, profile.currency)} per week.",
                priority = 1,
                sources = listOf(src503020)
            )
        }

        //saving goal feasability
        val goal = profile.savingGoal.coerceAtLeast(0.0)
        val months = monthsUntil(profile.savingGoalTargetDate)
        if (goal > 0.0) {
            val requiredPerMonth = goal / months
            val gap = requiredPerMonth - plan.savingsLimit

            if (gap <= 0.0) {
                cards += RecommendationCard(
                    id = "goal_on_track",
                    title = "Saving goal looks feasible",
                    message =
                        "To reach your saving goal of ${fmt(goal, profile.currency)} in about $months months, " +
                                "a simple average is ${fmt(requiredPerMonth, profile.currency)} per month. " +
                                "This is within the suggested savings allocation (${fmt(plan.savingsLimit, profile.currency)}).",
                    priority = 3,
                    sources = emptyList()
                )
            } else {
                cards += RecommendationCard(
                    id = "goal_gap",
                    title = "Adjust plan to meet your saving goal",
                    message =
                        "To reach ${fmt(goal, profile.currency)} in about $months months, the average required saving is " +
                                "${fmt(requiredPerMonth, profile.currency)} per month.\n" +
                                "Your suggested savings allocation is ${fmt(plan.savingsLimit, profile.currency)}, leaving a gap of " +
                                "${fmt(gap, profile.currency)}.\n\n" +
                                "Options: (1) reduce discretionary spending by ${fmt(gap, profile.currency)} per month, " +
                                "(2) extend your goal timeline, or (3) increase income.\n\n" +
                                "For personalised planning, consider booking a consultation.",
                    priority = 2,
                    sources = listOf(src503020)
                )
            }
        }

        //emergency fund guidance
        val essentialMonthly = plan.needsLimit
        if (essentialMonthly > 0.0) {
            val targetMonths = 3 // you can ask user "stable income?" to decide 3 vs 6
            val targetAmount = essentialMonthly * targetMonths

            cards += RecommendationCard(
                id = "emergency_fund",
                title = "Build an emergency fund (3–6 months)",
                message =
                    "A common guideline is to keep an emergency fund covering about 3–6 months of essential expenses. " +
                            "Using your estimated essential budget (${fmt(essentialMonthly, profile.currency)} per month), " +
                            "a 3-month target is approximately ${fmt(targetAmount, profile.currency)}.\n\n" +
                            "If your income is variable or you have dependents, consider aiming closer to 6 months.",
                priority = 2,
                sources = listOf(srcEmergencyFund)
            )
        }

        //fd suggestion
        cards += RecommendationCard(
            id = "fixed_deposit_option",
            title = "Option: Fixed deposit for short-term savings",
            message =
                "For short-term savings where capital preservation matters, a fixed deposit (FD) may be an option " +
                        "because returns are typically predictable over a chosen tenure. " +
                        "Rates vary by bank and time; verify current rates before deciding.",
            priority = 4,
            sources = listOf(srcFixedDeposit, srcFdReference)
        )

        // Sort by priority
        return plan to cards.sortedBy { it.priority }
    }

    private fun monthsUntil(targetMillis: Long?): Int {
        if (targetMillis == null) return 12 // default if user hasn’t set a target date

        val nowYm = YearMonth.from(Instant.now().atZone(zoneId))
        val targetYm = YearMonth.from(Instant.ofEpochMilli(targetMillis).atZone(zoneId))

        val diff = (targetYm.year - nowYm.year) * 12 + (targetYm.monthValue - nowYm.monthValue)
        // If target is this month or past, treat as 1 month to avoid division by zero
        return max(1, diff)
    }

    private fun fmt(amount: Double, currency: String): String {
        val rounded = ((amount * 100.0).roundToInt() / 100.0)
        return "$currency ${"%.2f".format(rounded)}"
    }

}