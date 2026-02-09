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

        val col = FirestorePaths.expenseCol(uid)

        val snap = col
            .whereGreaterThanOrEqualTo("dateMillis", start)
            .whereLessThanOrEqualTo("dateMillis", end)
            .get()
            .await()

        var total = 0.0
        for (doc in snap.documents) {
            val v = doc.getDouble("co2eKg")
            if (v != null) total += v
        }
        return total
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






}