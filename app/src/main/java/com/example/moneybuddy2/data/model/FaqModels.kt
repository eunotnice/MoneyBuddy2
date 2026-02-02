package com.example.moneybuddy2.data.model

data class FaqItem(
    val id: String,
    val category: String,
    val question: String,
    val keywords: List<String>,
    val answer: String
)