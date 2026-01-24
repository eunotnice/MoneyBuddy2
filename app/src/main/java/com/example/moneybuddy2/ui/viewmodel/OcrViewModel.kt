package com.example.moneybuddy2.ui.viewmodel

import android.util.Log
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneybuddy2.core.ocr.ParsedReceipt
import com.example.moneybuddy2.core.ocr.ReceiptParser
import com.example.moneybuddy2.data.model.Expense
import com.example.moneybuddy2.data.remote.FirebaseProvider
import com.example.moneybuddy2.data.repository.MoneyRepository
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.example.moneybuddy2.data.remote.GeminiReceiptService


//import com.example.moneybuddy2.data.remote.OpenAiReceiptService
import com.example.moneybuddy2.core.util.DateUtils.isoToMillis

data class OcrUiState(
    val loading: Boolean = false,
    val error: String? = null,

    val imageUri: Uri? = null,
    val rawText: String = "",

    val parsed: com.example.moneybuddy2.core.ocr.ParsedReceipt? = null
)

class OcrViewModel (
    private val repo: MoneyRepository
) : ViewModel() {

   // private val openAi = OpenAiReceiptService()
    private val gemini = GeminiReceiptService()

    private val TAG = "OcrVM"


    private val _ui = MutableStateFlow(OcrUiState())
    val ui: StateFlow<OcrUiState> = _ui

    fun setImage(uri: Uri?){
        _ui.value = _ui.value.copy(imageUri = uri, error = null)
    }

    fun runOcr(context: android.content.Context) {
        val uri = _ui.value.imageUri ?: run {
            _ui.value = _ui.value.copy(error = "Please pick an image first.")
            return
        }

        _ui.value = _ui.value.copy(loading = true, error = null, rawText = "", parsed = null)

        viewModelScope.launch {
            try {
                val image = InputImage.fromFilePath(context, uri)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val result = recognizer.process(image).await()

                val raw = result.text ?: ""
                Log.d(TAG, "OCR done. raw length=${raw.length}")
                val systemRules = """
    You extract purchase receipt fields from OCR text.
    IMPORTANT:
    - Merchant must be the business/store name, NOT server/cashier/staff.
    - Amount must be the GRAND TOTAL paid.
    - Date must be YYYY-MM-DD or null.
    Return ONLY JSON.
""".trimIndent()

                logGeminiInput(systemRules, raw)

                val parsed: ParsedReceipt = try {
                    val ai = withContext(Dispatchers.IO) {
                        gemini.extractReceiptFieldsBlocking(raw)
                    }

                    ParsedReceipt(
                        merchant = ai.merchant,
                        merchantCandidates = ai.merchantCandidates,
                        category = ai.category,
                        amount = ai.amount,
                        dateMillis = ai.dateIso?.let { isoToMillis(it) }
                    )

                } catch (e: Exception) {
                    Log.e(TAG, "Gemini failed, fallback to rule parser", e)

                    // IMPORTANT: this must return ParsedReceipt
                    ReceiptParser.parse(raw)   // <- this must be ParsedReceipt
                }

                Log.d(TAG, "AI PARSED RESULT = $parsed")


                _ui.value = _ui.value.copy(
                    loading = false,
                    rawText = raw,
                    parsed = parsed,
                    error = null
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(loading = false, error = e.message ?: "OCR failed")
            }
        }
    }

    fun saveConfirmedExpense(
        merchant: String,
        amount: Double,
        category: String,
        description: String,
        dateMillis: Long,
        onSaved: () -> Unit
    ) {
        val user = FirebaseProvider.auth.currentUser
        if (user == null) {
            _ui.value = _ui.value.copy(error = "User not logged in")
            return
        }
        val raw = _ui.value.rawText

        _ui.value = _ui.value.copy(loading = true, error = null)

        viewModelScope.launch {
            val expense = Expense(
                merchant = merchant.trim(),
                amount = amount,
                category = category,
                description = description.trim(),
                dateMillis = dateMillis,
                source = "ocr",
                rawText = raw
            )

            val ok = repo.addExpense(user.uid, expense)
            _ui.value = _ui.value.copy(loading = false, error = if (ok) null else "Failed to save expense")
            if (ok) onSaved()
        }
    }

    fun reset(){
        _ui.value = OcrUiState()
    }

    private fun logGeminiInput(systemRules: String, ocrText: String) {
        Log.d(TAG, "===== GEMINI INPUT =====")
        Log.d(TAG, "SYSTEM RULES:\n$systemRules")
        Log.d(TAG, "OCR TEXT (len=${ocrText.length}):\n$ocrText")
        Log.d(TAG, "========================")
    }

}