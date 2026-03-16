package com.example.moneybuddy2.game

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SimulatorUiState(
    val principalAmount: Double = 10_000.0,
    val years: Int = 10,
    val allocations: List<AllocationItem> = InvestmentType.entries.map { AllocationItem(it) },
    val simulationResult: SimulationResult? = null,
    val insights: List<String> = emptyList(),
    val errorMessage: String? = null
) {
    val totalAllocated: Int  get() = allocations.sumOf { it.allocationPercent }
    val remainingPercent: Int get() = 100 - totalAllocated
    val allocationIsValid: Boolean get() = totalAllocated == 100
}

class SimulatorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SimulatorUiState())
    val uiState: StateFlow<SimulatorUiState> = _uiState.asStateFlow()

    fun setAmount(amount: Float) =
        _uiState.update { it.copy(principalAmount = amount.toDouble(), simulationResult = null) }

    fun setYears(years: Int) =
        _uiState.update { it.copy(years = years, simulationResult = null) }

    fun setAllocation(type: InvestmentType, percent: Int) {
        _uiState.update { state ->
            state.copy(
                allocations = state.allocations.map {
                    if (it.type == type) it.copy(allocationPercent = percent) else it
                },
                simulationResult = null,
                errorMessage     = null
            )
        }
    }

    fun simulate() {
        val state = _uiState.value
        if (!state.allocationIsValid) {
            _uiState.update {
                it.copy(errorMessage = "Allocations must total 100%. Currently at ${state.totalAllocated}%.")
            }
            return
        }
        val input = SimulationInput(
            principalAmount = state.principalAmount,
            years           = state.years,
            allocations     = state.allocations
        )
        val result = SimulationEngine.simulate(input) ?: return
        _uiState.update {
            it.copy(
                simulationResult = result,
                insights         = SimulationEngine.generateInsights(result),
                errorMessage     = null
            )
        }
    }

    fun reset() = _uiState.update {
        SimulatorUiState(principalAmount = it.principalAmount, years = it.years)
    }
}
