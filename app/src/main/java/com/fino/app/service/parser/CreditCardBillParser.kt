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
        ),

        // Axis: "Payment of INR 15236.36 for Axis Bank Credit Card no. XX5519 is due on 01-01-26 with minimum amount due of INR 305."
        ParsePattern(
            name = "AXIS_BILL",
            regex = Regex(
                """Payment\s+of\s+INR\s+([0-9,]+\.?\d*)\s+for\s+Axis\s+Bank\s+Credit\s+Card\s+no\.\s+XX(\d{4})\s+is\s+due\s+on\s+(\d{2}-\d{2}-\d{2,4})\s+with\s+minimum\s+amount\s+due\s+of\s+INR\s+([0-9,]+\.?\d*)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // ICICI Statement Email: "ICICI Bank Credit Card XX1016 Statement is sent to email. Total of Rs 1,504.00 or minimum of Rs 100.00 is due by 05-JAN-26."
        ParsePattern(
            name = "ICICI_STATEMENT_EMAIL",
            regex = Regex(
                """ICICI\s+Bank\s+Credit\s+Card\s+XX(\d{4})\s+Statement\s+is\s+sent\s+to\s+\S+\.\s+Total\s+of\s+Rs\s+([0-9,]+\.?\d*)\s+or\s+minimum\s+of\s+Rs\s+([0-9,]+\.?\d*)\s+is\s+due\s+by\s+(\d{2}-\w{3}-\d{2,4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // ICICI Auto-debit: "Total Amount Due on ICICI Bank Credit Card XX2000 is INR 2,218.42. Amount will be debited...on or before 29-Dec-25."
        ParsePattern(
            name = "ICICI_AUTODEBIT_BILL",
            regex = Regex(
                """Total\s+Amount\s+Due\s+on\s+ICICI\s+Bank\s+Credit\s+Card\s+XX(\d{4})\s+is\s+INR\s+([0-9,]+\.?\d*).*?on\s+or\s+before\s+(\d{2}-\w{3}-\d{2,4})""",
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

            // Axis: Payment of INR X for Axis Bank Credit Card no. XX5519 is due on DD-MM-YY with minimum amount due of INR Y
            "AXIS_BILL" -> ParsedBill(
                cardLastFour = match.groupValues[2],
                bankName = "AXIS",
                totalDue = SmsParser.parseAmount(match.groupValues[1]),
                minimumDue = SmsParser.parseAmount(match.groupValues[4]),
                dueDate = parseBillDate(match.groupValues[3])
            )

            // ICICI Statement Email: Total of Rs X or minimum of Rs Y is due by DD-MMM-YY
            "ICICI_STATEMENT_EMAIL" -> ParsedBill(
                cardLastFour = match.groupValues[1],
                bankName = "ICICI",
                totalDue = SmsParser.parseAmount(match.groupValues[2]),
                minimumDue = SmsParser.parseAmount(match.groupValues[3]),
                dueDate = parseBillDate(match.groupValues[4])
            )

            // ICICI Auto-debit: Total Amount Due on ICICI Bank Credit Card XX2000 is INR X...on or before DD-MMM-YY
            "ICICI_AUTODEBIT_BILL" -> ParsedBill(
                cardLastFour = match.groupValues[1],
                bankName = "ICICI",
                totalDue = SmsParser.parseAmount(match.groupValues[2]),
                minimumDue = null,  // Not provided in this format
                dueDate = parseBillDate(match.groupValues[3])
            )

            else -> throw IllegalArgumentException("Unknown pattern: ${pattern.name}")
        }
    }

    private fun parseBillDate(dateStr: String): LocalDate {
        // Normalize the date string to handle uppercase months (e.g., "JAN" -> "Jan")
        val normalizedDate = normalizeDateString(dateStr.trim())

        val patterns = listOf(
            "dd-MMM-yy",
            "dd-MMM-yyyy",
            "dd-MM-yy",
            "dd-MM-yyyy"
        )

        for (pattern in patterns) {
            try {
                val formatter = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)
                return LocalDate.parse(normalizedDate, formatter)
            } catch (e: Exception) {
                // Try next pattern
            }
        }

        // Default to 30 days from now if parsing fails
        return LocalDate.now().plusDays(30)
    }

    /**
     * Normalize date string to handle uppercase month abbreviations.
     * Converts "05-JAN-26" to "05-Jan-26" for proper parsing.
     */
    private fun normalizeDateString(dateStr: String): String {
        // Match patterns like "05-JAN-26" or "05-JANUARY-26"
        val regex = Regex("""(\d{2})-([A-Za-z]+)-(\d{2,4})""")
        return regex.replace(dateStr) { match ->
            val day = match.groupValues[1]
            val month = match.groupValues[2].lowercase().replaceFirstChar { it.uppercase() }
            val year = match.groupValues[3]
            "$day-$month-$year"
        }
    }
}
