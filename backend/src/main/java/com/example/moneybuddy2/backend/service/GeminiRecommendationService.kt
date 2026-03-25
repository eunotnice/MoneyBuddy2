package com.example.moneybuddy2.backend.service

import com.example.moneybuddy2.backend.config.GeminiConfig
import com.example.moneybuddy2.backend.data.AiBudgetPlan
import com.example.moneybuddy2.backend.data.AiRecommendationRequest
import com.example.moneybuddy2.backend.data.AiRecommendationResponse
import com.example.moneybuddy2.backend.data.AiSavingRecommendation
import com.example.moneybuddy2.backend.data.UserFinanceSnapshot
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiRecommendationService {

    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()
    private val json = Json { ignoreUnknownKeys = true }

    fun generateRecommendation(
        requestInput: AiRecommendationRequest,
        snapshot: UserFinanceSnapshot
    ): AiRecommendationResponse {
        val systemInstruction = """
            You are MoneyBuddy's budgeting and saving assistant.

            Your job is to create a personalised budget plan and practical saving recommendations.

            Rules:
            1. Use the user's financial facts exactly as provided.
            2. Use the 50/30/20 rule as the baseline budgeting framework.
            3. You may adjust the budget ratio if the user's lifestyle, responsibilities, or goals justify it.
            4. If income is missing or estimated, clearly state that the plan is approximate.
            5. Prioritise emergency funds before fixed deposits when appropriate.
            6. Fixed deposit recommendations must remain educational and low-risk in nature. Do not provide investment advice or product-specific recommendations.
            7. Keep recommendations practical, realistic, and concise.
            8. Return valid JSON only, following the requested schema.
        """.trimIndent()

        val topCategoriesText = snapshot.topSpendCategories.joinToString("\n") {
            "- ${it.first}: RM %.2f".format(it.second)
        }

        val spendByCategoryText = snapshot.spendByCategory.entries.joinToString("\n") {
            "- ${it.key}: RM %.2f".format(it.value)
        }

        val userPrompt = """
            USER LIFESTYLE INPUT
            lifestyleNote: ${requestInput.lifestyleNote ?: ""}
            priorities: ${requestInput.priorities.joinToString(", ")}
            riskPreference: ${requestInput.riskPreference ?: ""}
            savingGoalNote: ${requestInput.savingGoalNote ?: ""}

            TRUSTED USER FINANCIAL FACTS
            currency: ${snapshot.currency}
            totalSpent: ${snapshot.totalSpent}
            totalIncome: ${snapshot.totalIncome}
            incomeConfidence: ${snapshot.incomeConfidence}
            periodStartMillis: ${snapshot.periodStartMillis}
            periodEndMillis: ${snapshot.periodEndMillis}

            spendingByCategory:
            $spendByCategoryText

            topSpendCategories:
            $topCategoriesText

            totalCo2eKg: ${snapshot.totalCo2eKg}
            treesEquivalent: ${snapshot.treesEquivalent}
            treesFactorLabel: ${snapshot.treesFactorLabel}

            TASK
            Create:
            1. a personalised budget plan
            2. a concise summary
            3. several practical saving recommendations

            The budget plan must include:
            - ruleLabel
            - incomeUsed
            - incomeConfidence
            - needsTarget
            - wantsTarget
            - savingsTarget
            - rationale

            The recommendations should reflect:
            - budget discipline
            - emergency fund priorities
            - fixed deposit suitability if appropriate
            - realistic saving behaviour
        """.trimIndent()

        val schema = JSONObject()
            .put("type", "object")
            .put("properties", JSONObject()
                .put("budgetPlan", JSONObject()
                    .put("type", "object")
                    .put("properties", JSONObject()
                        .put("ruleLabel", JSONObject().put("type", "string"))
                        .put("incomeUsed", JSONObject().put("type", listOf("number", "null")))
                        .put("incomeConfidence", JSONObject().put("type", "string"))
                        .put("needsTarget", JSONObject().put("type", listOf("number", "null")))
                        .put("wantsTarget", JSONObject().put("type", listOf("number", "null")))
                        .put("savingsTarget", JSONObject().put("type", listOf("number", "null")))
                        .put("rationale", JSONObject().put("type", "string"))
                    )
                    .put("required", JSONArray()
                        .put("ruleLabel")
                        .put("incomeUsed")
                        .put("incomeConfidence")
                        .put("needsTarget")
                        .put("wantsTarget")
                        .put("savingsTarget")
                        .put("rationale")
                    )
                )
                .put("summary", JSONObject().put("type", "string"))
                .put("recommendations", JSONObject()
                    .put("type", "array")
                    .put("items", JSONObject()
                        .put("type", "object")
                        .put("properties", JSONObject()
                            .put("title", JSONObject().put("type", "string"))
                            .put("message", JSONObject().put("type", "string"))
                            .put("priority", JSONObject().put("type", "integer"))
                            .put("category", JSONObject().put("type", "string"))
                        )
                        .put("required", JSONArray()
                            .put("title")
                            .put("message")
                            .put("priority")
                            .put("category")
                        )
                    )
                )
            )
            .put("required", JSONArray()
                .put("budgetPlan")
                .put("summary")
                .put("recommendations")
            )

        val bodyJson = JSONObject()
            .put("systemInstruction", JSONObject()
                .put("parts", JSONArray().put(JSONObject().put("text", systemInstruction))))
            .put("contents", JSONArray().put(
                JSONObject().put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
            ))
//            .put("tools", JSONArray().put(
//                JSONObject().put(
//                    "fileSearch",
//                    JSONObject().put(
//                        "fileSearchStoreNames",
//                        JSONArray().put(GeminiConfig.fileSearchStore)
//                    )
//                )
//            ))
            .put("generationConfig", JSONObject()
                .put("responseMimeType", "application/json")
                .put("responseJsonSchema", schema)
            )

        val httpRequest = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/${GeminiConfig.model}:generateContent?key=${GeminiConfig.apiKey}")
            .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(httpRequest).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                error("Gemini error: $raw")
            }

            val jsonResponse = JSONObject(raw)
            val candidates = jsonResponse.optJSONArray("candidates")
                ?: error("Gemini returned no candidates")
            if (candidates.length() == 0) error("Gemini returned empty candidates")

            val content = candidates.getJSONObject(0).optJSONObject("content")
                ?: error("Gemini returned no content")
            val parts = content.optJSONArray("parts")
                ?: error("Gemini returned no parts")
            if (parts.length() == 0) error("Gemini returned empty parts")

            val text = parts.getJSONObject(0).optString("text")
            if (text.isBlank()) error("Gemini returned blank text")

            val cleaned = text
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            println("===== GEMINI CLEANED JSON =====")
            println(cleaned)

            println("===== BEFORE DECODE =====")

            val decoded = json.decodeFromString(
                AiRecommendationResponse.serializer(),
                cleaned
            )

            println("===== AFTER DECODE =====")

            return decoded
            //return json.decodeFromString(AiRecommendationResponse.serializer(), cleaned)
            //return json.decodeFromString(AiRecommendationResponse.serializer(), text)
        }
    }
}