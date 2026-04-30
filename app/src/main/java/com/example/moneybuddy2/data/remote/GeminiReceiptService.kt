package com.example.moneybuddy2.data.remote

import android.util.Log
import com.example.moneybuddy2.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class GeminiReceiptService(
    private val client: OkHttpClient = OkHttpClient()
) {
    /**
     * Calls Gemini generateContent (REST) and returns structured receipt fields.
     *
     * Endpoint format:
     * https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent
     */
    fun extractReceiptFieldsBlocking(ocrText: String): AiReceiptResult {
        val apiKey = BuildConfig.GEMINI_API_KEY
        require(apiKey.isNotBlank()) { "GEMINI_API_KEY is empty. Check local.properties + build.gradle.kts." }

        // Use a stable, fast model for MVP
        val model = "gemini-2.5-flash"
        val url =
            "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        // ---- Controlled JSON output (responseMimeType + responseSchema) ----
        // Supported fields documented under GenerationConfig: responseMimeType + responseSchema :contentReference[oaicite:2]{index=2}
        val responseJsonSchema = JSONObject().apply {
            put("type", "object")
            put("additionalProperties", false)
            put("properties", JSONObject().apply {
                put("merchant", JSONObject().apply { put("type", "string") })
                put("amount", JSONObject().apply {
                    put("type", JSONArray().put("number").put("null"))
                })
                put("dateIso", JSONObject().apply {
                    put("type", JSONArray().put("string").put("null"))
                    put("description", "YYYY-MM-DD or null")
                })
                put("merchantCandidates", JSONObject().apply {
                    put("type", "array")
                    put("items", JSONObject().apply { put("type", "string") })
                })

                put("category", JSONObject().apply { put("type", "string") })
                put("categoryConfidence", JSONObject().apply { put("type", "number") })
                put("categoryReason", JSONObject().apply { put("type", "string") })
            })
            put("required", JSONArray()
                .put("merchant")
                .put("amount")
                .put("dateIso")
                .put("merchantCandidates")
                .put("category")
                .put("categoryConfidence")
                .put("categoryReason")
            )
        }

        val allowedCategories = listOf(
            "Food & Drink","Groceries","Transport","Shopping","Utilities",
            "Health","Education","Entertainment","Travel","Services","Others"
        ).joinToString(", ")

        val systemRules = """
            You extract purchase receipt fields from OCR text.
            Return ONLY JSON matching the schema.
            
            Rules:
            - Merchant must be the business/store name, NOT server/cashier/staff.
            - Amount must be the GRAND TOTAL paid (TOTAL/GRAND TOTAL/AMOUNT DUE).
            - Date must be YYYY-MM-DD if present; otherwise null.
            - merchantCandidates: up to 5 plausible business names; exclude staff/cashier names.
            
            Category classification:
            - Choose EXACTLY ONE category from this list: $allowedCategories
            - Use "Groceries" for supermarkets/minimarts; "Food & Drink" for restaurants/cafes/bars.
            - Use "Transport" for fuel, transit, ride-hailing, tolls.
            - Use "Utilities" for telco, internet, electricity, water.
            - Use "Shopping" for retail/general goods not groceries.
            - If uncertain, set category = "Others" with low confidence.
            
            Return:
            - categoryConfidence: 0.0–1.0
            - categoryReason: <= 12 words (e.g., "merchant is a cafe; items include latte").
            """.trimIndent()

        val bodyJson = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(
                        JSONObject().apply {
                            put("text", systemRules + "\n\nOCR TEXT:\n" + ocrText)
                        }
                    ))
                }
            ))

            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("responseJsonSchema", responseJsonSchema)
                put("temperature", 0.2)
            })
        }

        val reqBody = bodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val req = Request.Builder()
            .url(url)
            .post(reqBody)
            .build()

        Log.d("GeminiHTTP", "REQUEST URL = $url")
        Log.d("GeminiHTTP", "REQUEST BODY = ${bodyJson.toString(2)}")


        val resp = client.newCall(req).execute()
        val text = resp.body?.string().orEmpty()
        Log.d("GeminiHTTP", "RESPONSE BODY = $text")

        if (!resp.isSuccessful) {
            throw IllegalStateException("Gemini HTTP ${resp.code}: $text")
        }

        // Gemini REST response: candidates[0].content.parts[0].text
        val root = JSONObject(text)
        val candidates = root.optJSONArray("candidates") ?: JSONArray()
        val first = if (candidates.length() > 0) candidates.getJSONObject(0) else JSONObject()
        val content = first.optJSONObject("content") ?: JSONObject()
        val parts = content.optJSONArray("parts") ?: JSONArray()
        val part0 = if (parts.length() > 0) parts.getJSONObject(0) else JSONObject()
        var jsonText = part0.optString("text", "").trim()

        // Sometimes models wrap JSON in code fences; strip them
        jsonText = jsonText.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()

        val out = JSONObject(jsonText)

        val merchant = out.optString("merchant", "")
        val amount = if (out.isNull("amount")) null else out.optDouble("amount")
        val dateIso = if (out.isNull("dateIso")) null else out.optString("dateIso")

        val cand = out.optJSONArray("merchantCandidates") ?: JSONArray()
        val candidatesList = buildList {
            for (i in 0 until cand.length()) add(cand.optString(i))
        }
        val category = out.optString("category", "Others")
        out.optDouble("categoryConfidence", 0.0)
        out.optString("categoryReason", "")


        return AiReceiptResult(
            merchant = merchant,
            amount = amount,
            category = category,
//            categoryConfidence = categoryConfidence,
//            categoryReason = categoryReason,
            dateIso = dateIso,
            merchantCandidates = candidatesList
        )
    }
}
