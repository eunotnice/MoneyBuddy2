package com.example.moneybuddy2.backend.data

data class IncomeAssessment(
    val totalIncome: Double?,
    val confidence: String, // known | estimated | missing
    val estimatedFrom: String? = null
)