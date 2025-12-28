package com.fino.app.service.parser

import com.fino.app.domain.model.TransactionType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Parser for Bank of Baroda transaction SMS messages.
 * Handles transfers, UPI payments, and credits.
 */
class BankOfBarodaParser {

    private val patterns = listOf(
        // BoB Transfer: "Rs.154 transferred from A/c ...7623 to:MIN BAL CHRGS FO. Total Bal Rs.1234"
        ParsePattern(
            name = "BOB_TRANSFER",
            regex = Regex(
                """Rs\.?([0-9,]+\.?\d*)\s+transferred\s+from\s+A/c\s+\.{3}(\d+)\s+to[:\s]*(.+?)\.?\s*(?:Total\s+Bal|Bal\s+Rs|$)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // BoB Debit: "Rs.500 debited from A/c ...7623 for UPI to merchant@upi. Ref: 123456"
        ParsePattern(
            name = "BOB_DEBIT",
            regex = Regex(
                """Rs\.?([0-9,]+\.?\d*)\s+debited\s+from\s+A/c\s+\.{3}(\d+)\s+(?:for\s+)?(?:UPI\s+)?(?:to\s+)?(.+?)\.?\s*(?:Ref[:\s]*(\d+)|Total\s+Bal|Bal|$)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // BoB Credit (Incoming): "A/c ...7623 credited with INR 10000.00 on 2024-12-15. UPI Ref No 123456"
        ParsePattern(
            name = "BOB_CREDIT",
            regex = Regex(
                """A/c\s+\.{3}(\d+)\s+credited\s+with\s+(?:INR|Rs\.?)\s*([0-9,]+\.?\d*)\s+on\s+(\d{4}-\d{2}-\d{2}).*?(?:UPI\s+Ref\s+No\s+(\d+)|from\s+(.+?)\.?|$)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // BoB Credit alternate: "INR 5000 credited to A/c ...7623 from SENDER on DD-MM-YYYY"
        ParsePattern(
            name = "BOB_CREDIT_ALT",
            regex = Regex(
                """(?:INR|Rs\.?)\s*([0-9,]+\.?\d*)\s+credited\s+to\s+A/c\s+\.{3}(\d+)\s+from\s+(.+?)\s+on\s+(\d{2}-\d{2}-\d{4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // BoB UPI: "Your A/c ...7623 is debited for Rs.350 on DD-MM-YY for UPI txn. Ref 123456"
        ParsePattern(
            name = "BOB_UPI",
            regex = Regex(
                """A/c\s+\.{3}(\d+)\s+is\s+debited\s+for\s+Rs\.?([0-9,]+\.?\d*)\s+on\s+(\d{2}-\d{2}-\d{2,4})\s+for\s+UPI\s+txn.*?Ref[:\s]*(\d+)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // BoB NEFT/IMPS: "Rs.25000 transferred via NEFT from A/c ...7623 to BENEFICIARY on DD-MM-YY. Ref: NEFT123"
        ParsePattern(
            name = "BOB_NEFT",
            regex = Regex(
                """Rs\.?([0-9,]+\.?\d*)\s+transferred\s+via\s+(NEFT|IMPS)\s+from\s+A/c\s+\.{3}(\d+)\s+to\s+(.+?)\s+on\s+(\d{2}-\d{2}-\d{2,4}).*?Ref[:\s]*(\w+)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // BoB UPI User Credit: "Dear BOB UPI User: Your account is credited with INR 13500.00 on 2025-12-04 07:43:27 AM by UPI Ref No 286515111106; AvlBal: Rs13500.00 - BOB"
        ParsePattern(
            name = "BOB_UPI_USER_CREDIT",
            regex = Regex(
                """Dear\s+BOB\s+UPI\s+User:\s+Your\s+account\s+is\s+credited\s+with\s+INR\s+([0-9,]+\.?\d*)\s+on\s+(\d{4}-\d{2}-\d{2}).*?UPI\s+Ref\s+No\s+(\d+)""",
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
            "BOB_TRANSFER" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[3].trim(),
                transactionDate = LocalDateTime.now(), // No date in this format
                accountLastFour = match.groupValues[2],
                bankName = "BOB",
                paymentChannel = PaymentChannel.UNKNOWN,
                confidence = pattern.confidence
            )

            "BOB_DEBIT" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[3].trim(),
                transactionDate = LocalDateTime.now(),
                reference = match.groupValues.getOrNull(4)?.takeIf { it.isNotBlank() },
                accountLastFour = match.groupValues[2],
                bankName = "BOB",
                paymentChannel = PaymentChannel.UPI,
                confidence = pattern.confidence
            )

            "BOB_CREDIT" -> {
                // Date format: 2024-12-15
                val date = parseIsoDate(match.groupValues[3])
                val sender = match.groupValues.getOrNull(5)?.takeIf { it.isNotBlank() }

                ParsedTransaction(
                    amount = SmsParser.parseAmount(match.groupValues[2]),
                    type = TransactionType.CREDIT,
                    merchantName = sender ?: "Bank Transfer",
                    transactionDate = date,
                    reference = match.groupValues.getOrNull(4)?.takeIf { it.isNotBlank() },
                    accountLastFour = match.groupValues[1],
                    bankName = "BOB",
                    paymentChannel = PaymentChannel.UPI,
                    senderName = sender,
                    confidence = pattern.confidence
                )
            }

            "BOB_CREDIT_ALT" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.CREDIT,
                merchantName = match.groupValues[3].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[4]),
                accountLastFour = match.groupValues[2],
                bankName = "BOB",
                paymentChannel = PaymentChannel.UPI,
                senderName = match.groupValues[3].trim(),
                confidence = pattern.confidence
            )

            "BOB_UPI" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[2]),
                type = TransactionType.DEBIT,
                merchantName = "UPI Payment",
                transactionDate = SmsParser.parseDate(match.groupValues[3]),
                reference = match.groupValues[4],
                accountLastFour = match.groupValues[1],
                bankName = "BOB",
                paymentChannel = PaymentChannel.UPI,
                confidence = pattern.confidence
            )

            "BOB_NEFT" -> {
                val paymentType = match.groupValues[2].uppercase()
                val channel = if (paymentType == "NEFT") PaymentChannel.NEFT else PaymentChannel.IMPS

                ParsedTransaction(
                    amount = SmsParser.parseAmount(match.groupValues[1]),
                    type = TransactionType.DEBIT,
                    merchantName = match.groupValues[4].trim(),
                    transactionDate = SmsParser.parseDate(match.groupValues[5]),
                    reference = match.groupValues[6],
                    accountLastFour = match.groupValues[3],
                    bankName = "BOB",
                    paymentChannel = channel,
                    confidence = pattern.confidence
                )
            }

            "BOB_UPI_USER_CREDIT" -> {
                val date = parseIsoDate(match.groupValues[2])
                ParsedTransaction(
                    amount = SmsParser.parseAmount(match.groupValues[1]),
                    type = TransactionType.CREDIT,
                    merchantName = "UPI Credit",
                    transactionDate = date,
                    reference = match.groupValues[3],
                    bankName = "BOB",
                    paymentChannel = PaymentChannel.UPI,
                    confidence = pattern.confidence
                )
            }

            else -> throw IllegalArgumentException("Unknown pattern: ${pattern.name}")
        }
    }

    /**
     * Parse ISO date format: "2024-12-15"
     */
    private fun parseIsoDate(dateStr: String): LocalDateTime {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
            LocalDate.parse(dateStr, formatter).atStartOfDay()
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }
}
