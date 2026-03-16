package com.example.moneybuddy2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.moneybuddy2.data.repository.AuthRepository
import com.example.moneybuddy2.data.repository.ChatRepository
import com.example.moneybuddy2.ui.viewmodel.ChatViewModel

class ChatViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val authRepository = AuthRepository()
        val chatRepository = ChatRepository(authRepository)
        return ChatViewModel(chatRepository) as T
    }
}