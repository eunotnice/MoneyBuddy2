package com.example.moneybuddy2.backend.data

import kotlinx.serialization.Serializable

@Serializable
data class AiSavingRecommendation(
    val title: String,
    val message: String,
    val priority: Int,
    val category: String
)