package com.example.moneybuddy2.data.model

data class Expense (
    val id: String = "",
    val merchant: String = "",
    val amount: Double = 0.0,
    val category: String = "Other",
    val description: String = "",
    val source: String = "Manual",
    val dateMillis: Long = System.currentTimeMillis(),
    val rawText: String? = null,
    val createdAt: Long = System.currentTimeMillis(),

    val receiptImageUrl: String? = null,
    val receiptStoragePath: String? = null,
    //carbon emissiont tracker
    val co2eKg: Double? = null,
    val co2eRuleId: String = "none",
    val co2eFactorVersion: String = "unknown",
    val co2eAssumptions: Map<String, Any> = emptyMap()
)