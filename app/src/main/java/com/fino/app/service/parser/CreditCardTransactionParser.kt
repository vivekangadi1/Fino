package com.fino.app.service.parser

import com.fino.app.domain.model.TransactionType

/**
 * Parser for Credit Card transaction SMS messages.
 * Handles HDFC, SBI, ICICI, Axis bank formats.
 */
class CreditCardTransactionParser {

    private val patterns = listOf(
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

            else -> throw IllegalArgumentException("Unknown pattern: ${pattern.name}")
        }
    }
}
