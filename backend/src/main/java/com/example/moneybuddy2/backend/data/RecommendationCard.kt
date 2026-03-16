package com.example.moneybuddy2.backend.data

data class RecommendationCard(
    val id: String,
    val title: String,
    val message: String,
    val priority: Int,
    val sources: List<SourceRef>
)