package com.fino.app.service.parser

import com.fino.app.domain.model.TransactionType

/**
 * Parser for UPI and account transaction SMS messages.
 * Handles HDFC, SBI, ICICI, Axis bank formats including multi-line SMS.
 */
class UpiTransactionParser {

    private val patterns = listOf(
        // ==================== HDFC PATTERNS ====================

        // HDFC Multi-line: "Sent Rs.334.00\nFrom HDFC Bank A/C *9862\nTo MERCHANT\nOn 21/12/25\nRef 123456"
        ParsePattern(
            name = "HDFC_UPI_MULTILINE",
            regex = Regex(
                """Sent\s+Rs\.?([0-9,]+\.?\d*)\s+From\s+HDFC\s+Bank\s+A/C\s+\*(\d+)\s+To\s+(.+?)\s+On\s+(\d{2}/\d{2}/\d{2,4})\s+Ref\s+(\d+)""",
                setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
            ),
            confidence = 0.95f
        ),

        // HDFC Legacy: "Paid Rs.350.00 to MERCHANT on DD-MM-YY using UPI. UPI Ref: 123456. -HDFC Bank"
        ParsePattern(
            name = "HDFC_UPI",
            regex = Regex(
                """Paid\s+Rs\.?([0-9,]+\.?\d*)\s+to\s+(.+?)\s+on\s+(\d{2}-\d{2}-\d{2,4})\s+using\s+UPI.*?UPI\s+Ref[:\s]*(\d+)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // ==================== ICICI PATTERNS ====================

        // ICICI Account UPI: "ICICI Bank Acct XX494 debited for Rs 1534.00 on 20-Dec-25; MERCHANT credited. UPI:123456"
        ParsePattern(
            name = "ICICI_ACCOUNT_UPI",
            regex = Regex(
                """ICICI\s+Bank\s+Acct?\s+XX?(\d+)\s+debited\s+for\s+Rs\.?\s*([0-9,]+\.?\d*)\s+on\s+(\d{2}-\w{3}-\d{2,4});?\s*(.+?)\s+credited\.?\s*UPI[:\s]*(\d+)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // ICICI Autopay: "Rs 1499.00 debited from ICICI Bank Savings Account XX494 on 17-Dec-25 towards JioHotstar for Autopay AutoPay Retrieval Ref No.535196959911"
        // Also handles: "towards Google Play for GOOGLE AutoPay", "towards Amazon India for Amazon Prime AutoPay"
        ParsePattern(
            name = "ICICI_AUTOPAY",
            regex = Regex(
                """Rs\.?\s*([0-9,]+\.?\d*)\s+debited\s+from\s+ICICI\s+Bank\s+(?:Savings\s+)?Account\s+XX(\d+)\s+on\s+(\d{2}-\w{3}-\d{2,4})\s+towards\s+(.+?)\s+for\s+(?:Autopay|MERCHANTMANDATE|Create\s+Mandate|[A-Za-z]+(?:\s+[A-Za-z]+)*)\s*AutoPay""",
                setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
            ),
            confidence = 0.95f,
            isSubscription = true
        ),

        // ICICI Autopay successful debit: "Your account has been successfully debited with Rs 199.00 on 27-Nov-25 towards Google Play for GOOGLE AutoPay"
        // Also handles various vendor-specific autopay patterns
        ParsePattern(
            name = "ICICI_AUTOPAY_SUCCESS",
            regex = Regex(
                """Your\s+account\s+has\s+been\s+successfully\s+debited\s+with\s+Rs\.?\s*([0-9,]+\.?\d*)\s+on\s+(\d{2}-\w{3}-\d{2,4})\s+towards\s+(.+?)\s+for\s+(?:[A-Za-z]+(?:\s+[A-Za-z]+)*\s+)?AutoPay""",
                setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
            ),
            confidence = 0.95f,
            isSubscription = true
        ),

        // ICICI Bank Sweep to OD: "ICICI Bank Acc XX494 debited Rs. 11,629.00 on 24-Dec-25 InfoSweep to OD A.Avl Bal Rs. 6,402.79."
        ParsePattern(
            name = "ICICI_SWEEP",
            regex = Regex(
                """ICICI\s+Bank\s+Acc\s+XX(\d+)\s+debited\s+Rs\.?\s*([\d,]+\.?\d*)\s+on\s+(\d{2}-\w{3}-\d{2,4})\s+InfoSweep\s+to\s+OD""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // ICICI AutoPay Mandate Revocation: "Dear Customer, your AutoPay mandate is successfully revoked towards GOOGLE INDIA DI for Rs 199.00, RRN 182265865362-ICICI Bank."
        // Important for tracking cancelled recurring subscriptions
        ParsePattern(
            name = "ICICI_AUTOPAY_REVOCATION",
            regex = Regex(
                """your\s+AutoPay\s+mandate\s+is\s+successfully\s+revoked\s+towards\s+(.+?)\s+for\s+Rs\.?\s*([0-9,]+\.?\d*),?\s*RRN\s+(\d+)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f,
            isSubscription = true
        ),

        // Note: "Your account will be debited..." notifications are skipped - we only track actual debits

        // ICICI Credit (Incoming): "Acct XX494 is credited with Rs 10000.00 on 15-Dec-25 from SENDER. UPI:123456"
        ParsePattern(
            name = "ICICI_CREDIT",
            regex = Regex(
                """Acct?\s+XX?(\d+)\s+is\s+credited\s+with\s+Rs\.?\s*([0-9,]+\.?\d*)\s+on\s+(\d{2}-\w{3}-\d{2,4})\s+from\s+(.+?)\.?\s*UPI[:\s]*(\d+)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // ICICI Legacy: "INR 500.00 debited from A/c XX1234 on DD-MM-YY for UPI to merchant@ybl. Ref 123456"
        ParsePattern(
            name = "ICICI_UPI",
            regex = Regex(
                """INR\s+([0-9,]+\.?\d*)\s+debited\s+from\s+A/c\s+XX(\d+)\s+on\s+(\d{2}-\d{2}-\d{2,4})\s+for\s+UPI\s+to\s+(\S+).*?Ref[:\s]*(\d+)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // ICICI alternate: "Dear Customer, Rs.499 has been debited from your account **1234 for UPI txn to MERCHANT. Ref: 12345"
        ParsePattern(
            name = "ICICI_UPI_ALT",
            regex = Regex(
                """Rs\.?([0-9,]+\.?\d*)\s+has\s+been\s+debited.*?account\s+\*\*(\d+).*?UPI.*?to\s+(\S+).*?Ref[:\s]*(\d+)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.9f
        ),

        // ==================== SBI PATTERNS ====================

        // SBI: "Rs.1200 debited from A/c XX1234 to VPA merchant@upi on DD-MM-YY. UPI Ref 123456 -SBI"
        ParsePattern(
            name = "SBI_UPI",
            regex = Regex(
                """Rs\.?([0-9,]+\.?\d*)\s+debited\s+from\s+A/c\s+XX(\d+)\s+to\s+VPA\s+(\S+)\s+on\s+(\d{2}-\d{2}-\d{2,4}).*?UPI\s+Ref[:\s]*(\d+)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // Generic VPA with DD-Mon-YY date: "Rs.1890 debited from A/c XX1234 to VPA tatapower@ybl on 15-Dec-25 for ... UPI Ref 123456789 -ICICI"
        ParsePattern(
            name = "GENERIC_VPA_UPI",
            regex = Regex(
                """Rs\.?([0-9,]+\.?\d*)\s+debited\s+from\s+A/c\s+XX(\d+)\s+to\s+VPA\s+(\S+)\s+on\s+(\d{2}-\w{3}-\d{2,4}).*?UPI\s+Ref[:\s]*(\d+)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // ==================== AXIS PATTERNS ====================

        // Axis: "INR 899.00 debited from A/c no. XX4321 on DD-Mon-YY for UPI-MERCHANT. UPI Ref: 123456"
        ParsePattern(
            name = "AXIS_UPI",
            regex = Regex(
                """INR\s+([0-9,]+\.?\d*)\s+debited\s+from\s+A/c\s+no\.\s*XX(\d+)\s+on\s+(\d{2}-\w{3}-\d{2,4})\s+for\s+UPI-(.+?)\.?\s+UPI\s+Ref[:\s]*(\d+)""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f
        ),

        // Axis NACH/AutoPay: "INR 499.00 debited from A/c XX1234 via NACH for MERCHANT on DD-Mon-YY"
        ParsePattern(
            name = "AXIS_AUTOPAY",
            regex = Regex(
                """INR\s+([0-9,]+\.?\d*)\s+debited\s+from\s+A/c\s+(?:no\.\s*)?XX(\d+)\s+via\s+NACH\s+for\s+(.+?)\s+on\s+(\d{2}-\w{3}-\d{2,4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f,
            isSubscription = true
        ),

        // ==================== HDFC AUTOPAY PATTERNS ====================

        // HDFC AutoPay: "Auto Debit of Rs.499.00 from HDFC Bank A/C *1234 for MERCHANT on DD-MM-YY"
        ParsePattern(
            name = "HDFC_AUTOPAY",
            regex = Regex(
                """Auto\s+Debit\s+of\s+Rs\.?([0-9,]+\.?\d*)\s+from\s+HDFC\s+Bank\s+A/C\s+\*(\d+)\s+for\s+(.+?)\s+on\s+(\d{2}[-/]\d{2}[-/]\d{2,4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f,
            isSubscription = true
        ),

        // HDFC NACH/Mandate: "HDFC Bank A/c XX1234 debited Rs.999 for NACH/ECS debit towards MERCHANT"
        ParsePattern(
            name = "HDFC_NACH",
            regex = Regex(
                """HDFC\s+Bank\s+A/c\s+XX(\d+)\s+debited\s+Rs\.?([0-9,]+\.?\d*)\s+for\s+(?:NACH|ECS)\s+(?:debit\s+)?towards\s+(.+?)(?:\s+on\s+(\d{2}[-/]\d{2}[-/]\d{2,4}))?""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f,
            isSubscription = true
        ),

        // ==================== SBI AUTOPAY PATTERNS ====================

        // SBI AutoPay/NACH: "A/C debited Rs.599.00 towards MERCHANT Autopay on DD-MM-YY"
        ParsePattern(
            name = "SBI_AUTOPAY",
            regex = Regex(
                """A/C\s+(?:XX)?(\d+)?\s*debited\s+Rs\.?([0-9,]+\.?\d*)\s+towards\s+(.+?)\s+(?:Autopay|NACH|Mandate)\s+on\s+(\d{2}[-/]\d{2}[-/]\d{2,4})""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f,
            isSubscription = true
        ),

        // SBI Standing Instruction: "Rs 499.00 debited from SBI A/c XX1234 by Standing Instruction for MERCHANT"
        ParsePattern(
            name = "SBI_STANDING_INSTRUCTION",
            regex = Regex(
                """Rs\.?\s*([0-9,]+\.?\d*)\s+debited\s+from\s+SBI\s+A/c\s+XX(\d+)\s+by\s+Standing\s+Instruction\s+for\s+(.+?)(?:\s+on\s+(\d{2}[-/]\d{2}[-/]\d{2,4}))?""",
                RegexOption.IGNORE_CASE
            ),
            confidence = 0.95f,
            isSubscription = true
        ),

        // ==================== GENERIC AUTOPAY/MANDATE PATTERNS ====================

        // Generic NACH/Mandate debit: "Rs.299 debited via NACH/mandate/autopay for MERCHANT"
        ParsePattern(
            name = "GENERIC_MANDATE",
            regex = Regex(
                """Rs\.?\s*([0-9,]+\.?\d*)\s+debited\s+(?:from\s+(?:A/c|Account)\s+XX(\d+)\s+)?via\s+(?:NACH|mandate|autopay|standing\s+instruction)\s+(?:for|towards)\s+(.+?)(?:\s+on\s+(\d{2}[-/]\w{2,3}[-/]\d{2,4}))?""",
                setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
            ),
            confidence = 0.90f,
            isSubscription = true
        ),

        // Generic Subscription keyword: Matches any SMS with "subscription" and amount
        ParsePattern(
            name = "GENERIC_SUBSCRIPTION",
            regex = Regex(
                """Rs\.?\s*([0-9,]+\.?\d*)\s+(?:debited|charged)\s+(?:from\s+(?:A/c|Account)\s+XX(\d+)\s+)?for\s+(?:your\s+)?(.+?)\s+subscription""",
                setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
            ),
            confidence = 0.90f,
            isSubscription = true
        )
    )

    /**
     * Known subscription merchants for auto-confirmation of recurring patterns
     */
    companion object {
        val SUBSCRIPTION_MERCHANTS = listOf(
            // Streaming Services
            "netflix", "hotstar", "jiohotstar", "jio hotstar", "prime video", "amazon prime",
            "spotify", "youtube", "youtube premium", "disney", "disney+", "zee5", "sonyliv",
            "apple music", "apple tv", "apple one", "hbo", "hulu", "mubi", "voot", "altbalaji",
            "aha", "sun nxt", "hoichoi", "discovery+",

            // Cloud & Productivity
            "icloud", "google one", "google drive", "dropbox", "microsoft 365", "office 365",
            "adobe", "notion", "slack", "zoom", "grammarly", "canva", "figma", "github",
            "evernote", "todoist", "1password", "lastpass", "nordvpn", "expressvpn",

            // Gaming
            "playstation", "ps plus", "psn", "xbox", "xbox game pass", "nintendo", "steam",
            "epic games", "ubisoft", "ea play", "geforce now",

            // Fitness & Wellness
            "cult", "cure.fit", "cult.fit", "healthify", "healthifyme", "fittr", "nike",
            "peloton", "headspace", "calm",

            // News & Reading
            "times prime", "economic times", "the hindu", "hindu", "toi+", "mint",
            "audible", "kindle unlimited", "scribd", "blinkist", "medium",

            // Telecom & Utilities
            "jio", "airtel", "vi", "vodafone", "idea", "bsnl", "tata play", "dish tv",
            "sun direct", "d2h", "act fibernet", "hathway", "you broadband",

            // Food & Delivery
            "swiggy one", "swiggy super", "zomato pro", "zomato gold", "dunzo",

            // Finance & Banking
            "cred protect", "cred", "paytm first", "amazon pay later",

            // Education
            "coursera", "udemy", "skillshare", "linkedin learning", "masterclass", "unacademy",
            "byjus", "byju's", "vedantu", "upgrad"
        )

        /**
         * Check if a merchant name matches a known subscription service
         */
        fun isKnownSubscription(merchantName: String): Boolean {
            val normalizedName = merchantName.lowercase().trim()
            return SUBSCRIPTION_MERCHANTS.any { subscription ->
                normalizedName.contains(subscription) ||
                subscription.contains(normalizedName.take(5)) // Partial match for short names
            }
        }
    }

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
            // HDFC Multi-line format
            "HDFC_UPI_MULTILINE" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[3].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[4]),
                reference = match.groupValues[5],
                accountLastFour = match.groupValues[2],
                bankName = "HDFC",
                paymentChannel = PaymentChannel.UPI,
                confidence = pattern.confidence
            )

            "HDFC_UPI" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[2].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[3]),
                reference = match.groupValues[4],
                bankName = "HDFC",
                paymentChannel = PaymentChannel.UPI,
                confidence = pattern.confidence
            )

            // ICICI Account UPI
            "ICICI_ACCOUNT_UPI" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[2]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[4].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[3]),
                reference = match.groupValues[5],
                accountLastFour = match.groupValues[1],
                bankName = "ICICI",
                paymentChannel = PaymentChannel.UPI,
                confidence = pattern.confidence
            )

            // ICICI Autopay (subscription)
            "ICICI_AUTOPAY" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[4].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[3]),
                accountLastFour = match.groupValues[2],
                bankName = "ICICI",
                paymentChannel = PaymentChannel.AUTOPAY,
                isLikelySubscription = true,
                confidence = pattern.confidence
            )

            // ICICI Autopay successful debit
            "ICICI_AUTOPAY_SUCCESS" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[3].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[2]),
                bankName = "ICICI",
                paymentChannel = PaymentChannel.AUTOPAY,
                isLikelySubscription = true,
                confidence = pattern.confidence
            )

            // ICICI Bank Sweep to OD account
            "ICICI_SWEEP" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[2]),
                type = TransactionType.DEBIT,
                merchantName = "Sweep to OD Account",
                transactionDate = SmsParser.parseDate(match.groupValues[3]),
                accountLastFour = match.groupValues[1],
                bankName = "ICICI",
                paymentChannel = PaymentChannel.UNKNOWN,
                confidence = pattern.confidence
            )

            // ICICI AutoPay Mandate Revocation - tracks cancelled recurring subscriptions
            "ICICI_AUTOPAY_REVOCATION" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[2]),
                type = TransactionType.DEBIT,  // Mandate amount, not actual debit
                merchantName = match.groupValues[1].trim(),
                transactionDate = java.time.LocalDateTime.now(),
                reference = match.groupValues[3],
                bankName = "ICICI",
                paymentChannel = PaymentChannel.AUTOPAY,
                isLikelySubscription = true,
                isMandateRevocation = true,
                confidence = pattern.confidence
            )

            // ICICI Credit (incoming money)
            "ICICI_CREDIT" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[2]),
                type = TransactionType.CREDIT,
                merchantName = match.groupValues[4].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[3]),
                reference = match.groupValues[5],
                accountLastFour = match.groupValues[1],
                bankName = "ICICI",
                paymentChannel = PaymentChannel.UPI,
                senderName = match.groupValues[4].trim(),
                confidence = pattern.confidence
            )

            "ICICI_UPI" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[4].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[3]),
                reference = match.groupValues[5],
                accountLastFour = match.groupValues[2],
                bankName = "ICICI",
                paymentChannel = PaymentChannel.UPI,
                confidence = pattern.confidence
            )

            "ICICI_UPI_ALT" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[3].trim(),
                transactionDate = java.time.LocalDateTime.now(),
                reference = match.groupValues[4],
                accountLastFour = match.groupValues[2],
                bankName = "ICICI",
                paymentChannel = PaymentChannel.UPI,
                confidence = pattern.confidence
            )

            "SBI_UPI" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[3].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[4]),
                reference = match.groupValues[5],
                accountLastFour = match.groupValues[2],
                bankName = "SBI",
                paymentChannel = PaymentChannel.UPI,
                confidence = pattern.confidence
            )

            "GENERIC_VPA_UPI" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[3].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[4]),
                reference = match.groupValues[5],
                accountLastFour = match.groupValues[2],
                paymentChannel = PaymentChannel.UPI,
                confidence = pattern.confidence
            )

            "AXIS_UPI" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[4].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[3]),
                reference = match.groupValues[5],
                accountLastFour = match.groupValues[2],
                bankName = "AXIS",
                paymentChannel = PaymentChannel.UPI,
                confidence = pattern.confidence
            )

            // Axis NACH/AutoPay
            "AXIS_AUTOPAY" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[3].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[4]),
                accountLastFour = match.groupValues[2],
                bankName = "AXIS",
                paymentChannel = PaymentChannel.AUTOPAY,
                isLikelySubscription = true,
                confidence = pattern.confidence
            )

            // HDFC AutoPay
            "HDFC_AUTOPAY" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[3].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[4]),
                accountLastFour = match.groupValues[2],
                bankName = "HDFC",
                paymentChannel = PaymentChannel.AUTOPAY,
                isLikelySubscription = true,
                confidence = pattern.confidence
            )

            // HDFC NACH
            "HDFC_NACH" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[2]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[3].trim(),
                transactionDate = match.groupValues.getOrNull(4)?.let { SmsParser.parseDate(it) }
                    ?: java.time.LocalDateTime.now(),
                accountLastFour = match.groupValues[1],
                bankName = "HDFC",
                paymentChannel = PaymentChannel.AUTOPAY,
                isLikelySubscription = true,
                confidence = pattern.confidence
            )

            // SBI AutoPay/NACH
            "SBI_AUTOPAY" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[2]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[3].trim(),
                transactionDate = SmsParser.parseDate(match.groupValues[4]),
                accountLastFour = match.groupValues.getOrNull(1)?.takeIf { it.isNotEmpty() },
                bankName = "SBI",
                paymentChannel = PaymentChannel.AUTOPAY,
                isLikelySubscription = true,
                confidence = pattern.confidence
            )

            // SBI Standing Instruction
            "SBI_STANDING_INSTRUCTION" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[3].trim(),
                transactionDate = match.groupValues.getOrNull(4)?.let { SmsParser.parseDate(it) }
                    ?: java.time.LocalDateTime.now(),
                accountLastFour = match.groupValues[2],
                bankName = "SBI",
                paymentChannel = PaymentChannel.AUTOPAY,
                isLikelySubscription = true,
                confidence = pattern.confidence
            )

            // Generic NACH/Mandate
            "GENERIC_MANDATE" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[3].trim(),
                transactionDate = match.groupValues.getOrNull(4)?.let { SmsParser.parseDate(it) }
                    ?: java.time.LocalDateTime.now(),
                accountLastFour = match.groupValues.getOrNull(2)?.takeIf { it.isNotEmpty() },
                paymentChannel = PaymentChannel.AUTOPAY,
                isLikelySubscription = true,
                confidence = pattern.confidence
            )

            // Generic Subscription keyword
            "GENERIC_SUBSCRIPTION" -> ParsedTransaction(
                amount = SmsParser.parseAmount(match.groupValues[1]),
                type = TransactionType.DEBIT,
                merchantName = match.groupValues[3].trim(),
                transactionDate = java.time.LocalDateTime.now(),
                accountLastFour = match.groupValues.getOrNull(2)?.takeIf { it.isNotEmpty() },
                paymentChannel = PaymentChannel.UNKNOWN,
                isLikelySubscription = true,
                confidence = pattern.confidence
            )

            else -> throw IllegalArgumentException("Unknown pattern: ${pattern.name}")
        }
    }
}
