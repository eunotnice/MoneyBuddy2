package com.example.moneybuddy2.game
enum class InvestmentType(
    val displayName: String,
    val annualReturnRate: Double,
    val riskLabel: String,
    val description: String,
    val colorHex: String
) {
    FIXED_DEPOSIT(
        displayName      = "Fixed Deposit",
        annualReturnRate = 0.038,
        riskLabel        = "Very low risk",
        description      = "Bank-guaranteed returns. Safe but modest growth.",
        colorHex         = "#7F77DD"
    ),
    BONDS(
        displayName      = "Bonds",
        annualReturnRate = 0.055,
        riskLabel        = "Low risk",
        description      = "Government or corporate debt. Steady income stream.",
        colorHex         = "#1D9E75"
    ),
    STOCKS(
        displayName      = "Stocks",
        annualReturnRate = 0.10,
        riskLabel        = "Higher risk",
        description      = "Equity ownership in companies. High reward potential.",
        colorHex         = "#D85A30"
    ),
    UNIT_TRUST(
        displayName      = "Unit Trust",
        annualReturnRate = 0.075,
        riskLabel        = "Moderate risk",
        description      = "Pooled fund managed by professionals.",
        colorHex         = "#BA7517"
    ),
    PROPERTY(
        displayName      = "Property",
        annualReturnRate = 0.065,
        riskLabel        = "Moderate risk",
        description      = "Real estate investment. Long-term capital appreciation.",
        colorHex         = "#185FA5"
    )
}

// ── User's allocation for one investment type ─────────────────────────────────

data class AllocationItem(
    val type: InvestmentType,
    val allocationPercent: Int = 0   // 0–100
) {
    val isAllocated: Boolean get() = allocationPercent > 0
}

// ── Inputs to the simulation ──────────────────────────────────────────────────

data class SimulationInput(
    val principalAmount: Double,
    val years: Int,
    val allocations: List<AllocationItem>
) {
    val totalAllocated: Int get() = allocations.sumOf { it.allocationPercent }
    val isValid: Boolean get() = totalAllocated == 100 && principalAmount > 0 && years > 0
}

// ── Result for one investment type ───────────────────────────────────────────

data class InvestmentResult(
    val type: InvestmentType,
    val allocationPercent: Int,
    val amountInvested: Double,
    val futureValue: Double,
    val totalGain: Double,
    val gainPercent: Double,
    val yearlyValues: List<Double>          // index 0 = year 0 (principal)
)

// ── Full simulation output ────────────────────────────────────────────────────

data class SimulationResult(
    val input: SimulationInput,
    val results: List<InvestmentResult>,
    val grandTotal: Double,
    val totalGain: Double,
    val overallGainPercent: Double
) {
    val bestPerformer: InvestmentResult get() = results.maxBy { it.futureValue }
    val isDiversified: Boolean get() = results.size >= 3
    val hasSafeAndGrowth: Boolean get() {
        val safe   = setOf(InvestmentType.FIXED_DEPOSIT, InvestmentType.BONDS)
        val growth = setOf(InvestmentType.STOCKS, InvestmentType.UNIT_TRUST, InvestmentType.PROPERTY)
        val types  = results.map { it.type }.toSet()
        return types.any { it in safe } && types.any { it in growth }
    }
}
