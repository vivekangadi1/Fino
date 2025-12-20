package com.fino.app.domain.model

/**
 * Represents spending information for a specific payment method.
 *
 * @property paymentMethod Type of payment method ("UPI", "CREDIT_CARD", "UNKNOWN")
 * @property bankName Bank name (e.g., "HDFC", "ICICI", "SBI", "AXIS") or "Unknown"
 * @property cardLastFour Last 4 digits of card number for credit cards
 * @property displayName Human-readable display name (e.g., "HDFC UPI", "HDFC ****1234")
 * @property amount Total amount spent using this payment method
 * @property transactionCount Number of transactions using this payment method
 * @property percentage Percentage of total spending (0.0 to 1.0)
 */
data class PaymentMethodSpending(
    val paymentMethod: String,
    val bankName: String,
    val cardLastFour: String?,
    val displayName: String,
    val amount: Double,
    val transactionCount: Int,
    val percentage: Float
)

/**
 * Complete breakdown of spending by payment methods.
 *
 * @property upiTransactions List of UPI payment method spendings, sorted by amount descending
 * @property creditCardTransactions List of credit card payment method spendings, sorted by amount descending
 * @property unknownTransactions Spending from transactions with unknown payment method
 * @property totalUpiSpend Total amount spent via UPI
 * @property totalCreditCardSpend Total amount spent via credit cards
 * @property totalUnknownSpend Total amount spent via unknown payment methods
 */
data class PaymentMethodBreakdown(
    val upiTransactions: List<PaymentMethodSpending>,
    val creditCardTransactions: List<PaymentMethodSpending>,
    val unknownTransactions: PaymentMethodSpending?,
    val totalUpiSpend: Double,
    val totalCreditCardSpend: Double,
    val totalUnknownSpend: Double
)
