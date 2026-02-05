package com.example.moneybuddy2.core.carbon

import android.content.Context
import org.json.JSONObject

data class EmissionFactors (
    val version: String,
    val electricityKgPerKwh: Double,
    val petrolKgPerLitre: Double
)

fun loadEmissionFactors(context: Context): EmissionFactors {
    return runCatching {
        val json = context.assets.open("emission_factors.json")
            .bufferedReader().use { it.readText() }
        val obj = org.json.JSONObject(json)
        EmissionFactors(
            version = obj.getString("version"),
            electricityKgPerKwh = obj.getDouble("electricityKgPerKwh"),
            petrolKgPerLitre = obj.getDouble("petrolKgPerLitre")
        )
    }.getOrElse {
        // Provide safe defaults but label as unknown
        EmissionFactors(version = "unknown", electricityKgPerKwh = 0.0, petrolKgPerLitre = 0.0)
    }
}