package com.example.moneybuddy2.core.carbon

import com.example.moneybuddy2.data.model.CarbonActivity
import com.example.moneybuddy2.data.model.CarbonReport

class CarbonCalculator {
    fun calculate(activity: CarbonActivity, factors: EmissionFactors): CarbonReport {
        val e = activity.electricityKwh * factors.electricityKgPerKwh
        val f = activity.fuelLitres * factors.petrolKgPerLitre
        val total = e + f

        return CarbonReport(
            month = activity.month,
            totalKgCo2e = total,
            breakdown = mapOf("Electricity" to e, "Fuel" to f),
            factorsVersion = factors.version
        )
    }
}