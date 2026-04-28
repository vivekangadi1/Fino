package com.fino.app.service.parser

import com.fino.app.domain.model.CashbackReward
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Parses cashback-credited SMS from banks/credit-card issuers into CashbackReward records.
 * Call matches(body) first; matching bodies must NOT be routed through the standard
 * transaction parser (they are not debits/credits in the normal sense).
 */
object CashbackParser {

    private val CASHBACK_CREDIT_REGEX = Regex(
        "(?i)(cash\\s?back|reward\\s?points?)[^.]*?(credited|rewarded|earned|credited to your (account|card))",
        RegexOption.DOT_MATCHES_ALL
    )

    private val AMOUNT_REGEX = Regex(
        "(?i)(?:rs|inr|₹)\\.?\\s*([0-9]+(?:[.,][0-9]{1,2})?)",
        RegexOption.DOT_MATCHES_ALL
    )

    private val BANK_HINT_REGEX = Regex(
        "(?i)\\b(hdfc|icici|axis|sbi|kotak|amex|amazon\\s?pay|paytm|citi)\\b"
    )

    private val CARD_LAST_FOUR_REGEX = Regex("(?i)(?:card|ending|xx+)\\s*([0-9]{4})")

    fun matches(body: String): Boolean = CASHBACK_CREDIT_REGEX.containsMatchIn(body)

    /**
     * Extract a CashbackReward from the SMS body. Returns null if the amount cannot be parsed.
     */
    fun parse(body: String, receivedAt: LocalDateTime = LocalDateTime.now()): CashbackReward? {
        if (!matches(body)) return null
        val amount = AMOUNT_REGEX.find(body)
            ?.groupValues?.getOrNull(1)
            ?.replace(",", "")
            ?.toDoubleOrNull() ?: return null
        if (amount <= 0) return null

        val source = BANK_HINT_REGEX.find(body)?.value?.uppercase()?.trim() ?: "Card"
        val period = receivedAt.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val last4 = CARD_LAST_FOUR_REGEX.find(body)?.groupValues?.getOrNull(1)
        val description = buildString {
            append("Cashback credited")
            if (last4 != null) append(" on card ending $last4")
        }

        val creditedAt = receivedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        return CashbackReward(
            amount = amount,
            period = period,
            creditedAt = creditedAt,
            source = source,
            description = description
        )
    }
}
