package com.example.moneybuddy2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.moneybuddy2.data.repository.AuthRepository
import com.example.moneybuddy2.data.repository.AiRecommendationRepository

class RecommendationViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val authRepository = AuthRepository()
        val recommendationRepository = AiRecommendationRepository(authRepository)
        return AiRecommendationViewModel(recommendationRepository) as T
    }
}