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
    val version: String = "MY-2024-v1",

    // Physics-based — well sourced
    val petrolKgPerLitre: Double = 2.31,        // IPCC AR6 WG3, petrol combustion
    val electricityKgPerKwh: Double = 0.585,    // Suruhanjaya Tenaga Malaysia 2022

    // Spend-based intensity factors (kgCO₂e per RM)
    // Source notes stored in CarbonEstimate.assumptions per estimate
    val foodKgPerRm: Double = 0.033,
    val groceriesKgPerRm: Double = 0.025,
    val shoppingKgPerRm: Double = 0.018,
    val entertainmentKgPerRm: Double = 0.010,
    val genericFallbackKgPerRm: Double = 0.008,

    // Transport
    val rideHailKgPerKm: Double = 0.171,        // DEFRA 2023 passenger car avg
    val rideHailRmPerKm: Double = 1.75          // Grab/MyCar midpoint estimate
)

data class CarbonReport(
    val month: String,
    val totalKgCo2e: Double,
    val breakdown: Map<String, Double>,
    val factorsVersion: String
)