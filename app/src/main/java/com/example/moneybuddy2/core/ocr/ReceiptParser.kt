package com.example.moneybuddy2.core.ocr

import java.util.Locale
import java.util.Calendar

data class ParsedReceipt(
    val merchant: String = "",
    val amount: Double? = 0.0,
    val dateMillis: Long? = null,
    val rawText: String = "",
    val category: String? = "Other",
    val merchantCandidates: List<String>? = emptyList()
)

object ReceiptParser {
    fun parse(rawText: String): ParsedReceipt{
        val lines = rawText
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val merchant = guessMerchant(lines)
        val amount = guessAmount(lines)
        val dateMillis = guessDateMillis(lines)

        return ParsedReceipt(
            merchant = merchant,
            amount = amount,
            dateMillis = dateMillis
        )
    }

    private fun guessMerchant(lines: List<String>): String {
        if (lines.isEmpty()) return ""

        val blacklistWords = listOf(
            "tax", "invoice", "receipt", "thank", "welcome",
            "subtotal", "total", "change", "cash", "visa", "master",
            "tel", "phone", "gst", "sst"
        )

        // Consider first 12 lines; merchant is usually near top
        val candidates = lines.take(12).filter { line ->
            val lower = line.lowercase(Locale.getDefault())

            // Reject lines that look like address/metadata
            val hasBlacklist = blacklistWords.any { lower.contains(it) }
            val tooShort = line.length < 3
            val mostlyDigits = line.count { it.isDigit() } >= (line.length / 2)
            val hasMoney = extractMoney(line) != null
            val looksLikeAddress = lower.contains("jalan") || lower.contains("jln") ||
                    lower.contains("no.") || lower.contains("postcode") || lower.contains("kuala") ||
                    lower.contains("selangor") || lower.contains("pulau") || lower.contains("kedah") ||
                    lower.contains("penang") || lower.contains("malaysia")

            !hasBlacklist && !tooShort && !mostlyDigits && !hasMoney && !looksLikeAddress
        }

        // Prefer the first candidate that is mostly letters
        return candidates.firstOrNull() ?: lines.first()
    }


    private fun guessAmount(lines: List<String>): Double? {
        //look for lines containing TOTAL first
        val totalCandidates = lines.filter{ it.lowercase(Locale.getDefault()).contains("total") }
        val fromTotal = totalCandidates.mapNotNull { extractMoney(it) }.maxOrNull()
        if(fromTotal != null)
            return fromTotal
        //otherwise pick the largest money-like value on receipt
        val all = lines.mapNotNull { extractMoney(it) }
        return all.maxOrNull()
    }

    private fun extractMoney(line: String): Double?{
        val regex = Regex("""(?i)(rm\s*)?(\d{1,3}(?:[,\s]\d{3})*|\d+)([.,]\d{2})""")
        val match = regex.find(line) ?: return null
        val number = match.value
            .lowercase(Locale.getDefault())
            .replace("rm", "")
            .replace(" ", "")
            .replace(",", "")       // remove thousands separator
            .replace("\u00A0", "")  // non-breaking space if present
            .trim()

        //normalize if decimal uses comma
        val normalized = if (number.count { it == ',' } == 1 && number.count { it == '.' } == 0) {
            number.replace(",", ".")
        } else number

        return normalized.toDoubleOrNull()
    }

    private fun guessDateMillis(lines: List<String>): Long? {
        val patterns = listOf(
            Regex("""\b(\d{2})[/-](\d{2})[/-](\d{4})\b"""), // dd/MM/yyyy
            Regex("""\b(\d{4})[/-](\d{2})[/-](\d{2})\b""")  // yyyy/MM/dd
        )

        val joined = lines.joinToString(" ")

        // Try dd/MM/yyyy first
        patterns[0].find(joined)?.let { m ->
            val dd = m.groupValues[1].toIntOrNull() ?: return null
            val mm = m.groupValues[2].toIntOrNull() ?: return null
            val yyyy = m.groupValues[3].toIntOrNull() ?: return null
            return calendarMillis(yyyy, mm, dd)
        }

        // Try yyyy/MM/dd
        patterns[1].find(joined)?.let { m ->
            val yyyy = m.groupValues[1].toIntOrNull() ?: return null
            val mm = m.groupValues[2].toIntOrNull() ?: return null
            val dd = m.groupValues[3].toIntOrNull() ?: return null
            return calendarMillis(yyyy, mm, dd)
        }

        return null
    }

    private fun calendarMillis(year: Int, month1to12: Int, day: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, (month1to12 - 1).coerceIn(0, 11))
        cal.set(Calendar.DAY_OF_MONTH, day.coerceIn(1, 31))
        cal.set(Calendar.HOUR_OF_DAY, 12)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}