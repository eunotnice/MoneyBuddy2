package com.example.moneybuddy2.data.repository

import com.example.moneybuddy2.data.model.Expense
import com.example.moneybuddy2.data.model.Income
import com.example.moneybuddy2.data.model.UserProfile

interface MoneyRepository {
    suspend fun ensureUserProfile(uid: String, email: String?): Boolean
    suspend fun saveUserProfile(profile: UserProfile): Boolean
    suspend fun addIncome (uid: String, income: Income): Boolean
    suspend fun addExpense(uid: String, expense: Expense): Boolean
    suspend fun listLatestExpenses(uid: String, limit: Int = 20): List<Expense>
    suspend fun getExpenseById(uid: String, expenseId: String): Expense?
    suspend fun getIncomeById (uid: String, incomeId: String): Income?
    suspend fun updateExpense(uid: String, expense: Expense): Boolean
    suspend fun deleteExpense(uid: String, expenseId: String): Boolean

    suspend fun updateIncome(uid: String, income: Income): Boolean
    suspend fun deleteIncome(uid: String, incomeId: String): Boolean

    suspend fun getUserProfile(uid: String): UserProfile?

    suspend fun listIncomeInRange(uid: String, startMillis: Long, endMillis: Long): List<Income>
    suspend fun listExpensesInRange(uid: String, startMillis: Long, endMillis: Long): List<Expense>
    suspend fun listExpensesInRangeExclusive(uid: String, startMillis: Long, endExclusiveMillis: Long): List<Expense>

    suspend fun getMonthlyCarbonTotalKg(uid: String): Double
    suspend fun getCarbonTotalKgInRange (uid:String, startMillis: Long, endMillis: Long): Double


}
