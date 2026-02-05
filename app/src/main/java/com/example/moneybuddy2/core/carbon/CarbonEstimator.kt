package com.example.moneybuddy2.core.carbon

import com.example.moneybuddy2.data.model.CarbonActivity
import com.example.moneybuddy2.data.model.CarbonEstimate
import com.example.moneybuddy2.data.model.CarbonFactors

class CarbonEstimator(
    private val factors: CarbonFactors,
    private val petrolPriceRmPerLitre: Double,
    private val electricityRmPerKwh: Double? = null
) {

    fun estimate(
        merchant: String,
        amountRm: Double,
        category: String,
        description: String = ""
    ): CarbonEstimate? {

        if (amountRm <= 0) return null

        val cat = category.lowercase()
        val m = merchant.lowercase()
        val d = description.lowercase()

        /* =========================
         * Rule 1: Fuel / Transport
         * ========================= */
        val isFuel =
            cat.contains("transport") ||
                    cat.contains("fuel") ||
                    cat.contains("petrol")

        if (isFuel && petrolPriceRmPerLitre > 0) {
            val litres = amountRm / petrolPriceRmPerLitre
            val kg = litres * factors.petrolKgPerLitre

            return CarbonEstimate(
                kgCo2e = kg,
                ruleId = "FUEL_RM_TO_LITRE",
                factorVersion = factors.version,
                assumptions = mapOf(
                    "petrolPriceRmPerLitre" to petrolPriceRmPerLitre,
                    "petrolKgPerLitre" to factors.petrolKgPerLitre
                )
            )
        }

        /* =========================
         * Rule 2: Electricity bill
         * ========================= */
        val looksLikeElectricity =
            cat.contains("utilities") ||
                    cat.contains("electric") ||
                    m.contains("tnb") ||
                    d.contains("electric")

        if (looksLikeElectricity && electricityRmPerKwh != null && electricityRmPerKwh > 0) {
            val kwh = amountRm / electricityRmPerKwh
            val kg = kwh * factors.electricityKgPerKwh

            return CarbonEstimate(
                kgCo2e = kg,
                ruleId = "ELECTRICITY_RM_TO_KWH",
                factorVersion = factors.version,
                assumptions = mapOf(
                    "electricityRmPerKwh" to electricityRmPerKwh,
                    "electricityKgPerKwh" to factors.electricityKgPerKwh
                )
            )
        }

        // Future rules go here (ride-hailing, flights, etc.)

        return null
    }
}