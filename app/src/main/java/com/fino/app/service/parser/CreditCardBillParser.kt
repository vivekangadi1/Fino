package com.fino.app.service.parser

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Parser for Credit Card Bill SMS messages.
 * Extracts total due, minimum due, and due date.
 */
class CreditCardBillParser {

    private val patterns = listOf(
        // HDFC: "Your HDFC Credit Card XX4523 statement is ready. Total Due: Rs.12450. Min Due: Rs.625. Due Date: 05-Jan-25"
        ParsePattern(
            name = "HDFC_BILL",
            regex = Regex(
                """HDFC\s+Credit\s+Card\s+XX(\d{4})\s+statement.*?Total\s+Due[:\s]*Rs\.?([0-9,]+\.?\d*).*?Min\s+Due[:\s]*Rs\.?([0-9,]+\.?\d*).*?Due\s+Date[:\s]*(\d{2}-\w{3}-\d{2,4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // HDFC alternate: "HDFC Credit Card XX4523 Bill: Total Due Rs.25,430, Min Due Rs.1,272, Due Date 05-Jan-25"
        ParsePattern(
            name = "HDFC_BILL_ALT",
            regex = Regex(
                """HDFC\s+Credit\s+Card\s+XX(\d{4})\s+Bill[:\s]*Total\s+Due\s+Rs\.?([0-9,]+\.?\d*),?\s+Min\s+Due\s+Rs\.?([0-9,]+\.?\d*),?\s+Due\s+Date\s+(\d{2}-\w{3}-\d{2,4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // ICICI: "ICICI Card bill generated. Amount: Rs.3200. Due: 12-Jan-25"
        ParsePattern(
            name = "ICICI_BILL",
            regex = Regex(
                """ICICI\s+Card\s+bill\s+generated.*?Amount[:\s]*Rs\.?([0-9,]+\.?\d*).*?Due[:\s]*(\d{2}-\w{3}-\d{2,4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.9f
        ),

        // ICICI full: "Your ICICI Credit Card XX8976 bill is Rs.8,750. Min Due: Rs.438. Due Date: 12-Jan-25"
        ParsePattern(
            name = "ICICI_BILL_FULL",
            regex = Regex(
                """ICICI\s+Credit\s+Card\s+XX(\d{4})\s+bill\s+is\s+Rs\.?([0-9,]+\.?\d*).*?Min\s+Due[:\s]*Rs\.?([0-9,]+\.?\d*).*?Due\s+Date[:\s]*(\d{2}-\w{3}-\d{2,4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // SBI: "SBI Card XX3456 Statement: Total Due Rs.15,600, Min Due Rs.780, Due by 20-Jan-25"
        ParsePattern(
            name = "SBI_BILL",
            regex = Regex(
                """SBI\s+Card\s+XX(\d{4})\s+Statement[:\s]*Total\s+Due\s+Rs\.?([0-9,]+\.?\d*),?\s+Min\s+Due\s+Rs\.?([0-9,]+\.?\d*),?\s+Due\s+by\s+(\d{2}-\w{3}-\d{2,4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        )
    )

    fun parse(smsBody: String): ParsedBill? {
        for (pattern in patterns) {
            val match = pattern.regex.find(smsBody)
            if (match != null) {
                return extractBill(match, pattern)
            }
        }
        return null
    }

    private fun extractBill(match: MatchResult, pattern: ParsePattern): ParsedBill {
        return when (pattern.name) {
            "HDFC_BILL", "HDFC_BILL_ALT" -> ParsedBill(
                cardLastFour = match.groupValues[1],
                bankName = "HDFC",
                totalDue = SmsParser.parseAmount(match.groupValues[2]),
                minimumDue = SmsParser.parseAmount(match.groupValues[3]),
                dueDate = parseBillDate(match.groupValues[4])
            )

            "ICICI_BILL" -> ParsedBill(
                cardLastFour = "",  // Not available in this format
                bankName = "ICICI",
                totalDue = SmsParser.parseAmount(match.groupValues[1]),
                minimumDue = null,
                dueDate = parseBillDate(match.groupValues[2])
            )

            "ICICI_BILL_FULL" -> ParsedBill(
                cardLastFour = match.groupValues[1],
                bankName = "ICICI",
                totalDue = SmsParser.parseAmount(match.groupValues[2]),
                minimumDue = SmsParser.parseAmount(match.groupValues[3]),
                dueDate = parseBillDate(match.groupValues[4])
            )

            "SBI_BILL" -> ParsedBill(
                cardLastFour = match.groupValues[1],
                bankName = "SBI",
                totalDue = SmsParser.parseAmount(match.groupValues[2]),
                minimumDue = SmsParser.parseAmount(match.groupValues[3]),
                dueDate = parseBillDate(match.groupValues[4])
            )

            else -> throw IllegalArgumentException("Unknown pattern: ${pattern.name}")
        }
    }

    private fun parseBillDate(dateStr: String): LocalDate {
        val patterns = listOf(
            "dd-MMM-yy",
            "dd-MMM-yyyy",
            "dd-MM-yy",
            "dd-MM-yyyy"
        )

        for (pattern in patterns) {
            try {
                val formatter = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)
                return LocalDate.parse(dateStr.trim(), formatter)
            } catch (e: Exception) {
                // Try next pattern
            }
        }

        // Default to 30 days from now if parsing fails
        return LocalDate.now().plusDays(30)
    }
}
