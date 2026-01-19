package com.example.moneybuddy2.data.model

import com.google.firebase.Timestamp

data class UserProfile (
    val uid: String = "",
    val email: String? = "",
    val displayName: String = "",
    val monthlyIncome: Double = 0.0,
    val monthlyBudget: Double = 0.0,
    val savingGoal: Double = 0.0,
    val currency: String = "MYR",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)