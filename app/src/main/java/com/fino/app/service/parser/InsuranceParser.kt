package com.fino.app.service.parser

import com.fino.app.domain.model.TransactionType

/**
 * Parser for insurance premium payment SMS messages.
 * Handles LIC, HDFC Life, ICICI Prudential, and other insurance providers.
 */
class InsuranceParser {

    private val patterns = listOf(
        // LIC Premium: "Dear Customer, Rs.12500 has been debited from your A/c XX1234 towards LIC Premium for Policy No.12345678. -ICICI Bank"
        ParsePattern(
            name = "LIC_PREMIUM",
            regex = Regex(
                """Rs\.?([0-9,]+\.?\d*)\s+has\s+been\s+debited\s+from\s+(?:your\s+)?A/c\s+XX(\d+)\s+towards\s+LIC\s+Premium""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f,
            isSubscription = true
        ),

        // HDFC Life Premium: "HDFC Life Premium of Rs.8000 debited from A/c XX5678 on 15-Dec-25. Policy: 987654321"
        ParsePattern(
            name = "HDFC_LIFE_PREMIUM",
            regex = Regex(
                """HDFC\s+Life\s+Premium\s+of\s+Rs\.?([0-9,]+\.?\d*)\s+debited\s+from\s+A/c\s+XX(\d+)\s+on\s+(\d{2}-\w{3}-\d{2,4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f,
            isSubscription = true
        ),

        // Insurance AutoPay: "Rs 5000.00 debited from ICICI Bank Savings Account XX494 on 10-Dec-25 towards ICICI Prudential Life for Insurance AutoPay"
        ParsePattern(
            name = "INSURANCE_AUTOPAY",
            regex = Regex(
                """Rs\.?\s*([0-9,]+\.?\d*)\s+debited\s+from\s+\w+\s+Bank\s+(?:Savings\s+)?Account\s+XX(\d+)\s+on\s+(\d{2}-\w{3}-\d{2,4})\s+towards\s+(.+?)\s+for\s+Insurance\s+AutoPay""",
                setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
            ),
            confidence = 0.95f,
            isSubscription = true
        ),

        // Generic insurance premium: "Insurance premium of Rs.X debited"
        ParsePattern(
            name = "GENERIC_INSURANCE",
            regex = Regex(
                """Insurance\s+(?:premium\s+)?(?:of\s+)?Rs\.?([0-9,]+\.?\d*)\s+debited""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.85f,
            isSubscription = true
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
            "LIC_PREMIUM" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = "LIC Premium",
                transactionDate = java.time.LocalDateTime.now(),
                accountLastFour = match.groupValues[2],
                bankName = extractBankName(smsBody),
                paymentChannel = PaymentChannel.INSURANCE,
                isLikelySubscription = true,
                confidence = pattern.confidence
            )

            "HDFC_LIFE_PREMIUM" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = "HDFC Life Premium",
                transactionDate = SmsParser.parseDate(match.groupValues[3]),
                accountLastFour = match.groupValues[2],
                bankName = "HDFC",
                paymentChannel = PaymentChannel.INSURANCE,
                isLikelySubscription = true,
                confidence = pattern.confidence
            )

            "INSURANCE_AUTOPAY" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[4].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[3]),
                accountLastFour = match.groupValues[2],
                bankName = extractBankName(smsBody),
                paymentChannel = PaymentChannel.INSURANCE,
                isLikelySubscription = true,
                confidence = pattern.confidence
            )

            "GENERIC_INSURANCE" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = "Insurance Premium",
                transactionDate = java.time.LocalDateTime.now(),
                bankName = extractBankName(smsBody),
                paymentChannel = PaymentChannel.INSURANCE,
                isLikelySubscription = true,
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
