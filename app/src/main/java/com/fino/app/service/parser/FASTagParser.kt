package com.fino.app.service.parser

import com.fino.app.domain.model.TransactionType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Parser for FASTag toll transaction SMS messages.
 * Handles ICICI, HDFC, Axis, SBI FASTag formats.
 * Toll transactions are categorized as Transport expenses.
 */
class FASTagParser {

    private val patterns = listOf(
        // ICICI FASTag: "Rs.75 paid at LinkRoadL1Toll for KA03NK9463 on 19-12-2025 20:21:54 with ICICI Bank FASTag"
        ParsePattern(
            name = "ICICI_FASTAG",
            regex = Regex(
                """Rs\.?([0-9,]+\.?\d*)\s+paid\s+at\s+(.+?)\s+for\s+(\w+)\s+on\s+(\d{2}-\d{2}-\d{4})\s+(\d{2}:\d{2}:\d{2})\s+with\s+ICICI\s+Bank\s+FASTag""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // ICICI FASTag alternate: "INR 75.00 deducted from FASTag linked to vehicle KA03NK9463 at TollPlaza on DD-MM-YYYY"
        ParsePattern(
            name = "ICICI_FASTAG_ALT",
            regex = Regex(
                """(?:INR|Rs\.?)\s*([0-9,]+\.?\d*)\s+deducted\s+from\s+FASTag\s+linked\s+to\s+vehicle\s+(\w+)\s+at\s+(.+?)\s+on\s+(\d{2}-\d{2}-\d{4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // HDFC FASTag: "Rs.45 toll charged at TollPlaza for vehicle MH12AB1234 on DD-MM-YY. HDFC Bank FASTag"
        ParsePattern(
            name = "HDFC_FASTAG",
            regex = Regex(
                """Rs\.?([0-9,]+\.?\d*)\s+toll\s+charged\s+at\s+(.+?)\s+for\s+vehicle\s+(\w+)\s+on\s+(\d{2}-\d{2}-\d{2,4}).*?HDFC\s+Bank\s+FASTag""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // Axis FASTag: "Toll of Rs.60 paid at TollPlaza for MH04CD5678 via Axis Bank FASTag on DD-MM-YY"
        ParsePattern(
            name = "AXIS_FASTAG",
            regex = Regex(
                """Toll\s+of\s+Rs\.?([0-9,]+\.?\d*)\s+paid\s+at\s+(.+?)\s+for\s+(\w+)\s+via\s+Axis\s+Bank\s+FASTag\s+on\s+(\d{2}-\d{2}-\d{2,4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // SBI FASTag: "SBI FASTag: Rs.50 deducted for vehicle KA01EF9012 at TollPlaza on DD-MM-YYYY"
        ParsePattern(
            name = "SBI_FASTAG",
            regex = Regex(
                """SBI\s+FASTag[:\s]+Rs\.?([0-9,]+\.?\d*)\s+deducted\s+for\s+vehicle\s+(\w+)\s+at\s+(.+?)\s+on\s+(\d{2}-\d{2}-\d{4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // Generic FASTag pattern
        ParsePattern(
            name = "GENERIC_FASTAG",
            regex = Regex(
                """Rs\.?([0-9,]+\.?\d*)\s+(?:paid|deducted|charged)\s+(?:at|for)\s+(.+?)\s+(?:for|via)\s+(?:vehicle\s+)?(\w+).*?FASTag.*?on\s+(\d{2}-\d{2}-\d{2,4})""",
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

    @Suppress("UNUSED_PARAMETER")
    private fun extractTransaction(match: MatchResult, pattern: ParsePattern, originalSms: String): ParsedTransaction {
        return when (pattern.name) {
            "ICICI_FASTAG" -> {
                // Has both date and time: "19-12-2025 20:21:54"
                val dateTimeStr = "${match.groupValues[4]} ${match.groupValues[5]}"
                val dateTime = parseFASTagDateTime(dateTimeStr)

                ParsedTransaction(
                    amount = SmsParser.parseAmount(match.groupValues[1]),
                    type = TransactionType.DEBIT,
                    merchantName = match.groupValues[2].trim(), // Toll plaza name
                    transactionDate = dateTime,
                    bankName = "ICICI",
                    paymentChannel = PaymentChannel.FASTAG,
                    tollName = match.groupValues[2].trim(),
                    vehicleNumber = match.groupValues[3].trim(),
                    confidence = pattern.confidence
                )
            }

            "ICICI_FASTAG_ALT" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[3].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[4]),
                bankName = "ICICI",
                paymentChannel = PaymentChannel.FASTAG,
                tollName = match.groupValues[3].trim(),
                vehicleNumber = match.groupValues[2].trim(),
                confidence = pattern.confidence
            )

            "HDFC_FASTAG" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[2].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[4]),
                bankName = "HDFC",
                paymentChannel = PaymentChannel.FASTAG,
                tollName = match.groupValues[2].trim(),
                vehicleNumber = match.groupValues[3].trim(),
                confidence = pattern.confidence
            )

            "AXIS_FASTAG" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[2].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[4]),
                bankName = "AXIS",
                paymentChannel = PaymentChannel.FASTAG,
                tollName = match.groupValues[2].trim(),
                vehicleNumber = match.groupValues[3].trim(),
                confidence = pattern.confidence
            )

            "SBI_FASTAG" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[3].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[4]),
                bankName = "SBI",
                paymentChannel = PaymentChannel.FASTAG,
                tollName = match.groupValues[3].trim(),
                vehicleNumber = match.groupValues[2].trim(),
                confidence = pattern.confidence
            )

            "GENERIC_FASTAG" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[2].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[4]),
                paymentChannel = PaymentChannel.FASTAG,
                tollName = match.groupValues[2].trim(),
                vehicleNumber = match.groupValues[3].trim(),
                confidence = pattern.confidence
            )

            else -> throw IllegalArgumentException("Unknown pattern: ${pattern.name}")
        }
    }

    /**
     * Parse FASTag datetime format: "19-12-2025 20:21:54"
     */
    private fun parseFASTagDateTime(dateTimeStr: String): LocalDateTime {
        return try {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH)
            LocalDateTime.parse(dateTimeStr, formatter)
        } catch (e: Exception) {
            // Fallback to just date
            try {
                SmsParser.parseDate(dateTimeStr.split(" ")[0])
            } catch (e2: Exception) {
                LocalDateTime.now()
            }
        }
    }
}
