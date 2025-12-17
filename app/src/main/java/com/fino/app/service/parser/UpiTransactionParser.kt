package com.fino.app.service.parser

import com.fino.app.domain.model.TransactionType

/**
 * Parser for UPI transaction SMS messages.
 * Handles HDFC, SBI, ICICI, Axis bank formats.
 */
class UpiTransactionParser {

    private val patterns = listOf(
        // HDFC: "Paid Rs.350.00 to MERCHANT on DD-MM-YY using UPI. UPI Ref: 123456. -HDFC Bank"
        ParsePattern(
            name = "HDFC_UPI",
            regex = Regex(
                """Paid\s+Rs\.?([0-9,]+\.?\d*)\s+to\s+(.+?)\s+on\s+(\d{2}-\d{2}-\d{2,4})\s+using\s+UPI.*?UPI\s+Ref[:\s]*(\d+)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // SBI: "Rs.1200 debited from A/c XX1234 to VPA merchant@upi on DD-MM-YY. UPI Ref 123456 -SBI"
        ParsePattern(
            name = "SBI_UPI",
            regex = Regex(
                """Rs\.?([0-9,]+\.?\d*)\s+debited\s+from\s+A/c\s+XX(\d+)\s+to\s+VPA\s+(\S+)\s+on\s+(\d{2}-\d{2}-\d{2,4}).*?UPI\s+Ref[:\s]*(\d+)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // ICICI: "INR 500.00 debited from A/c XX1234 on DD-MM-YY for UPI to merchant@ybl. Ref 123456"
        ParsePattern(
            name = "ICICI_UPI",
            regex = Regex(
                """INR\s+([0-9,]+\.?\d*)\s+debited\s+from\s+A/c\s+XX(\d+)\s+on\s+(\d{2}-\d{2}-\d{2,4})\s+for\s+UPI\s+to\s+(\S+).*?Ref[:\s]*(\d+)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // ICICI alternate: "Dear Customer, Rs.499 has been debited from your account **1234 for UPI txn to MERCHANT. Ref: 12345"
        ParsePattern(
            name = "ICICI_UPI_ALT",
            regex = Regex(
                """Rs\.?([0-9,]+\.?\d*)\s+has\s+been\s+debited.*?account\s+\*\*(\d+).*?UPI.*?to\s+(\S+).*?Ref[:\s]*(\d+)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.9f
        ),

        // Axis: "INR 899.00 debited from A/c no. XX4321 on DD-Mon-YY for UPI-MERCHANT. UPI Ref: 123456"
        ParsePattern(
            name = "AXIS_UPI",
            regex = Regex(
                """INR\s+([0-9,]+\.?\d*)\s+debited\s+from\s+A/c\s+no\.\s*XX(\d+)\s+on\s+(\d{2}-\w{3}-\d{2,4})\s+for\s+UPI-(.+?)\.?\s+UPI\s+Ref[:\s]*(\d+)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        )
    )

    fun parse(smsBody: String): ParsedTransaction? {
        for (pattern in patterns) {
            val match = pattern.regex.find(smsBody)
            if (match != null) {
                return extractTransaction(match, pattern)
            }
        }
        return null
    }

    private fun extractTransaction(match: MatchResult, pattern: ParsePattern): ParsedTransaction {
        return when (pattern.name) {
            "HDFC_UPI" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[2].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[3]),
                reference = match.groupValues[4],
                bankName = "HDFC",
                confidence = pattern.confidence
            )

            "SBI_UPI" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[3].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[4]),
                reference = match.groupValues[5],
                accountLastFour = match.groupValues[2],
                bankName = "SBI",
                confidence = pattern.confidence
            )

            "ICICI_UPI" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[4].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[3]),
                reference = match.groupValues[5],
                accountLastFour = match.groupValues[2],
                bankName = "ICICI",
                confidence = pattern.confidence
            )

            "ICICI_UPI_ALT" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[3].trim(),
                transactionDate = java.time.LocalDateTime.now(),
                reference = match.groupValues[4],
                accountLastFour = match.groupValues[2],
                bankName = "ICICI",
                confidence = pattern.confidence
            )

            "AXIS_UPI" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[4].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[3]),
                reference = match.groupValues[5],
                accountLastFour = match.groupValues[2],
                bankName = "AXIS",
                confidence = pattern.confidence
            )

            else -> throw IllegalArgumentException("Unknown pattern: ${pattern.name}")
        }
    }
}
