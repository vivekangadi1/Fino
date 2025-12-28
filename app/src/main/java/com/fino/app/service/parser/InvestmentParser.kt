package com.fino.app.service.parser

import com.fino.app.domain.model.TransactionType

/**
 * Parser for investment-related SMS messages.
 * Handles NPS contributions, SIP debits, and Mutual Fund purchases.
 */
class InvestmentParser {

    private val patterns = listOf(
        // NPS contribution: "Rs.5000 debited from A/c XX1234 towards NPS contribution. PRAN: 1234567890 -ICICI Bank"
        ParsePattern(
            name = "NPS_CONTRIBUTION",
            regex = Regex(
                """Rs\.?([0-9,]+\.?\d*)\s+debited\s+from\s+A/c\s+XX(\d+)\s+towards\s+NPS\s+contribution""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f,
            isSubscription = true
        ),

        // SIP debit: "Rs.2500 debited from A/c XX5678 towards SIP for HDFC Balanced Advantage Fund on 15-Dec-25 -HDFC Bank"
        ParsePattern(
            name = "SIP_DEBIT",
            regex = Regex(
                """Rs\.?([0-9,]+\.?\d*)\s+debited\s+from\s+A/c\s+XX(\d+)\s+towards\s+SIP\s+for\s+(.+?)\s+on\s+(\d{2}-\w{3}-\d{2,4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f,
            isSubscription = true
        ),

        // Mutual Fund purchase: "Rs.10000 debited from A/c XX9012 for Mutual Fund Purchase - Axis Bluechip Fund. Ref: MF123456 -Axis Bank"
        ParsePattern(
            name = "MUTUAL_FUND",
            regex = Regex(
                """Rs\.?([0-9,]+\.?\d*)\s+debited\s+from\s+A/c\s+XX(\d+)\s+for\s+Mutual\s+Fund\s+Purchase\s*-?\s*(.+?)\.?\s+Ref""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // Generic investment: "debited...towards...investment/invested"
        ParsePattern(
            name = "GENERIC_INVESTMENT",
            regex = Regex(
                """Rs\.?([0-9,]+\.?\d*)\s+(?:debited|invested)\s+from\s+A/c\s+XX(\d+)\s+(?:towards|for)\s+(.+?)(?:\.|$)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.8f
        )
    )

    fun parse(smsBody: String): ParsedTransaction? {
        for (pattern in patterns) {
            val match = pattern.regex.find(smsBody)
            if (match != null) {
                return extractTransaction(match, pattern, smsBody)
            }
        }
        return null
    }

    private fun extractTransaction(match: MatchResult, pattern: ParsePattern, smsBody: String): ParsedTransaction {
        return when (pattern.name) {
            "NPS_CONTRIBUTION" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = "NPS Contribution",
                transactionDate = java.time.LocalDateTime.now(),
                accountLastFour = match.groupValues[2],
                bankName = extractBankName(smsBody),
                paymentChannel = PaymentChannel.INVESTMENT,
                isLikelySubscription = true,
                confidence = pattern.confidence
            )

            "SIP_DEBIT" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = "SIP - ${match.groupValues[3].trim()}",
                transactionDate = SmsParser.parseDate(match.groupValues[4]),
                accountLastFour = match.groupValues[2],
                bankName = extractBankName(smsBody),
                paymentChannel = PaymentChannel.INVESTMENT,
                isLikelySubscription = true,
                confidence = pattern.confidence
            )

            "MUTUAL_FUND" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = "MF - ${match.groupValues[3].trim()}",
                transactionDate = java.time.LocalDateTime.now(),
                accountLastFour = match.groupValues[2],
                bankName = extractBankName(smsBody),
                paymentChannel = PaymentChannel.INVESTMENT,
                confidence = pattern.confidence
            )

            "GENERIC_INVESTMENT" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[3].trim(),
                transactionDate = java.time.LocalDateTime.now(),
                accountLastFour = match.groupValues[2],
                bankName = extractBankName(smsBody),
                paymentChannel = PaymentChannel.INVESTMENT,
                confidence = pattern.confidence
            )

            else -> throw IllegalArgumentException("Unknown pattern: ${pattern.name}")
        }
    }

    private fun extractBankName(smsBody: String): String {
        val bankPatterns = listOf("ICICI", "HDFC", "SBI", "AXIS", "KOTAK", "BOB")
        val upperBody = smsBody.uppercase()
        return bankPatterns.firstOrNull { upperBody.contains(it) } ?: "Unknown"
    }
}
