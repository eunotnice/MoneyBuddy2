package com.example.moneybuddy2.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.moneybuddy2.core.carbon.CarbonCalculator
import com.example.moneybuddy2.core.carbon.EmissionFactors
import com.example.moneybuddy2.data.model.CarbonActivity
import com.example.moneybuddy2.data.model.CarbonReport
import com.example.moneybuddy2.data.repository.MoneyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class  CarbonViewModel (
    private val repo: MoneyRepository,
    private val calc: CarbonCalculator,
    private val factors: EmissionFactors
): ViewModel() {

    private val _report = MutableStateFlow<CarbonReport?>(null)
    val report: StateFlow<CarbonReport?> = _report

    fun refresh(month: String, electricityKwh: Double, fuelLitres: Double) {
        val activity = CarbonActivity(month, electricityKwh, fuelLitres)
        _report.value = calc.calculate(activity, factors)
    }
}