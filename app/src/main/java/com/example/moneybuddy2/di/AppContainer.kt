package com.example.moneybuddy2.di

import com.example.moneybuddy2.core.recommendation.Recommendation
import com.example.moneybuddy2.data.repository.MoneyRepository
import com.example.moneybuddy2.data.repository.MoneyRepositoryImpl
import com.example.moneybuddy2.ui.viewmodel.OcrViewModel
import com.example.moneybuddy2.ui.viewmodel.ChatViewModel
import com.example.moneybuddy2.ui.viewmodel.RecommendationViewModel

class AppContainer {
    val repository: MoneyRepository = MoneyRepositoryImpl()
    // Shared OCR state between pick + confirm screens
    val ocrViewModel: OcrViewModel by lazy { OcrViewModel(repository) }

    val chatViewModel: ChatViewModel by lazy { ChatViewModel(repository) }
    val recommendationViewModel: RecommendationViewModel by lazy { RecommendationViewModel(repository) }
}