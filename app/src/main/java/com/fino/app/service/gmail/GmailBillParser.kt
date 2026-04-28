package com.fino.app.service.gmail

import com.fino.app.service.upi.CcBillPayeeVpa
import org.jsoup.Jsoup
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extracts a credit-card bill summary from the plain-text representation of a
 * statement email. Handles HDFC / ICICI / Axis out of the box; other issuers
 * fall back to a generic amount+date scrape.
 *
 * Returns null rather than throwing when fields can't be located, so the worker
 * can skip silently without burning retries.
 */
@Singleton
class GmailBillParser @Inject constructor() {

    fun parse(bank: String, from: String, subject: String, htmlOrText: String): ParsedBill? {
        val text = plainText(htmlOrText)
        val totalDue = extractAmount(text, TOTAL_DUE_PATTERNS) ?: return null
        val dueDate = extractDate(text, DUE_DATE_PATTERNS) ?: return null
        val minDue = extractAmount(text, MIN_DUE_PATTERNS)
        val cycleEnd = extractDate(text, CYCLE_END_PATTERNS) ?: dueDate
        val cycleStart = extractDate(text, CYCLE_START_PATTERNS)
        val last4 = LAST_4_PATTERN.find(text)?.groupValues?.getOrNull(1)

        val payee = CcBillPayeeVpa.resolve(bank)

        return ParsedBill(
            bank = bank,
            last4 = last4,
            cycleStartMillis = cycleStart,
            cycleEndMillis = cycleEnd,
            dueDateMillis = dueDate,
            totalDue = totalDue,
            minDue = minDue,
            payeeVpa = payee?.vpa,
            payeeName = payee?.payeeName
        )
    }

    private fun plainText(input: String): String {
        if (!input.contains('<')) return input
        return Jsoup.parse(input).text()
    }

    private fun extractAmount(text: String, patterns: List<Regex>): Double? {
        for (pattern in patterns) {
            val match = pattern.find(text) ?: continue
            val raw = match.groupValues.drop(1).firstOrNull { it.isNotBlank() } ?: continue
            val cleaned = raw.replace(",", "").replace(Regex("[^0-9.]"), "")
            val value = cleaned.toDoubleOrNull()
            if (value != null && value > 0) return value
        }
        return null
    }

    private fun extractDate(text: String, patterns: List<Regex>): Long? {
        for (pattern in patterns) {
            val match = pattern.find(text) ?: continue
            val raw = match.groupValues.drop(1).firstOrNull { it.isNotBlank() } ?: continue
            val parsed = parseDate(raw) ?: continue
            return parsed
        }
        return null
    }

    private fun parseDate(raw: String): Long? {
        val candidates = listOf(
            "dd-MM-yyyy", "dd/MM/yyyy", "dd MMM yyyy", "dd MMMM yyyy",
            "MMM dd, yyyy", "yyyy-MM-dd", "dd-MMM-yyyy"
        )
        for (pattern in candidates) {
            try {
                val format = SimpleDateFormat(pattern, Locale.ENGLISH).apply {
                    timeZone = TimeZone.getTimeZone("Asia/Kolkata")
                    isLenient = false
                }
                val date = format.parse(raw.trim()) ?: continue
                return date.time
            } catch (_: ParseException) {
            }
        }
        return null
    }

    companion object {
        private val TOTAL_DUE_PATTERNS = listOf(
            Regex("(?i)total\\s*amount\\s*due[^0-9]*([0-9,]+(?:\\.[0-9]{1,2})?)"),
            Regex("(?i)total\\s*due[^0-9]*([0-9,]+(?:\\.[0-9]{1,2})?)"),
            Regex("(?i)amount\\s*payable[^0-9]*([0-9,]+(?:\\.[0-9]{1,2})?)")
        )

        private val MIN_DUE_PATTERNS = listOf(
            Regex("(?i)minimum\\s*amount\\s*due[^0-9]*([0-9,]+(?:\\.[0-9]{1,2})?)"),
            Regex("(?i)min(?:imum)?\\s*due[^0-9]*([0-9,]+(?:\\.[0-9]{1,2})?)")
        )

        private val DUE_DATE_PATTERNS = listOf(
            Regex("(?i)payment\\s*due\\s*date[^0-9A-Za-z]*([0-9A-Za-z ,/\\-]+)"),
            Regex("(?i)due\\s*(?:by|on|date)[^0-9A-Za-z]*([0-9A-Za-z ,/\\-]+)")
        )

        private val CYCLE_START_PATTERNS = listOf(
            Regex("(?i)statement\\s*period[^0-9A-Za-z]*([0-9A-Za-z ,/\\-]+?)(?:\\s*[-to]{1,3}\\s*[0-9])")
        )

        private val CYCLE_END_PATTERNS = listOf(
            Regex("(?i)statement\\s*date[^0-9A-Za-z]*([0-9A-Za-z ,/\\-]+)"),
            Regex("(?i)bill\\s*date[^0-9A-Za-z]*([0-9A-Za-z ,/\\-]+)")
        )

        private val LAST_4_PATTERN = Regex("(?i)(?:card|ending)(?:\\s*(?:no|number))?[^0-9]*(\\d{4})")
    }
}
