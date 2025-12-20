package com.fino.app.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime
import java.time.YearMonth

class PaymentMethodTrendTest {

    private val januaryTransactions = listOf(
        Transaction(
            id = 1L,
            amount = 5000.0,
            type = TransactionType.DEBIT,
            merchantName = "Swiggy",
            transactionDate = LocalDateTime.of(2024, 1, 15, 14, 30),
            source = TransactionSource.SMS,
            paymentMethod = "UPI"
        ),
        Transaction(
            id = 2L,
            amount = 3000.0,
            type = TransactionType.DEBIT,
            merchantName = "Amazon",
            transactionDate = LocalDateTime.of(2024, 1, 20, 10, 15),
            source = TransactionSource.SMS,
            paymentMethod = "CREDIT_CARD"
        ),
        Transaction(
            id = 3L,
            amount = 2000.0,
            type = TransactionType.DEBIT,
            merchantName = "Store",
            transactionDate = LocalDateTime.of(2024, 1, 25, 16, 45),
            source = TransactionSource.MANUAL,
            paymentMethod = "CASH"
        )
    )

    private val februaryTransactions = listOf(
        Transaction(
            id = 4L,
            amount = 6000.0,
            type = TransactionType.DEBIT,
            merchantName = "Zomato",
            transactionDate = LocalDateTime.of(2024, 2, 10, 19, 0),
            source = TransactionSource.SMS,
            paymentMethod = "UPI"
        ),
        Transaction(
            id = 5L,
            amount = 4000.0,
            type = TransactionType.DEBIT,
            merchantName = "Flipkart",
            transactionDate = LocalDateTime.of(2024, 2, 18, 11, 30),
            source = TransactionSource.SMS,
            paymentMethod = "CREDIT_CARD"
        ),
        Transaction(
            id = 6L,
            amount = 1500.0,
            type = TransactionType.DEBIT,
            merchantName = "ATM",
            transactionDate = LocalDateTime.of(2024, 2, 22, 14, 0),
            source = TransactionSource.MANUAL,
            paymentMethod = "DEBIT_CARD"
        )
    )

    @Test
    fun `calculatePaymentMethodTrend groups transactions by month correctly`() {
        val allTransactions = januaryTransactions + februaryTransactions
        val trend = calculatePaymentMethodTrend(allTransactions, periodCount = 2)

        assertEquals(2, trend.monthlyUsage.size)
    }

    @Test
    fun `calculatePaymentMethodTrend calculates UPI amount correctly`() {
        val allTransactions = januaryTransactions + februaryTransactions
        val trend = calculatePaymentMethodTrend(allTransactions, periodCount = 2)

        // January: 5000 UPI
        val januaryUsage = trend.monthlyUsage.find { it.yearMonth == YearMonth.of(2024, 1) }
        assertNotNull(januaryUsage)
        assertEquals(5000.0, januaryUsage!!.upiAmount, 0.01)

        // February: 6000 UPI
        val februaryUsage = trend.monthlyUsage.find { it.yearMonth == YearMonth.of(2024, 2) }
        assertNotNull(februaryUsage)
        assertEquals(6000.0, februaryUsage!!.upiAmount, 0.01)
    }

    @Test
    fun `calculatePaymentMethodTrend calculates credit card amount correctly`() {
        val allTransactions = januaryTransactions + februaryTransactions
        val trend = calculatePaymentMethodTrend(allTransactions, periodCount = 2)

        val januaryUsage = trend.monthlyUsage.find { it.yearMonth == YearMonth.of(2024, 1) }
        assertEquals(3000.0, januaryUsage!!.creditCardAmount, 0.01)

        val februaryUsage = trend.monthlyUsage.find { it.yearMonth == YearMonth.of(2024, 2) }
        assertEquals(4000.0, februaryUsage!!.creditCardAmount, 0.01)
    }

    @Test
    fun `calculatePaymentMethodTrend identifies preferred payment method`() {
        val allTransactions = januaryTransactions + februaryTransactions
        val trend = calculatePaymentMethodTrend(allTransactions, periodCount = 2)

        // Total UPI: 5000 + 6000 = 11000
        // Total Credit Card: 3000 + 4000 = 7000
        // UPI should be preferred
        assertEquals("UPI", trend.preferredMethod)
    }

    @Test
    fun `calculatePaymentMethodTrend calculates preferred method percentage`() {
        val allTransactions = januaryTransactions + februaryTransactions
        val trend = calculatePaymentMethodTrend(allTransactions, periodCount = 2)

        // Total: 11000 + 7000 + 2000 + 1500 = 21500
        // UPI: 11000 / 21500 * 100 = 51.16%
        assertEquals(51.16f, trend.preferredMethodPercentage, 0.1f)
    }

    @Test
    fun `calculatePaymentMethodTrend calculates trend direction for increasing UPI usage`() {
        val allTransactions = januaryTransactions + februaryTransactions
        val trend = calculatePaymentMethodTrend(allTransactions, periodCount = 2)

        // UPI increased from 5000 to 6000 (20% increase)
        assertEquals(TrendDirection.INCREASING, trend.trendDirection["UPI"])
    }

    @Test
    fun `calculatePaymentMethodTrend handles cash transactions`() {
        val allTransactions = januaryTransactions + februaryTransactions
        val trend = calculatePaymentMethodTrend(allTransactions, periodCount = 2)

        val januaryUsage = trend.monthlyUsage.find { it.yearMonth == YearMonth.of(2024, 1) }
        assertEquals(2000.0, januaryUsage!!.cashAmount, 0.01)
    }

    @Test
    fun `calculatePaymentMethodTrend handles debit card transactions`() {
        val allTransactions = januaryTransactions + februaryTransactions
        val trend = calculatePaymentMethodTrend(allTransactions, periodCount = 2)

        val februaryUsage = trend.monthlyUsage.find { it.yearMonth == YearMonth.of(2024, 2) }
        assertEquals(1500.0, februaryUsage!!.debitCardAmount, 0.01)
    }

    @Test
    fun `calculatePaymentMethodTrend counts transactions by payment method`() {
        val allTransactions = januaryTransactions + februaryTransactions
        val trend = calculatePaymentMethodTrend(allTransactions, periodCount = 2)

        val januaryUsage = trend.monthlyUsage.find { it.yearMonth == YearMonth.of(2024, 1) }
        assertEquals(1, januaryUsage!!.transactionCounts["UPI"])
        assertEquals(1, januaryUsage.transactionCounts["CREDIT_CARD"])
        assertEquals(1, januaryUsage.transactionCounts["CASH"])
    }

    @Test
    fun `calculatePaymentMethodTrend handles empty transactions`() {
        val trend = calculatePaymentMethodTrend(emptyList(), periodCount = 2)

        assertEquals(2, trend.monthlyUsage.size)
        assertEquals(0.0, trend.monthlyUsage[0].totalAmount, 0.01)
        assertEquals("Unknown", trend.preferredMethod)
        assertEquals(0f, trend.preferredMethodPercentage, 0.01f)
    }

    @Test
    fun `calculatePaymentMethodTrend only includes DEBIT transactions`() {
        val mixedTransactions = januaryTransactions + listOf(
            Transaction(
                id = 7L,
                amount = 50000.0,
                type = TransactionType.CREDIT, // This should be excluded
                merchantName = "Salary",
                transactionDate = LocalDateTime.of(2024, 1, 1, 9, 0),
                source = TransactionSource.MANUAL,
                paymentMethod = "UPI"
            )
        )

        val trend = calculatePaymentMethodTrend(mixedTransactions, periodCount = 1)

        // Should only count the 5000 UPI debit transaction, not the 50000 credit
        val januaryUsage = trend.monthlyUsage.find { it.yearMonth == YearMonth.of(2024, 1) }
        assertEquals(5000.0, januaryUsage!!.upiAmount, 0.01)
    }

    @Test
    fun `MonthlyPaymentMethodUsage calculates total correctly`() {
        val usage = MonthlyPaymentMethodUsage(
            yearMonth = YearMonth.of(2024, 1),
            upiAmount = 5000.0,
            creditCardAmount = 3000.0,
            debitCardAmount = 1000.0,
            cashAmount = 500.0,
            otherAmount = 200.0,
            totalAmount = 9700.0
        )

        assertEquals(9700.0, usage.totalAmount, 0.01)
    }
}
