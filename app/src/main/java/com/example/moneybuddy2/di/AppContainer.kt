package com.example.moneybuddy2.di

import com.example.moneybuddy2.data.repository.MoneyRepository
import com.example.moneybuddy2.data.repository.MoneyRepositoryImpl
import com.example.moneybuddy2.ui.viewmodel.OcrViewModel

class AppContainer {
    val repository: MoneyRepository = MoneyRepositoryImpl()
    // Shared OCR state between pick + confirm screens
    val ocrViewModel: OcrViewModel by lazy { OcrViewModel(repository) }

}