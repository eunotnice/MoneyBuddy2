package com.example.moneybuddy2.core.carbon

data class EmissionFactors (
    val version: String,
    val electricityKgPerKwh: Double,
    val petrolKgPerLitre: Double
)

