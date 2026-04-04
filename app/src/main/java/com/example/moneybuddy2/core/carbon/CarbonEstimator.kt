package com.example.moneybuddy2.core.carbon

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

        val cat = category.lowercase().trim()
        val m   = merchant.lowercase().trim()
        val d   = description.lowercase().trim()

        // ── Rule 1: Fuel / Petrol ─────────────────────────────────────────
        // Physics-based: RM → litres → kgCO₂e
        // Source: IPCC AR6, petrol combustion ~2.31 kgCO₂e/litre
        val isFuel = cat.containsAny("transport", "fuel", "petrol") ||
                m.containsAny("petronas", "shell", "petron", "bpetrol", "caltex")

        if (isFuel && petrolPriceRmPerLitre > 0) {
            val litres = amountRm / petrolPriceRmPerLitre
            val kg     = litres * factors.petrolKgPerLitre
            return CarbonEstimate(
                kgCo2e        = kg,
                ruleId        = "FUEL_RM_TO_LITRE",
                factorVersion = factors.version,
                assumptions   = mapOf(
                    "petrolPriceRmPerLitre" to petrolPriceRmPerLitre,
                    "petrolKgPerLitre"      to factors.petrolKgPerLitre,
                    "note"                  to "Physics-based via spend-to-litres conversion"
                )
            )
        }

        // ── Rule 2: Electricity ───────────────────────────────────────────
        // Physics-based: RM → kWh → kgCO₂e
        // Source: Malaysia grid emission factor ~0.585 kgCO₂e/kWh (ST 2022)
        val isElectricity = cat.containsAny("bills", "electric", "utility") ||
                m.containsAny("tnb", "tenaga") ||
                d.containsAny("electric", "utility", "bill")

        if (isElectricity && electricityRmPerKwh != null && electricityRmPerKwh > 0) {
            val kwh = amountRm / electricityRmPerKwh
            val kg  = kwh * factors.electricityKgPerKwh
            return CarbonEstimate(
                kgCo2e        = kg,
                ruleId        = "ELECTRICITY_RM_TO_KWH",
                factorVersion = factors.version,
                assumptions   = mapOf(
                    "electricityRmPerKwh" to electricityRmPerKwh,
                    "electricityKgPerKwh" to factors.electricityKgPerKwh,
                    "note"                to "Malaysia grid factor, Suruhanjaya Tenaga 2022"
                )
            )
        }

        // ── Rule 3: Ride-hailing ──────────────────────────────────────────
        // Spend-based: RM → estimated km → kgCO₂e
        // Grab/MyCar avg fare ~RM1.50–2.00/km, car emission ~0.21 kgCO₂e/km
        val isRideHail = m.containsAny("grab", "mycar", "maxim", "indriver") ||
                d.containsAny("grab", "ride", "taxi", "e-hailing")

        if (isRideHail) {
            val estKm = amountRm / 1.75          // midpoint RM/km estimate
            val kg    = estKm * 0.171             // passenger car avg, DEFRA 2023
            return CarbonEstimate(
                kgCo2e        = kg,
                ruleId        = "RIDE_HAIL_SPEND_TO_KM",
                factorVersion = factors.version,
                assumptions   = mapOf(
                    "rmPerKm"        to 1.75,
                    "kgCo2ePerKm"    to 0.171,
                    "note"           to "Spend-based estimate. DEFRA 2023 passenger car avg"
                )
            )
        }

        // ── Rule 4: Food & dining ─────────────────────────────────────────
        // Spend-based: RM → kgCO₂e via Malaysian food emission intensity
        // Source: Eco2 Malaysia study, mixed diet ~3.3 kgCO₂e per RM100
        val isFood = cat.containsAny("food", "dining", "restaurant", "cafe")

        if (isFood) {
            val kg = amountRm * 0.033
            return CarbonEstimate(
                kgCo2e        = kg,
                ruleId        = "FOOD_SPEND_INTENSITY",
                factorVersion = factors.version,
                assumptions   = mapOf(
                    "kgCo2ePerRm" to 0.033,
                    "note"        to "Spend-based. Mixed Malaysian diet average"
                )
            )
        }

        // ── Rule 5: Groceries ─────────────────────────────────────────────
        // Slightly lower than dining (less cooking energy overhead per RM)
        val isGroceries = cat.containsAny("groceries", "supermarket", "market") ||
                m.containsAny("mydin", "giant", "tesco", "aeon", "jaya grocer",
                    "village grocer", "99 speedmart", "kk mart")

        if (isGroceries) {
            val kg = amountRm * 0.025
            return CarbonEstimate(
                kgCo2e        = kg,
                ruleId        = "GROCERIES_SPEND_INTENSITY",
                factorVersion = factors.version,
                assumptions   = mapOf(
                    "kgCo2ePerRm" to 0.025,
                    "note"        to "Spend-based. EXIOBASE supply chain factor, MY-adjusted"
                )
            )
        }

        // ── Rule 6: Shopping / retail ─────────────────────────────────────
        // Source: EXIOBASE 3, retail goods avg ~0.4 kgCO₂e per USD,
        // converted to RM (~0.09 kgCO₂e per RM at PPP)
        val isShopping = cat.containsAny("shopping", "clothing", "electronics", "retail")

        if (isShopping) {
            val kg = amountRm * 0.018
            return CarbonEstimate(
                kgCo2e        = kg,
                ruleId        = "SHOPPING_SPEND_INTENSITY",
                factorVersion = factors.version,
                assumptions   = mapOf(
                    "kgCo2ePerRm" to 0.018,
                    "note"        to "Spend-based. EXIOBASE retail avg, PPP-adjusted to MYR"
                )
            )
        }

        // ── Rule 7: Entertainment / leisure ──────────────────────────────
        val isEntertainment = cat.containsAny("entertainment", "leisure", "recreation")

        if (isEntertainment) {
            val kg = amountRm * 0.010
            return CarbonEstimate(
                kgCo2e        = kg,
                ruleId        = "ENTERTAINMENT_SPEND_INTENSITY",
                factorVersion = factors.version,
                assumptions   = mapOf(
                    "kgCo2ePerRm" to 0.010,
                    "note"        to "Spend-based. Service sector average"
                )
            )
        }

        // ── Rule 8: Generic fallback ──────────────────────────────────────
        // Returns a low estimate rather than null so no expense is invisible
        // Uses conservative service-sector average
        val kg = amountRm * 0.008
        return CarbonEstimate(
            kgCo2e        = kg,
            ruleId        = "GENERIC_SPEND_FALLBACK",
            factorVersion = factors.version,
            assumptions   = mapOf(
                "kgCo2ePerRm" to 0.008,
                "note"        to "Generic fallback. Conservative service-sector estimate"
            )
        )
    }

    // ── Extension helper ──────────────────────────────────────────────────────
    private fun String.containsAny(vararg keywords: String) =
        keywords.any { this.contains(it) }
}