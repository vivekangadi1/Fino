package com.fino.app.service.parser

import com.fino.app.domain.model.TransactionType

/**
 * Parser for bank charge SMS messages.
 * Handles MAB (Minimum Average Balance) charges, AMB charges, and service fees.
 */
class BankChargeParser {

    private val patterns = listOf(
        // MAB charges: "Rs.590 debited from A/c XX1234 towards MAB charges for Oct 2025 -ICICI Bank"
        ParsePattern(
            name = "MAB_CHARGE",
            regex = Regex(
                """Rs\.?([0-9,]+\.?\d*)\s+debited\s+from\s+A/c\s+XX(\d+)\s+towards\s+MAB\s+charges""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // AMB non-maintenance: "Dear Customer, Rs.295 has been debited from A/c XX5678 for non-maintenance of AMB -HDFC Bank"
        ParsePattern(
            name = "AMB_CHARGE",
            regex = Regex(
                """Rs\.?([0-9,]+\.?\d*)\s+has\s+been\s+debited\s+from\s+A/c\s+XX(\d+)\s+for\s+non-maintenance\s+of\s+AMB""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // Service charges: "Rs.118 incl GST debited from A/c XX9012 towards Service Charges for Nov 2025 -SBI"
        ParsePattern(
            name = "SERVICE_CHARGE",
            regex = Regex(
                """Rs\.?([0-9,]+\.?\d*)\s+(?:incl\s+GST\s+)?debited\s+from\s+A/c\s+XX(\d+)\s+towards\s+Service\s+Charges""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // Generic bank charge: "debited...towards...charges"
        ParsePattern(
            name = "GENERIC_CHARGE",
            regex = Regex(
                """Rs\.?([0-9,]+\.?\d*)\s+(?:has\s+been\s+)?debited\s+from\s+A/c\s+XX(\d+)\s+towards\s+(.+?)\s+charges""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.85f
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
        val merchantName = when (pattern.name) {
            "MAB_CHARGE" -> "Bank Charge - MAB"
            "AMB_CHARGE" -> "Bank Charge - AMB"
            "SERVICE_CHARGE" -> "Bank Charge - Service"
            "GENERIC_CHARGE" -> "Bank Charge - ${match.groupValues.getOrNull(3)?.trim() ?: "Other"}"
            else -> "Bank Charge"
        }

        return ParsedTransaction(
            amount = SmsParser.parseAmount(match.groupValues[1]),
            type = TransactionType.DEBIT,
            merchantName = merchantName,
            transactionDate = java.time.LocalDateTime.now(),
            accountLastFour = match.groupValues[2],
            bankName = extractBankName(smsBody),
            paymentChannel = PaymentChannel.BANK_CHARGE,
            confidence = pattern.confidence
        )
    }

    private fun extractBankName(smsBody: String): String {
        val bankPatterns = listOf("ICICI", "HDFC", "SBI", "AXIS", "KOTAK", "BOB")
        val upperBody = smsBody.uppercase()
        return bankPatterns.firstOrNull { upperBody.contains(it) } ?: "Unknown"
    }
}
