package com.example.moneybuddy2.data.model

data class Income (
    val id: String = "",
    val amount: Double = 0.0,
    val category: String = "Other",
    val description: String = "",
    val dateMillis: Long = System.currentTimeMillis()
)