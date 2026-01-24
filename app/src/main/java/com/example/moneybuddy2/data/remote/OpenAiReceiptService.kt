//package com.example.moneybuddy2.data.remote
//
//import com.example.moneybuddy2.BuildConfig
//import com.example.moneybuddy2.data.model.AiReceiptResult
//import okhttp3.MediaType.Companion.toMediaType
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.RequestBody.Companion.toRequestBody
//import org.json.JSONArray
//import org.json.JSONObject
//
//class OpenAiReceiptService {
//
//    private val client = OkHttpClient()
//    private val jsonMedia = "application/json; charset=utf-8".toMediaType()
//
//    fun extractReceiptFieldsBlocking(ocrText: String): AiReceiptResult {
//        val prompt = buildPrompt(ocrText)
//
//        // Build Structured Output schema for the model
//        val properties = JSONObject().apply {
//            put("merchant", JSONObject().apply {
//                put("type", "string")
//                put("description", "Business/store name. Do NOT return server/cashier/staff names.")
//            })
//            put("amount", JSONObject().apply {
//                put("type", JSONArray().put("number").put("null"))
//                put("description", "Grand total paid (not subtotal).")
//            })
//            put("dateIso", JSONObject().apply {
//                put("type", JSONArray().put("string").put("null"))
//                put("description", "Purchase date in YYYY-MM-DD, or null if unknown.")
//            })
//            put("merchantCandidates", JSONObject().apply {
//                put("type", "array")
//                put("items", JSONObject().apply { put("type", "string") })
//                put("description", "Up to 5 plausible merchant names (exclude staff/cashier names)."
//                )
//            })
//        }
//
//        val schemaObj = JSONObject().apply {
//            put("type", "object")
//            put("additionalProperties", false)
//            put("properties", properties)
//            put("required", JSONArray()
//                .put("merchant")
//                .put("amount")
//                .put("dateIso")
//                .put("merchantCandidates")
//            )
//        }
//
//// ---- Build Responses API body ----
//        val bodyJson = JSONObject().apply {
//            put("model", "gpt-4o-mini")
//            put("store", false)
//            put("input", prompt)
//
//            // ✅ Correct placement: name/strict/schema under text.format
//            put("text", JSONObject().apply {
//                put("format", JSONObject().apply {
//                    put("type", "json_schema")
//                    put("name", "receipt_extraction")
//                    put("strict", true)
//                    put("schema", schemaObj)
//                })
//            })
//        }
//
//        val reqBody = bodyJson.toString().toRequestBody(jsonMedia)
//
//        val request = Request.Builder()
//            .url("https://api.openai.com/v1/responses")
//            .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
//            .addHeader("Content-Type", "application/json")
//            .post(reqBody)
//            .build()
//
//        client.newCall(request).execute().use { resp ->
//            val text = resp.body?.string().orEmpty()
//            if (!resp.isSuccessful) {
//                throw IllegalStateException("OpenAI HTTP ${resp.code}: $text")
//            }
//
//            val root = JSONObject(text)
//            val outputText = root.optString("output_text", "")
//            if (outputText.isBlank()) throw IllegalStateException("No output_text in response.")
//
//            // output_text is JSON text that matches our schema
//            val out = JSONObject(outputText)
//
//            val merchant = out.optString("merchant", "")
//            val amount = if (out.isNull("amount")) null else out.optDouble("amount")
//            val dateIso = if (out.isNull("dateIso")) null else out.optString("dateIso")
//
//            val candidatesJson = out.optJSONArray("merchantCandidates") ?: JSONArray()
//            val candidates = buildList {
//                for (i in 0 until candidatesJson.length()) {
//                    val s = candidatesJson.optString(i, "").trim()
//                    if (s.isNotBlank()) add(s)
//                }
//            }.take(5)
//
//            return AiReceiptResult(
//                merchant = merchant,
//                amount = amount,
//                dateIso = dateIso,
//                merchantCandidates = candidates
//            )
//        }
//    }
//
//    private fun buildPrompt(ocrText: String): String {
//        val trimmed = ocrText.trim().take(12000)
//        return """
//You are extracting structured fields from receipt OCR text.
//
//Rules:
//1) merchant must be the BUSINESS/STORE name, not cashier/server/staff/person name.
//2) amount should be the GRAND TOTAL paid (not subtotal). If unsure, return null.
//3) dateIso should be purchase date in YYYY-MM-DD. If unsure, return null.
//4) merchantCandidates: up to 5 plausible merchant names (business/store). Exclude staff/cashier names.
//
//Receipt OCR text:
//$trimmed
//""".trim()
//    }
//}