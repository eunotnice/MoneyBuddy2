package com.example.moneybuddy2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneybuddy2.core.chat.ChatPrompts
import com.example.moneybuddy2.core.chat.IntentClassifier
import com.example.moneybuddy2.data.remote.GeminiChatService
import com.example.moneybuddy2.data.repository.MoneyRepository
import com.example.moneybuddy2.data.repository.InsightsFactsBuilder
import com.example.moneybuddy2.data.model.ChatIntent
import com.example.moneybuddy2.data.model.ChatMessage
import com.example.moneybuddy2.data.model.ChatUiState
import com.example.moneybuddy2.data.model.Role
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.moneybuddy2.data.remote.FirebaseProvider
import com.example.moneybuddy2.data.remote.GeminiChatResult
import com.example.moneybuddy2.data.repository.ChatRepository

class ChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(ChatUiState())
    val ui: StateFlow<ChatUiState> = _ui

    fun send(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty()) return

        _ui.value = _ui.value.copy(
            messages = _ui.value.messages + ChatMessage(Role.USER, trimmed),
            sending = true,
            error = null
        )

        viewModelScope.launch {
            try {
                val intent = IntentClassifier.classify(trimmed)

                val replyText = when (intent) {
                    ChatIntent.PERSONAL_ADVICE_REQUEST -> {
                        ChatPrompts.PERSONAL_ADVICE_RESPONSE
                    }

                    ChatIntent.FIN_LITERACY_GENERAL,
                    ChatIntent.UNKNOWN,
                    ChatIntent.APP_INSIGHTS -> {
                        withContext(Dispatchers.IO) {
                            chatRepository.sendChatMessage(trimmed)
                        }
                    }
                }

                _ui.value = _ui.value.copy(
                    messages = _ui.value.messages + ChatMessage(Role.ASSISTANT, replyText),
                    sending = false,
                    error = null
                )

            } catch (e: Exception) {
                _ui.value = _ui.value.copy(
                    sending = false,
                    error = e.message ?: "Chat failed"
                )
            }
        }
    }
}
//class ChatViewModel(
//    private val chatRepository: ChatRepository
//) : ViewModel() {
//
//    private val _ui = MutableStateFlow(ChatUiState())
//    val ui: StateFlow<ChatUiState> = _ui
//
//    fun send(userText: String) {
//        val trimmed = userText.trim()
//        if (trimmed.isEmpty()) return
//
//        _ui.value = _ui.value.copy(
//            messages = _ui.value.messages + ChatMessage(Role.USER, trimmed),
//            sending = true,
//            error = null
//        )
//
//        viewModelScope.launch {
//            try {
//                val intent = IntentClassifier.classify(trimmed)
//
//                val replyText = when (intent) {
//                    ChatIntent.PERSONAL_ADVICE_REQUEST -> {
//                        ChatPrompts.PERSONAL_ADVICE_RESPONSE
//                    }
//
//                    ChatIntent.FIN_LITERACY_GENERAL,
//                    ChatIntent.UNKNOWN,
//                    ChatIntent.APP_INSIGHTS -> {
//                        chatRepository.sendChatMessage(trimmed)
//                    }
//                }
//
//                _ui.value = _ui.value.copy(
//                    messages = _ui.value.messages + ChatMessage(Role.ASSISTANT, replyText),
//                    sending = false
//                )
//
//            } catch (e: Exception) {
//                _ui.value = _ui.value.copy(
//                    sending = false,
//                    error = e.message ?: "Chat failed"
//                )
//            }
//        }
//    }
//}