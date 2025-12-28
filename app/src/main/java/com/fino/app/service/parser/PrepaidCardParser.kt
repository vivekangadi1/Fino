package com.fino.app.service.parser

import com.fino.app.domain.model.TransactionType

/**
 * Parser for Prepaid Card transaction SMS messages.
 * Handles ICICI Bank Prepaid Card formats.
 */
class PrepaidCardParser {

    private val patterns = listOf(
        // ICICI Prepaid Card: "Dear Customer, Rs 240.00 debited from ICICI Bank Prepaid Card 0880 on 21-Dec-25. Info- MERCHANT. The Available Balance is Rs 8260.00"
        ParsePattern(
            name = "ICICI_PREPAID",
            regex = Regex(
                """Rs\.?\s*([0-9,]+\.?\d*)\s+debited\s+from\s+ICICI\s+Bank\s+Prepaid\s+Card\s+(\d+)\s+on\s+(\d{2}-\w{3}-\d{2,4})\.?\s*Info-?\s*(.+?)\.?\s*(?:The\s+)?(?:Available|Avl)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // ICICI Prepaid alternate format without "Info-"
        ParsePattern(
            name = "ICICI_PREPAID_ALT",
            regex = Regex(
                """Rs\.?\s*([0-9,]+\.?\d*)\s+debited\s+from\s+ICICI\s+Bank\s+Prepaid\s+Card\s+(\d+)\s+on\s+(\d{2}-\w{3}-\d{2,4})\s+at\s+(.+?)\.?\s*(?:Available|Avl|Balance)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // Generic prepaid card pattern
        ParsePattern(
            name = "GENERIC_PREPAID",
            regex = Regex(
                """Rs\.?\s*([0-9,]+\.?\d*)\s+(?:debited|spent)\s+(?:from|using)\s+.*?Prepaid\s+Card.*?(\d{4})\s+(?:on|at)\s+(\d{2}-\w{3}-\d{2,4}).*?(?:at|for|Info-?)\s*(.+?)\.?\s*(?:Available|Avl|Balance|$)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.85f
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
            "ICICI_PREPAID", "ICICI_PREPAID_ALT" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[4].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[3]),
                cardLastFour = match.groupValues[2],
                bankName = "ICICI",
                paymentChannel = PaymentChannel.PREPAID_CARD,
                confidence = pattern.confidence
            )

            "GENERIC_PREPAID" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[4].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[3]),
                cardLastFour = match.groupValues[2],
                paymentChannel = PaymentChannel.PREPAID_CARD,
                confidence = pattern.confidence
            )

            else -> throw IllegalArgumentException("Unknown pattern: ${pattern.name}")
        }
    }
}
