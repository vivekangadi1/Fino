package com.fino.app.service.parser

import com.fino.app.domain.model.TransactionType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Main SMS parser that delegates to specific parser implementations.
 * Handles various Indian bank SMS formats for UPI, Credit Cards, and Bills.
 */
class SmsParser {

    private val upiParser = UpiTransactionParser()
    private val creditCardParser = CreditCardTransactionParser()
    private val billParser = CreditCardBillParser()

    /**
     * Parse an SMS body to extract transaction details.
     * Returns null if the SMS is not a valid transaction message.
     */
    fun parse(smsBody: String): ParsedTransaction? {
        // Skip non-transaction SMS
        if (isNonTransactionSms(smsBody)) {
            return null
        }

        // Try UPI parser first
        upiParser.parse(smsBody)?.let { return it }

        // Try Credit Card transaction parser
        creditCardParser.parse(smsBody)?.let { return it }

        return null
    }

    /**
     * Parse a credit card bill SMS.
     */
    fun parseBill(smsBody: String): ParsedBill? {
        return billParser.parse(smsBody)
    }

    /**
     * Check if SMS should be ignored (OTP, promo, balance, etc.)
     */
    private fun isNonTransactionSms(smsBody: String): Boolean {
        val lowerBody = smsBody.lowercase()

        val ignorePatterns = listOf(
            "otp",
            "one time password",
            "verification code",
            "cashback",
            "offer",
            "% off",
            "discount",
            "balance is rs",
            "account balance",
            "avl bal",
            "available balance",
            "fixed deposit",
            "fd matures",
            "reminder:",
            "payment is due",
            "thank you for shopping",
            "visit again"
        )

        return ignorePatterns.any { lowerBody.contains(it) }
    }

    companion object {
        /**
         * Parse amount string with Indian comma format.
         * Handles: "1,25,000.00", "350.00", "500", "Rs.350", "INR 500"
         */
        fun parseAmount(amountStr: String): Double {
            val cleaned = amountStr
                .replace(",", "")
                .replace("Rs.", "", ignoreCase = true)
                .replace("Rs", "", ignoreCase = true)
                .replace("INR", "", ignoreCase = true)
                .replace("₹", "")
                .trim()

            return cleaned.toDoubleOrNull() ?: 0.0
        }

        /**
         * Parse date from various formats.
         * Supports: DD-MM-YY, DD-MM-YYYY, DD/MM/YYYY, DD-Mon-YY
         */
        fun parseDate(dateStr: String): LocalDateTime {
            val patterns = listOf(
                "dd-MM-yy",
                "dd-MM-yyyy",
                "dd/MM/yyyy",
                "dd/MM/yy",
                "dd-MMM-yy",
                "dd-MMM-yyyy"
            )

            for (pattern in patterns) {
                try {
                    val formatter = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)
                    val date = LocalDate.parse(dateStr.trim(), formatter)
                    return date.atStartOfDay()
                } catch (e: Exception) {
                    // Try next pattern
                }
            }

            // Default to now if parsing fails
            return LocalDateTime.now()
        }
    }
}
