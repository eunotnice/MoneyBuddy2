//Note: the example above is a starting skeleton. After this works, you should replace the plain text parsing with proper structured output using Gemini JSON Schema, because that is the recommended reliable pattern.

package com.example.moneybuddy2.backend.service

import com.example.moneybuddy2.backend.config.GeminiConfig
import com.example.moneybuddy2.backend.data.UserFinanceSnapshot
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class GeminiService {

    private val client = OkHttpClient()

    fun generateAnswer(
        userMessage: String,
        snapshot: UserFinanceSnapshot,
        facts: List<InsightFact>
    ): String {
        val systemInstruction = """
            You are MoneyBuddy's financial assistant.
            Use the provided financial facts exactly as given.
            Do not invent personal numbers.
            If income is missing, state clearly that the advice is an estimate.
            Keep the answer practical and concise.
        """.trimIndent()

        val factsText = facts.joinToString("\n") { "- ${it.message}" }

        val userPrompt = """
            USER QUESTION:
            $userMessage

            SNAPSHOT:
            totalSpent=${snapshot.totalSpent}
            totalIncome=${snapshot.totalIncome}
            incomeConfidence=${snapshot.incomeConfidence}
            topSpendCategories=${snapshot.topSpendCategories}
            totalCo2eKg=${snapshot.totalCo2eKg}

            FACTS:
            $factsText
        """.trimIndent()

        val bodyJson = JSONObject()
            .put("systemInstruction", JSONObject()
                .put("parts", JSONArray().put(JSONObject().put("text", systemInstruction))))
            .put("contents", JSONArray().put(
                JSONObject().put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
            ))
            .put("tools", JSONArray().put(
                JSONObject().put(
                    "fileSearch",
                    JSONObject().put(
                        "fileSearchStoreNames",
                        JSONArray().put(GeminiConfig.fileSearchStore)
                    )
                )
            ))

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/${GeminiConfig.model}:generateContent?key=${GeminiConfig.apiKey}")
            .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) error("Gemini error: $raw")

            val json = JSONObject(raw)
            val candidates = json.optJSONArray("candidates") ?: return "No answer returned."
            if (candidates.length() == 0) return "No answer returned."

            val content = candidates.getJSONObject(0).optJSONObject("content") ?: return "No answer returned."
            val parts = content.optJSONArray("parts") ?: return "No answer returned."
            if (parts.length() == 0) return "No answer returned."

            return parts.getJSONObject(0).optString("text", "No answer returned.")
        }
    }
}