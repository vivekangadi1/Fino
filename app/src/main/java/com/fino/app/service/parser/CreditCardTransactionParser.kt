package com.fino.app.service.parser

import com.fino.app.domain.model.TransactionType

/**
 * Parser for Credit Card transaction SMS messages.
 * Handles HDFC, SBI, ICICI, Axis bank formats.
 */
class CreditCardTransactionParser {

    private val patterns = listOf(
        // ==================== ICICI NEW PATTERNS (Most common) ====================

        // ICICI CC Spent: "INR 625.00 spent using ICICI Bank Card XX2000 on 17-Dec-25 on SAMSUNG ELECTRO"
        ParsePattern(
            name = "ICICI_CC_SPENT_INR",
            regex = Regex(
                """INR\s+([0-9,]+\.?\d*)\s+spent\s+using\s+ICICI\s+Bank\s+Card\s+XX(\d+)\s+on\s+(\d{2}-\w{3}-\d{2,4})\s+on\s+(.+?)\.?\s*(?:Avl|If\s+not|$)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // ICICI CC USD: "USD 102.97 spent using ICICI Bank Card XX2000 on 20-Dec-25 on CLAUDE.AI SUBSC"
        ParsePattern(
            name = "ICICI_CC_SPENT_USD",
            regex = Regex(
                """USD\s+([0-9,]+\.?\d*)\s+spent\s+using\s+ICICI\s+Bank\s+Card\s+XX(\d+)\s+on\s+(\d{2}-\w{3}-\d{2,4})\s+on\s+(.+?)\.?\s*(?:Avl|If\s+not|$)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // ==================== AXIS NEW PATTERNS ====================

        // Axis CC Multi-line: "Spent INR 340\nAxis Bank Card no. XX5519\n20-12-25 15:19:56 IST\nMERCHANT"
        ParsePattern(
            name = "AXIS_CC_MULTILINE",
            regex = Regex(
                """Spent\s+INR\s+([0-9,]+\.?\d*)\s+Axis\s+Bank\s+Card\s+no\.\s*XX(\d+)\s+(\d{2}-\d{2}-\d{2})\s+\d{2}:\d{2}:\d{2}\s+IST\s+(.+?)\s+Avl\s+Limit""",
                setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
            ),
            confidence = 0.95f
        ),

        // ==================== HDFC PATTERNS ====================

        // HDFC: "HDFC Bank Credit Card XX4523 has been used for Rs.2340.00 at MERCHANT on DD-MM-YY at HH:MM:SS"
        ParsePattern(
            name = "HDFC_CC",
            regex = Regex(
                """HDFC\s+Bank\s+Credit\s+Card\s+XX(\d{4})\s+has\s+been\s+used\s+for\s+Rs\.?([0-9,]+\.?\d*)\s+at\s+(.+?)\s+on\s+(\d{2}-\d{2}-\d{2,4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // HDFC Alert: "Alert: Your HDFC Credit Card XX4523 has been used for Rs.649.00 at MERCHANT on DD-MM-YY"
        ParsePattern(
            name = "HDFC_CC_ALERT",
            regex = Regex(
                """HDFC\s+Credit\s+Card\s+XX(\d{4})\s+has\s+been\s+used\s+for\s+Rs\.?([0-9,]+\.?\d*)\s+at\s+(.+?)\s+on\s+(\d{2}-\d{2}-\d{2,4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // ==================== ICICI LEGACY PATTERNS ====================

        // ICICI: "Alert: ICICI Card ending 8976 used for INR 5550.00 at MERCHANT on DD-Mon-YY"
        ParsePattern(
            name = "ICICI_CC",
            regex = Regex(
                """ICICI\s+Card\s+ending\s+(\d{4})\s+used\s+for\s+INR\s+([0-9,]+\.?\d*)\s+at\s+(.+?)\s+on\s+(\d{2}-\w{3}-\d{2,4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // ICICI alternate: "ICICI Credit Card XX8976 was used for Rs.1,299 at MERCHANT on DD/MM/YYYY"
        ParsePattern(
            name = "ICICI_CC_ALT",
            regex = Regex(
                """ICICI\s+Credit\s+Card\s+XX(\d{4})\s+was\s+used\s+for\s+Rs\.?([0-9,]+\.?\d*)\s+at\s+(.+?)\s+on\s+(\d{2}/\d{2}/\d{4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // ==================== SBI PATTERNS ====================

        // SBI: "Your SBI Card ending 3456 was used for Rs.649 at MERCHANT on DD/MM/YYYY"
        ParsePattern(
            name = "SBI_CC",
            regex = Regex(
                """SBI\s+Card\s+ending\s+(\d{4})\s+was\s+used\s+for\s+Rs\.?([0-9,]+\.?\d*)\s+at\s+(.+?)\s+on\s+(\d{2}/\d{2}/\d{4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // SBI txn: "SBI Card XX3456 txn of Rs.2,999 at MERCHANT on DD-Mon-YYYY"
        ParsePattern(
            name = "SBI_CC_TXN",
            regex = Regex(
                """SBI\s+Card\s+XX(\d{4})\s+txn\s+of\s+Rs\.?([0-9,]+\.?\d*)\s+at\s+(.+?)\s+on\s+(\d{2}-\w{3}-\d{4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // ==================== AXIS LEGACY PATTERNS ====================

        // Axis: "Your Axis Bank Credit Card ending 7890 was used for Rs.899 at MERCHANT on DD-Mon-YY"
        ParsePattern(
            name = "AXIS_CC",
            regex = Regex(
                """Axis\s+Bank\s+Credit\s+Card\s+ending\s+(\d{4})\s+was\s+used\s+for\s+Rs\.?([0-9,]+\.?\d*)\s+at\s+(.+?)\s+on\s+(\d{2}-\w{3}-\d{2,4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // Subscription patterns
        // Google Play: "Google Play charged Rs.129 to your card ending 4523 for YouTube Premium subscription"
        ParsePattern(
            name = "GOOGLE_PLAY",
            regex = Regex(
                """Google\s+Play\s+charged\s+Rs\.?([0-9,]+\.?\d*)\s+to\s+your\s+card\s+ending\s+(\d{4})\s+for\s+(.+?)(?:\s+subscription)?$""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f,
            isSubscription = true
        ),

        // Netflix: "Your Netflix subscription of Rs.649 has been renewed using card XX8976"
        ParsePattern(
            name = "NETFLIX",
            regex = Regex(
                """Netflix\s+subscription\s+of\s+Rs\.?([0-9,]+\.?\d*)\s+has\s+been\s+renewed.*?card\s+XX(\d{4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f,
            isSubscription = true
        ),

        // Amazon Prime: "Amazon Prime membership renewed. Rs.1499 charged to card ending 4523"
        ParsePattern(
            name = "AMAZON_PRIME",
            regex = Regex(
                """Amazon\s+Prime\s+membership\s+renewed.*?Rs\.?([0-9,]+\.?\d*)\s+charged\s+to\s+card\s+ending\s+(\d{4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f,
            isSubscription = true
        ),

        // Spotify: "Spotify Premium renewed. Rs.119 charged to card XX8976"
        ParsePattern(
            name = "SPOTIFY",
            regex = Regex(
                """Spotify\s+Premium\s+renewed.*?Rs\.?([0-9,]+\.?\d*)\s+charged\s+to\s+card\s+XX(\d{4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f,
            isSubscription = true
        ),

        // ==================== ICICI STANDING INSTRUCTIONS ====================

        // ICICI CC Standing Instruction processed (USD): "we have successfully processed the payment of USD 4.72 for OpenAILLC, as per the Standing Instruction XohYoDZtIk, on 05/11/2025 for your ICICI Bank Credit Card 2000"
        ParsePattern(
            name = "ICICI_CC_STANDING_USD",
            regex = Regex(
                """successfully\s+processed\s+the\s+payment\s+of\s+USD\s+([0-9,]+\.?\d*)\s+for\s+(.+?),\s+as\s+per\s+the\s+Standing\s+Instruction\s+\w+,\s+on\s+(\d{2}/\d{2}/\d{4})\s+for\s+your\s+ICICI\s+Bank\s+Credit\s+Card\s+(\d{4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f,
            isSubscription = true
        ),

        // ICICI CC Standing Instruction processed (INR): "we have successfully processed the payment of INR 672.60 for Amazon, as per the Standing Instruction XoUcHa7LF0"
        ParsePattern(
            name = "ICICI_CC_STANDING_INR",
            regex = Regex(
                """successfully\s+processed\s+the\s+payment\s+of\s+INR\s+([0-9,]+\.?\d*)\s+for\s+(.+?),\s+as\s+per\s+the\s+Standing\s+Instruction\s+\w+,\s+on\s+(\d{2}/\d{2}/\d{4})\s+for\s+your\s+ICICI\s+Bank\s+Credit\s+Card\s+(\d{4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f,
            isSubscription = true
        ),

        // ==================== AXIS AUTOPAY ====================

        // Axis CC AutoPay notification: "INR 2060.28 for Airtel will be auto debited via Axis Bank Credit Card no. XX5519 by 07-12-25"
        // Note: This is a notification, not actual debit - skip for now

        // Axis CC AutoPay actual debit would come as regular Axis CC spent notification
        // which is already handled by AXIS_CC_MULTILINE pattern
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
            "HDFC_CC", "HDFC_CC_ALERT" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[2]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[3].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[4]),
                cardLastFour = match.groupValues[1],
                bankName = "HDFC",
                confidence = pattern.confidence
            )

            "ICICI_CC", "ICICI_CC_ALT" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[2]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[3].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[4]),
                cardLastFour = match.groupValues[1],
                bankName = "ICICI",
                confidence = pattern.confidence
            )

            "SBI_CC", "SBI_CC_TXN" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[2]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[3].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[4]),
                cardLastFour = match.groupValues[1],
                bankName = "SBI",
                confidence = pattern.confidence
            )

            "AXIS_CC" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[2]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[3].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[4]),
                cardLastFour = match.groupValues[1],
                bankName = "AXIS",
                paymentChannel = PaymentChannel.CREDIT_CARD,
                confidence = pattern.confidence
            )

            // New ICICI CC patterns
            "ICICI_CC_SPENT_INR" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[4].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[3]),
                cardLastFour = match.groupValues[2],
                bankName = "ICICI",
                currency = "INR",
                paymentChannel = PaymentChannel.CREDIT_CARD,
                confidence = pattern.confidence
            )

            "ICICI_CC_SPENT_USD" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[4].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[3]),
                cardLastFour = match.groupValues[2],
                bankName = "ICICI",
                currency = "USD",
                paymentChannel = PaymentChannel.CREDIT_CARD,
                confidence = pattern.confidence
            )

            // Axis CC Multi-line format
            "AXIS_CC_MULTILINE" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[4].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[3]),
                cardLastFour = match.groupValues[2],
                bankName = "AXIS",
                paymentChannel = PaymentChannel.CREDIT_CARD,
                confidence = pattern.confidence
            )

            "GOOGLE_PLAY" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[3].trim(),
                transactionDate = java.time.LocalDateTime.now(),
                cardLastFour = match.groupValues[2],
                isLikelySubscription = true,
                confidence = pattern.confidence
            )

            "NETFLIX" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = "Netflix",
                transactionDate = java.time.LocalDateTime.now(),
                cardLastFour = match.groupValues[2],
                isLikelySubscription = true,
                confidence = pattern.confidence
            )

            "AMAZON_PRIME" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = "Amazon Prime",
                transactionDate = java.time.LocalDateTime.now(),
                cardLastFour = match.groupValues[2],
                isLikelySubscription = true,
                confidence = pattern.confidence
            )

            "SPOTIFY" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = "Spotify Premium",
                transactionDate = java.time.LocalDateTime.now(),
                cardLastFour = match.groupValues[2],
                isLikelySubscription = true,
                confidence = pattern.confidence
            )

            // ICICI CC Standing Instructions (USD)
            "ICICI_CC_STANDING_USD" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[2].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[3]),
                cardLastFour = match.groupValues[4],
                bankName = "ICICI",
                currency = "USD",
                paymentChannel = PaymentChannel.STANDING_INSTRUCTION,
                isLikelySubscription = true,
                confidence = pattern.confidence
            )

            // ICICI CC Standing Instructions (INR)
            "ICICI_CC_STANDING_INR" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[2].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[3]),
                cardLastFour = match.groupValues[4],
                bankName = "ICICI",
                currency = "INR",
                paymentChannel = PaymentChannel.STANDING_INSTRUCTION,
                isLikelySubscription = true,
                confidence = pattern.confidence
            )

            else -> throw IllegalArgumentException("Unknown pattern: ${pattern.name}")
        }
    }
}
