package com.example.moneybuddy2.backend.data

data class Income(
    val id: String = "",
    val amount: Double = 0.0,
    val category: String = "Other",
    val description: String = "",
    val dateMillis: Long = System.currentTimeMillis()
)