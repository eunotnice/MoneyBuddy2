package com.example.moneybuddy2.data.repository

import com.example.moneybuddy2.data.model.Expense
import com.example.moneybuddy2.data.model.UserProfile
import com.example.moneybuddy2.data.remote.FirestorePaths
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import android.util.Log
import com.example.moneybuddy2.core.carbon.CarbonEstimator
import com.example.moneybuddy2.core.util.DateUtils.endOfCurrentMonthMillis
import com.example.moneybuddy2.core.util.DateUtils.startOfCurrentMonthMillis
import com.example.moneybuddy2.data.model.BudgetBucket
import com.example.moneybuddy2.data.model.BudgetBucketSummary
import com.example.moneybuddy2.data.model.ExpenseCategory
import com.example.moneybuddy2.data.model.Income
import com.example.moneybuddy2.data.model.IncomeAssessment
import com.example.moneybuddy2.data.model.UserFinanceSnapshot

class MoneyRepositoryImpl  (
    private val carbonEstimator: CarbonEstimator
) : MoneyRepository{
    override suspend fun ensureUserProfile(uid: String, email: String?): Boolean {
        return try {
            val docRef = FirestorePaths.userDoc(uid)
            val snap = docRef.get().await()
            if (!snap.exists()){
                val profile = UserProfile(
                    uid = uid,
                    email = email,
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now()
                )
                docRef.set(profile).await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val snap = FirestorePaths.userDoc(uid).get().await()
            snap.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun saveUserProfile(profile: UserProfile): Boolean {
        return try {
            FirestorePaths.userDoc(profile.uid)
                .set(profile.copy(updatedAt = Timestamp.now()), SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
    override suspend fun addIncome(uid:String, income: Income): Boolean{
        return try{
            val col = FirestorePaths.incomeCol(uid)
            val doc = col.document()

            val enriched = income.copy(id = doc.id)
            doc.set(enriched).await()
            true
        } catch (e:Exception){
            false
        }
    }
    override suspend fun addExpense(uid: String, expense: Expense): Boolean {
        return try {
            val col = FirestorePaths.expenseCol(uid)
            val doc = col.document()

            val est = carbonEstimator.estimate(
                merchant = expense.merchant,
                amountRm = expense.amount,
                category = expense.category,
                description = expense.description
            )

            val enriched = expense.copy(
                id = doc.id,
                merchant = expense.merchant.trim(),
                description = expense.description.trim(),

                co2eKg = est?.kgCo2e,
                co2eRuleId = est?.ruleId ?: "none",
                co2eFactorVersion = est?.factorVersion ?: "unknown",
                co2eAssumptions = est?.assumptions ?: emptyMap()
            )

            doc.set(enriched).await()
            true
        } catch (e: Exception) {
            false
        }
    }


    override suspend fun listLatestExpenses(uid: String, limit: Int): List<Expense> {
        return try {
            val snap = FirestorePaths.expenseCol(uid)
                .orderBy("dateMillis", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snap.documents.mapNotNull { it.toObject(Expense::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun updateExpense(uid: String, expense: Expense): Boolean {
        return try {
            if (expense.id.isBlank()) return false

            val docRef = FirestorePaths.expenseCol(uid).document(expense.id)
            docRef.set(expense).await()  // overwrite doc with updated fields
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteExpense(uid: String, expenseId: String): Boolean {
        return try {
            if (expenseId.isBlank()) return false

            val docRef = FirestorePaths.expenseCol(uid).document(expenseId)
            docRef.delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun listIncomeInRange(
        uid: String,
        startMillis: Long,
        endMillis: Long
    ): List<Income> {
        return try {
            val snap = FirestorePaths.incomeCol(uid)
                .whereGreaterThanOrEqualTo("dateMillis", startMillis)
                .whereLessThanOrEqualTo("dateMillis", endMillis)
                .orderBy("dateMillis", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            snap.documents.mapNotNull { it.toObject(Income::class.java) }
        } catch (e: Exception) {
            Log.e("MoneyRepo", "listIncomeInRange failed", e)
            throw e
        }
    }

    override suspend fun listExpensesInRange(
        uid: String,
        startMillis: Long,
        endMillis: Long
    ): List<Expense> {
        return try {
            val snap = FirestorePaths.expenseCol(uid)
                .whereGreaterThanOrEqualTo("dateMillis", startMillis)
                .whereLessThanOrEqualTo("dateMillis", endMillis)
                .orderBy("dateMillis", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            snap.documents.mapNotNull { it.toObject(Expense::class.java) }
        } catch (e: Exception) {
            Log.e("MoneyRepo", "listExpensesInRange failed", e)
            throw e   // IMPORTANT: let ViewModel show the error
        }
    }

    override suspend fun getMonthlyCarbonTotalKg(uid: String): Double {
        val start = startOfCurrentMonthMillis()
        val end = endOfCurrentMonthMillis()
        return getCarbonTotalKgInRange(uid, start, end)
    }

    override suspend fun listExpensesInRangeExclusive(
        uid: String,
        startMillis: Long,
        endExclusiveMillis: Long
    ): List<Expense> {
        val col = FirestorePaths.expenseCol(uid)
        val snap = col
            .whereGreaterThanOrEqualTo("dateMillis", startMillis)
            .whereLessThan("dateMillis", endExclusiveMillis)   // ✅ exclusive end
            .get()
            .await()

        return snap.documents.mapNotNull { it.toObject(Expense::class.java) }
    }

    override suspend fun getCarbonTotalKgInRange(
        uid: String,
        startMillis: Long,
        endMillis: Long
    ): Double {
        val col = FirestorePaths.expenseCol(uid)

        val snap = col
            .whereGreaterThanOrEqualTo("dateMillis", startMillis)
            .whereLessThanOrEqualTo("dateMillis", endMillis)
            .get()
            .await()

        var total = 0.0
        for (doc in snap.documents) {
            val v = doc.getDouble("co2eKg")
            if (v != null) total += v
        }
        return total
    }

    private fun categoryToBudgetBucket(category: String): BudgetBucket {
        return when (category) {
            ExpenseCategory.FOOD_DRINK.wire -> BudgetBucket.NEEDS
            ExpenseCategory.GROCERIES.wire -> BudgetBucket.NEEDS
            ExpenseCategory.TRANSPORT.wire -> BudgetBucket.NEEDS
            ExpenseCategory.UTILITIES.wire -> BudgetBucket.NEEDS
            ExpenseCategory.HEALTH.wire -> BudgetBucket.NEEDS
            ExpenseCategory.EDUCATION.wire -> BudgetBucket.NEEDS
            ExpenseCategory.SERVICES.wire -> BudgetBucket.NEEDS

            ExpenseCategory.SHOPPING.wire -> BudgetBucket.WANTS
            ExpenseCategory.ENTERTAINMENT.wire -> BudgetBucket.WANTS
            ExpenseCategory.TRAVEL.wire -> BudgetBucket.WANTS

            else -> BudgetBucket.UNCATEGORISED
        }
    }

    private fun computeBudgetBucketSummary(expenses: List<Expense>): BudgetBucketSummary {
        var needs = 0.0
        var wants = 0.0
        var uncategorised = 0.0

        for (expense in expenses) {
            when (categoryToBudgetBucket(expense.category)) {
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

    private fun median(values: List<Double>): Double? {
        if (values.isEmpty()) return null
        val sorted = values.sorted()
        val mid = sorted.size / 2

        return if (sorted.size % 2 == 0) {
            (sorted[mid - 1] + sorted[mid]) / 2.0
        } else {
            sorted[mid]
        }
    }

    suspend fun assessIncome(
        uid: String,
        periodStartMillis: Long,
        periodEndMillis: Long
    ): IncomeAssessment {
        val currentIncomes = listIncomeInRange(uid, periodStartMillis, periodEndMillis)
        val currentTotal = currentIncomes.sumOf { it.amount }

        if (currentTotal > 0.0) {
            return IncomeAssessment(
                totalIncome = currentTotal,
                confidence = "known",
                estimatedFrom = "current_period"
            )
        }

        val ninetyDaysMillis = 90L * 24L * 60L * 60L * 1000L
        val historicStart = periodStartMillis - ninetyDaysMillis

        val historicIncomes = listIncomeInRange(uid, historicStart, periodEndMillis)

        if (historicIncomes.isNotEmpty()) {
            val monthlyTotals = historicIncomes
                .groupBy { income ->
                    val cal = java.util.Calendar.getInstance()
                    cal.timeInMillis = income.dateMillis
                    "${cal.get(java.util.Calendar.YEAR)}-${cal.get(java.util.Calendar.MONTH) + 1}"
                }
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

    suspend fun buildUserFinanceSnapshot(
        uid: String,
        periodDays: Int = 30,
        nowMillis: Long = System.currentTimeMillis()
    ): UserFinanceSnapshot {
        val periodStartMillis = nowMillis - periodDays * 24L * 60L * 60L * 1000L
        val periodEndMillis = nowMillis

        val expenses = listExpensesInRange(uid, periodStartMillis, periodEndMillis)
        val incomeAssessment = assessIncome(uid, periodStartMillis, periodEndMillis)

        val spendByCategory = expenses
            .groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.amount } }

        val topCategories = spendByCategory
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        val totalCo2eKg = expenses.sumOf { it.co2eKg ?: 0.0 }.takeIf { it > 0.0 }

        val co2eByCategory = expenses
            .groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.co2eKg ?: 0.0 } }

        return UserFinanceSnapshot(
            userId = uid,
            nowMillis = nowMillis,
            currency = "MYR",
            periodStartMillis = periodStartMillis,
            periodEndMillis = periodEndMillis,
            totalSpent = expenses.sumOf { it.amount },
            totalIncome = incomeAssessment.totalIncome,
            incomeCondifence = incomeAssessment.confidence,
            spendByCategory = spendByCategory,
            topSpendCategories = topCategories,
            totalCo2eKg = totalCo2eKg,
            co2eByCategory = co2eByCategory,
            treesEquivalent = null,
            treesFactorLabel = null
        )
    }
}