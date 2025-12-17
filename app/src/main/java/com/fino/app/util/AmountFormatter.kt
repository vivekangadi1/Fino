package com.fino.app.util

import java.text.DecimalFormat

/**
 * Utility class for formatting currency amounts in Indian Rupee format.
 */
class AmountFormatter {

    private val standardFormat = DecimalFormat("#,##,##0.##")
    private val noDecimalFormat = DecimalFormat("#,##,##0")

    /**
     * Format amount with rupee symbol.
     * Examples: ₹350, ₹350.50, ₹1,234
     */
    fun format(amount: Double): String {
        val prefix = if (amount < 0) "-₹" else "₹"
        val absAmount = kotlin.math.abs(amount)

        return if (absAmount % 1.0 == 0.0) {
            "$prefix${noDecimalFormat.format(absAmount.toLong())}"
        } else {
            "$prefix${standardFormat.format(absAmount)}"
        }
    }

    /**
     * Format amount in compact notation.
     * Examples: ₹45.2K, ₹1.2L, ₹1.5Cr, ₹999
     */
    fun formatCompact(amount: Double): String {
        val absAmount = kotlin.math.abs(amount)
        val prefix = if (amount < 0) "-₹" else "₹"

        return when {
            absAmount >= 10_000_000 -> {
                // Crores (1 Cr = 10,000,000)
                val crores = absAmount / 10_000_000
                "$prefix${formatDecimal(crores)}Cr"
            }
            absAmount >= 100_000 -> {
                // Lakhs (1 L = 100,000)
                val lakhs = absAmount / 100_000
                "$prefix${formatDecimal(lakhs)}L"
            }
            absAmount >= 1_000 -> {
                // Thousands
                val thousands = absAmount / 1_000
                "$prefix${formatDecimal(thousands)}K"
            }
            else -> {
                // Under 1000, show full amount
                format(amount)
            }
        }
    }

    private fun formatDecimal(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            String.format("%.1f", value)
        }
    }

    companion object {
        private val instance = AmountFormatter()

        fun format(amount: Double): String = instance.format(amount)
        fun formatCompact(amount: Double): String = instance.formatCompact(amount)
    }
}
