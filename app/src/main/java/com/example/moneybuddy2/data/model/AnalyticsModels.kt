package com.example.moneybuddy2.data.model

import androidx.compose.ui.graphics.Color


data class CategorySlice(
    val category: String,
    val amount: Double,
    val percent: Double,
    val color: Color
)