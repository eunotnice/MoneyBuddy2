package com.example.moneybuddy2.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.moneybuddy2.data.repository.FaqRepository
import com.example.moneybuddy2.di.AppContainer

class ChatbotViewModelFactory (
    private val appContext: Context,
    private val container: AppContainer
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatbotViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return container.createCompanyBotViewModel(appContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}