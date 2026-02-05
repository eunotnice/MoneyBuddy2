package com.example.moneybuddy2.data.model

data class CarbonActivity(
    val month: String,
    val electricityKwh: Double,
    val fuelLitres: Double
)

data class CarbonEstimate(
    val kgCo2e: Double,
    val ruleId: String,
    val factorVersion: String,
    val assumptions: Map<String, Any> = emptyMap()
)

data class CarbonFactors(
    val version: String,
    val electricityKgPerKwh: Double,
    val petrolKgPerLitre: Double
)

data class CarbonReport(
    val month: String,
    val totalKgCo2e: Double,
    val breakdown: Map<String, Double>,
    val factorsVersion: String
)