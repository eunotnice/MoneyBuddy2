package com.example.moneybuddy2.data.model

data class Expense (
    val id: String = "",
    val merchant: String = "",
    val amount: Double = 0.0,
    val category: String = "Other",
    val description: String = "",
    val source: String = "Manual",
    val dataMillis: Long = System.currentTimeMillis(),
    val rawText: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)