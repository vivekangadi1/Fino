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
    private val prepaidCardParser = PrepaidCardParser()
    private val fastagParser = FASTagParser()
    private val bobParser = BankOfBarodaParser()
    private val billParser = CreditCardBillParser()
    private val insuranceParser = InsuranceParser()
    private val bankChargeParser = BankChargeParser()
    private val investmentParser = InvestmentParser()
    private val emiParser = EMIParser()

    /**
     * Parse an SMS body to extract transaction details.
     * Tries parsers in priority order - more specific parsers first.
     * Returns null if the SMS is not a valid transaction message.
     */
    fun parse(smsBody: String): ParsedTransaction? {
        // Skip non-transaction SMS
        if (isNonTransactionSms(smsBody)) {
            return null
        }

        // Try FASTag parser first (very specific format)
        fastagParser.parse(smsBody)?.let { return it }

        // Try Prepaid Card parser (specific format)
        prepaidCardParser.parse(smsBody)?.let { return it }

        // Try EMI parser (specific loan EMI patterns)
        emiParser.parse(smsBody)?.let { return it }

        // Try Insurance parser
        insuranceParser.parse(smsBody)?.let { return it }

        // Try Investment parser (NPS, SIP, Mutual Funds)
        investmentParser.parse(smsBody)?.let { return it }

        // Try Bank Charge parser (MAB, AMB, Service Charges)
        bankChargeParser.parse(smsBody)?.let { return it }

        // Try UPI parser
        upiParser.parse(smsBody)?.let { return it }

        // Try Credit Card transaction parser
        creditCardParser.parse(smsBody)?.let { return it }

        // Try Bank of Baroda parser (catches remaining BoB formats)
        bobParser.parse(smsBody)?.let { return it }

        return null
    }

    /**
     * Parse a credit card bill SMS.
     */
    fun parseBill(smsBody: String): ParsedBill? {
        return billParser.parse(smsBody)
    }

    /**
     * Check if SMS should be ignored (OTP, promo, balance-only, etc.)
     * Context-aware: allows transaction SMS that happen to include balance info.
     */
    private fun isNonTransactionSms(smsBody: String): Boolean {
        val lowerBody = smsBody.lowercase()

        // Hard ignore patterns - always skip these
        val hardIgnorePatterns = listOf(
            "otp",
            "one time password",
            "verification code",
            "% off",
            "discount",
            "fixed deposit",
            "fd matures",
            "reminder:",
            "payment is due",
            "thank you for shopping",
            "visit again",
            "has been declined",
            "could not be processed",
            "has failed",
            "is not enabled",
            "low funds",
            "low balance",
            "recharge your",
            "maintain sufficient"
        )

        if (hardIgnorePatterns.any { lowerBody.contains(it) }) {
            return true
        }

        // Check if this looks like a transaction
        val hasTransaction = lowerBody.contains("debited") ||
                             lowerBody.contains("spent") ||
                             lowerBody.contains("credited") ||
                             lowerBody.contains("paid at") ||
                             lowerBody.contains("transferred") ||
                             lowerBody.contains("sent rs")

        // Soft ignore patterns - only skip if NOT a transaction
        if (!hasTransaction) {
            val softIgnorePatterns = listOf(
                "cashback",
                "offer",
                "avl bal",
                "available balance",
                "balance is rs",
                "account balance",
                "your balance"
            )
            if (softIgnorePatterns.any { lowerBody.contains(it) }) {
                return true
            }
        }

        return false
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
