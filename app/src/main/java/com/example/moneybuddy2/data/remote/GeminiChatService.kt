package com.example.moneybuddy2.data.remote

import com.example.moneybuddy2.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class GeminiChatService (
    private val client: OkHttpClient = OkHttpClient()
) {
    fun generateTextBlocking(system: String, user: String): GeminiChatResult {
        val apiKey = BuildConfig.GEMINI_API_KEY
        require(apiKey.isNotBlank()) {"GEMINI_API_KEY is empty."}

        val model = "gemini-2.5-flash"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val bodyJson = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(
                        JSONObject().apply {
                            put("text", "SYSTEM:\n$system\n\nUSER:\n$user")
                        }
                    ))
                }
            ))
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.4)
            })
        }

        val req = Request.Builder()
            .url(url)
            .post(bodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        val resp = client.newCall(req).execute()
        val raw = resp.body?.string().orEmpty()

        if (!resp.isSuccessful) {
            throw IllegalStateException("Gemini HTTP ${resp.code}: $raw")
        }

        val root = JSONObject(raw)
        val candidates = root.optJSONArray("candidates") ?: JSONArray()
        val first = if (candidates.length() > 0) candidates.getJSONObject(0) else JSONObject()
        val content = first.optJSONObject("content") ?: JSONObject()
        val parts = content.optJSONArray("parts") ?: JSONArray()
        val part0 = if (parts.length() > 0) parts.getJSONObject(0) else JSONObject()
        val text = part0.optString("text", "").trim()

        return GeminiChatResult(text = text)
    }
}