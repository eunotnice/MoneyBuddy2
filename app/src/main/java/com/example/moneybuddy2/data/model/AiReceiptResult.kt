package com.example.moneybuddy2.data.model

data class AiReceiptResult(
    val merchant: String,
    val amount: Double? = null,
    val dateIso: String? = null,
    val merchantCandidates: List<String>? = emptyList(),
    val category: String,
    val categoryConfidence: Double,
    val categoryReason: String
)
