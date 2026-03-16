package com.example.moneybuddy2.data.model

data class IncomeAssessment(
    val totalIncome: Double?,
    val confidence: String,
    val estimatedFrom: String? = null
)