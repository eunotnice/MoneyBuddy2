package com.example.moneybuddy2.data.repository

import com.example.moneybuddy2.data.model.Expense
import com.example.moneybuddy2.data.model.UserProfile

interface MoneyRepository {
    suspend fun ensureUserProfile(uid: String, email: String?): Boolean
    suspend fun saveUserProfile(profile: UserProfile): Boolean

    suspend fun addExpense(uid: String, expense: Expense): Boolean
    suspend fun listLatestExpenses(uid: String, limit: Int = 20): List<Expense>

    suspend fun updateExpense(uid: String, expense: Expense): Boolean
    suspend fun deleteExpense(uid: String, expenseId: String): Boolean
}
