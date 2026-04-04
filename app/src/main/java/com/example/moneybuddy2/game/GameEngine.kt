package com.example.moneybuddy2.game

import kotlin.math.pow

object SimulationEngine {

    fun simulate(input: SimulationInput): SimulationResult? {
        if (!input.isValid) return null

        val activeAllocations = input.allocations.filter { it.isAllocated }

        val results = activeAllocations.map { alloc ->
            val amountInvested = input.principalAmount * alloc.allocationPercent / 100.0
            val rate = alloc.type.annualReturnRate

            // Compound interest: FV = PV × (1 + r)^n
            val futureValue = amountInvested * (1 + rate).pow(input.years)
            val totalGain   = futureValue - amountInvested
            val gainPercent = totalGain / amountInvested * 100

            // Year-by-year values for the line chart
            val yearlyValues = (0..input.years).map { year ->
                amountInvested * (1 + rate).pow(year)
            }

            InvestmentResult(
                type              = alloc.type,
                allocationPercent = alloc.allocationPercent,
                amountInvested    = amountInvested,
                futureValue       = futureValue,
                totalGain         = totalGain,
                gainPercent       = gainPercent,
                yearlyValues      = yearlyValues
            )
        }

        val grandTotal     = results.sumOf { it.futureValue }
        val totalGain      = grandTotal - input.principalAmount
        val overallGainPct = totalGain / input.principalAmount * 100

        return SimulationResult(
            input              = input,
            results            = results.sortedByDescending { it.futureValue },
            grandTotal         = grandTotal,
            totalGain          = totalGain,
            overallGainPercent = overallGainPct
        )
    }

    /** Build insight messages to show the user after simulation. */
    fun generateInsights(result: SimulationResult): List<String> {
        val insights = mutableListOf<String>()
        val best = result.bestPerformer
        val fmt  = { v: Double -> "RM %,.0f".format(v) }

        insights += "Best performer: ${best.type.displayName} grew " +
                "${fmt(best.amountInvested)} → ${fmt(best.futureValue)} " +
                "(+${"%.0f".format(best.gainPercent)}%)"

        insights += "Total gain of ${fmt(result.totalGain)} over ${result.input.years} " +
                "year${if (result.input.years > 1) "s" else ""} " +
                "(${"%.1f".format(result.overallGainPercent)}% overall return)"

        if (result.hasSafeAndGrowth)
            insights += "You balanced safety and growth — a smart diversification strategy."

        if (result.isDiversified)
            insights += "Spreading across ${result.results.size} types reduces the impact of any single loss."

        if (result.results.any { it.type == InvestmentType.FIXED_DEPOSIT })
            insights += "Fixed deposit acts as your safety net — it always grows, slowly but surely."

        if (result.results.any { it.type == InvestmentType.STOCKS } && result.input.years >= 10)
            insights += "Stocks tend to outperform over long horizons like ${result.input.years} years despite short-term swings."

        return insights
    }
}