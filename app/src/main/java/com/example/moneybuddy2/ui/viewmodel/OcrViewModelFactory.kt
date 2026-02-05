package com.example.moneybuddy2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.moneybuddy2.core.carbon.CarbonEstimator
import com.example.moneybuddy2.data.repository.MoneyRepository

class OcrViewModelFactory(
    private val repo: MoneyRepository,
    private val carbonEstimator: CarbonEstimator
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OcrViewModel::class.java)) {
            return OcrViewModel(repo, carbonEstimator) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

