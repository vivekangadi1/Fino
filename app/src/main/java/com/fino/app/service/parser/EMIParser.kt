package com.fino.app.service.parser

import com.fino.app.domain.model.TransactionType

/**
 * Parser for EMI (Equated Monthly Installment) SMS messages.
 * Handles home loans, car loans, personal loans, and education loans.
 */
class EMIParser {

    private val patterns = listOf(
        // Home Loan EMI: "EMI of Rs.25000 for Home Loan A/c 1234567890 debited from A/c XX1234 on 05-Dec-25 -HDFC Bank"
        ParsePattern(
            name = "HOME_LOAN_EMI",
            regex = Regex(
                """EMI\s+of\s+Rs\.?([0-9,]+\.?\d*)\s+for\s+Home\s+Loan\s+A/c\s+\d+\s+debited\s+from\s+A/c\s+XX(\d+)\s+on\s+(\d{2}-\w{3}-\d{2,4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f,
            isSubscription = true
        ),

        // Car Loan EMI: "Car Loan EMI Rs.15000 auto-debited from A/c XX5678 on 10-Dec-25. Loan A/c: 9876543210 -ICICI Bank"
        ParsePattern(
            name = "CAR_LOAN_EMI",
            regex = Regex(
                """Car\s+Loan\s+EMI\s+Rs\.?([0-9,]+\.?\d*)\s+auto-debited\s+from\s+A/c\s+XX(\d+)\s+on\s+(\d{2}-\w{3}-\d{2,4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f,
            isSubscription = true
        ),

        // Personal Loan EMI: "Personal Loan EMI of Rs.8500 paid from A/c XX9012 on 15-Dec-25. Outstanding: Rs.1,50,000 -SBI"
        ParsePattern(
            name = "PERSONAL_LOAN_EMI",
            regex = Regex(
                """Personal\s+Loan\s+EMI\s+(?:of\s+)?Rs\.?([0-9,]+\.?\d*)\s+paid\s+from\s+A/c\s+XX(\d+)\s+on\s+(\d{2}-\w{3}-\d{2,4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f,
            isSubscription = true
        ),

        // Education Loan EMI: "Education Loan EMI Rs.12000 debited from A/c XX3456 on 20-Dec-25. Ref: EL2024001 -Axis Bank"
        ParsePattern(
            name = "EDUCATION_LOAN_EMI",
            regex = Regex(
                """Education\s+Loan\s+EMI\s+Rs\.?([0-9,]+\.?\d*)\s+debited\s+from\s+A/c\s+XX(\d+)\s+on\s+(\d{2}-\w{3}-\d{2,4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f,
            isSubscription = true
        ),

        // Generic EMI pattern: "EMI of Rs.X debited" or "EMI Rs.X debited"
        ParsePattern(
            name = "GENERIC_EMI",
            regex = Regex(
                """(?:(\w+)\s+)?(?:Loan\s+)?EMI\s+(?:of\s+)?Rs\.?([0-9,]+\.?\d*)\s+(?:auto-)?debited\s+from\s+A/c\s+XX(\d+)(?:\s+on\s+(\d{2}-\w{3}-\d{2,4}))?""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.9f,
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
            "HOME_LOAN_EMI" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = "Home Loan EMI",
                transactionDate = SmsParser.parseDate(match.groupValues[3]),
                accountLastFour = match.groupValues[2],
                bankName = extractBankName(smsBody),
                paymentChannel = PaymentChannel.EMI,
                isLikelySubscription = true,
                confidence = pattern.confidence
            )

            "CAR_LOAN_EMI" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = "Car Loan EMI",
                transactionDate = SmsParser.parseDate(match.groupValues[3]),
                accountLastFour = match.groupValues[2],
                bankName = extractBankName(smsBody),
                paymentChannel = PaymentChannel.EMI,
                isLikelySubscription = true,
                confidence = pattern.confidence
            )

            "PERSONAL_LOAN_EMI" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = "Personal Loan EMI",
                transactionDate = SmsParser.parseDate(match.groupValues[3]),
                accountLastFour = match.groupValues[2],
                bankName = extractBankName(smsBody),
                paymentChannel = PaymentChannel.EMI,
                isLikelySubscription = true,
                confidence = pattern.confidence
            )

            "EDUCATION_LOAN_EMI" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = "Education Loan EMI",
                transactionDate = SmsParser.parseDate(match.groupValues[3]),
                accountLastFour = match.groupValues[2],
                bankName = extractBankName(smsBody),
                paymentChannel = PaymentChannel.EMI,
                isLikelySubscription = true,
                confidence = pattern.confidence
            )

            "GENERIC_EMI" -> {
                val loanType = match.groupValues[1].takeIf { it.isNotEmpty() } ?: "Loan"
                val dateStr = match.groupValues.getOrNull(4)?.takeIf { it.isNotEmpty() }

                ParsedTransaction(
                    amount = SmsParser.parseAmount(match.groupValues[2]),
                    type = TransactionType.DEBIT,
                    merchantName = "$loanType EMI",
                    transactionDate = dateStr?.let { SmsParser.parseDate(it) } ?: java.time.LocalDateTime.now(),
                    accountLastFour = match.groupValues[3],
                    bankName = extractBankName(smsBody),
                    paymentChannel = PaymentChannel.EMI,
                    isLikelySubscription = true,
                    confidence = pattern.confidence
                )
            }

            else -> throw IllegalArgumentException("Unknown pattern: ${pattern.name}")
        }
    }

    private fun extractBankName(smsBody: String): String {
        val bankPatterns = listOf("ICICI", "HDFC", "SBI", "AXIS", "KOTAK", "BOB")
        val upperBody = smsBody.uppercase()
        return bankPatterns.firstOrNull { upperBody.contains(it) } ?: "Unknown"
    }
}
