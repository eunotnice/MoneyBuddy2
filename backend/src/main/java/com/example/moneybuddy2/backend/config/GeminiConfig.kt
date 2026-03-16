package com.example.moneybuddy2.backend.config

object GeminiConfig {
    val apiKey: String = System.getenv("GEMINI_API_KEY") ?: error("Missing GEMINI_API_KEY")
    val model: String = "gemini-2.5-flash"
    val fileSearchStore: String = System.getenv("GEMINI_FILE_SEARCH_STORE")
        ?: error("Missing GEMINI_FILE_SEARCH_STORE")
}