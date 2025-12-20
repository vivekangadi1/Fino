package com.fino.app.domain.model

import java.time.YearMonth

/**
 * Payment method usage data for a specific month
 */
data class MonthlyPaymentMethodUsage(
    val yearMonth: YearMonth,
    val upiAmount: Double,
    val creditCardAmount: Double,
    val debitCardAmount: Double,
    val cashAmount: Double,
    val otherAmount: Double,
    val totalAmount: Double,
    val transactionCounts: Map<String, Int> = emptyMap()
)

/**
 * Trend data showing payment method preferences over time
 */
data class PaymentMethodTrend(
    val monthlyUsage: List<MonthlyPaymentMethodUsage>,
    val preferredMethod: String,
    val preferredMethodPercentage: Float,
    val trendDirection: Map<String, TrendDirection>
)

/**
 * Calculate payment method trend from transactions over multiple months
 */
fun calculatePaymentMethodTrend(
    transactions: List<Transaction>,
    periodCount: Int = 6
): PaymentMethodTrend {
    val currentMonth = YearMonth.now()
    val monthlyUsageList = mutableListOf<MonthlyPaymentMethodUsage>()

    // Group transactions by month and calculate payment method usage
    for (i in periodCount - 1 downTo 0) {
        val targetMonth = currentMonth.minusMonths(i.toLong())

        val monthTransactions = transactions.filter { transaction ->
            val txMonth = YearMonth.from(transaction.transactionDate)
            txMonth == targetMonth && transaction.type == TransactionType.DEBIT
        }

        var upiAmount = 0.0
        var creditCardAmount = 0.0
        var debitCardAmount = 0.0
        var cashAmount = 0.0
        var otherAmount = 0.0

        val transactionCounts = mutableMapOf<String, Int>()

        monthTransactions.forEach { transaction ->
            val amount = transaction.amount
            when (transaction.paymentMethod?.uppercase()) {
                "UPI" -> {
                    upiAmount += amount
                    transactionCounts["UPI"] = transactionCounts.getOrDefault("UPI", 0) + 1
                }
                "CREDIT_CARD", "CREDIT CARD" -> {
                    creditCardAmount += amount
                    transactionCounts["CREDIT_CARD"] = transactionCounts.getOrDefault("CREDIT_CARD", 0) + 1
                }
                "DEBIT_CARD", "DEBIT CARD" -> {
                    debitCardAmount += amount
                    transactionCounts["DEBIT_CARD"] = transactionCounts.getOrDefault("DEBIT_CARD", 0) + 1
                }
                "CASH" -> {
                    cashAmount += amount
                    transactionCounts["CASH"] = transactionCounts.getOrDefault("CASH", 0) + 1
                }
                else -> {
                    otherAmount += amount
                    transactionCounts["OTHER"] = transactionCounts.getOrDefault("OTHER", 0) + 1
                }
            }
        }

        val totalAmount = upiAmount + creditCardAmount + debitCardAmount + cashAmount + otherAmount

        monthlyUsageList.add(
            MonthlyPaymentMethodUsage(
                yearMonth = targetMonth,
                upiAmount = upiAmount,
                creditCardAmount = creditCardAmount,
                debitCardAmount = debitCardAmount,
                cashAmount = cashAmount,
                otherAmount = otherAmount,
                totalAmount = totalAmount,
                transactionCounts = transactionCounts
            )
        )
    }

    // Determine preferred payment method (overall)
    val totalUpi = monthlyUsageList.sumOf { it.upiAmount }
    val totalCreditCard = monthlyUsageList.sumOf { it.creditCardAmount }
    val totalDebitCard = monthlyUsageList.sumOf { it.debitCardAmount }
    val totalCash = monthlyUsageList.sumOf { it.cashAmount }
    val totalOther = monthlyUsageList.sumOf { it.otherAmount }
    val grandTotal = totalUpi + totalCreditCard + totalDebitCard + totalCash + totalOther

    val methodTotals = mapOf(
        "UPI" to totalUpi,
        "Credit Card" to totalCreditCard,
        "Debit Card" to totalDebitCard,
        "Cash" to totalCash,
        "Other" to totalOther
    )

    val preferredMethod = methodTotals.maxByOrNull { it.value }?.key ?: "Unknown"
    val preferredMethodPercentage = if (grandTotal > 0) {
        ((methodTotals[preferredMethod] ?: 0.0) / grandTotal * 100).toFloat()
    } else {
        0f
    }

    // Calculate trend direction for each payment method
    val trendDirection = mutableMapOf<String, TrendDirection>()

    if (monthlyUsageList.size >= 2) {
        val recentUsage = monthlyUsageList.last()
        val previousUsage = monthlyUsageList[monthlyUsageList.size - 2]

        // UPI trend
        val upiChange = calculatePercentageChange(recentUsage.upiAmount, previousUsage.upiAmount)
        trendDirection["UPI"] = determineTrendDirection(upiChange)

        // Credit Card trend
        val ccChange = calculatePercentageChange(recentUsage.creditCardAmount, previousUsage.creditCardAmount)
        trendDirection["Credit Card"] = determineTrendDirection(ccChange)

        // Debit Card trend
        val dcChange = calculatePercentageChange(recentUsage.debitCardAmount, previousUsage.debitCardAmount)
        trendDirection["Debit Card"] = determineTrendDirection(dcChange)

        // Cash trend
        val cashChange = calculatePercentageChange(recentUsage.cashAmount, previousUsage.cashAmount)
        trendDirection["Cash"] = determineTrendDirection(cashChange)
    }

    return PaymentMethodTrend(
        monthlyUsage = monthlyUsageList,
        preferredMethod = preferredMethod,
        preferredMethodPercentage = preferredMethodPercentage,
        trendDirection = trendDirection
    )
}
