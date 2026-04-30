package com.example.moneybuddy2.core.ocr

import java.util.Locale

data class ReceiptValidationResult(
    val isReceipt: Boolean,
    val confidence: Double,
    val reason: String
)

object ReceiptValidator {

    fun validate(rawText: String): ReceiptValidationResult {
        val text = rawText.trim()
        if (text.isBlank()) {
            return ReceiptValidationResult(
                isReceipt = false,
                confidence = 0.0,
                reason = "No text detected. Please upload a clearer receipt image."
            )
        }

        val lower = text.lowercase(Locale.getDefault())
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }

        var score = 0
        // 1. Has money values
        val moneyRegex = Regex("""(?i)(rm\s*)?(\d{1,3}(?:[,\s]\d{3})*|\d+)([.,]\d{2})""")
        val moneyMatches = moneyRegex.findAll(text).count()
        if (moneyMatches >= 1) score += 2
        if (moneyMatches >= 3) score += 1
        // 2. Has receipt keywords
        val keywords = listOf(
            "total", "subtotal", "tax", "cash", "change", "receipt",
            "invoice", "amount due", "grand total", "payment"
        )
        val keywordHits = keywords.count { lower.contains(it) }
        if (keywordHits >= 1) score += 2
        if (keywordHits >= 2) score += 1
        // 3. Has date pattern
        val dateRegex1 = Regex("""\b\d{2}[/-]\d{2}[/-]\d{4}\b""")
        val dateRegex2 = Regex("""\b\d{4}[/-]\d{2}[/-]\d{2}\b""")
        if (dateRegex1.containsMatchIn(text) || dateRegex2.containsMatchIn(text)) {
            score += 2
        }
        // 4. Multiple short transactional lines
        val shortLines = lines.count { it.length in 3..40 }
        if (shortLines >= 4) score += 1
        // 5. Penalise paragraph-like text
        val longLines = lines.count { it.length > 80 }
        if (longLines >= 2) score -= 2
        // 6. Penalise too little text
        if (text.length < 20) score -= 3
        val confidence = (score.coerceIn(0, 8)) / 8.0
        return when {
            score >= 4 -> ReceiptValidationResult(
                isReceipt = true,
                confidence = confidence,
                reason = "Receipt detected."
            )
            else -> ReceiptValidationResult(
                isReceipt = false,
                confidence = confidence,
                reason = "This image does not appear to be a valid purchase receipt."
            )
        }
    }
}