package com.example.moneybuddy2.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class AiReceiptResult(
    val merchant: String,
    val amount: Double? = null,
    val dateIso: String? = null,
    val category: String? = null,
    val merchantCandidates: List<String>? = emptyList()
)

