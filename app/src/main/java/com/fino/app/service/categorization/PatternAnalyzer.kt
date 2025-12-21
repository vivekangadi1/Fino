package com.fino.app.service.categorization

import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pattern-based category inference using contextual signals.
 * Analyzes transaction time, amount patterns, and SMS content.
 */
@Singleton
class PatternAnalyzer @Inject constructor() {

    /**
     * Infer category from transaction context.
     * Returns null if no patterns match.
     */
    fun inferFromContext(
        amount: Double,
        time: LocalDateTime,
        smsBody: String
    ): PatternInference? {

        val hour = time.hour
        val bodyLower = smsBody.lowercase()

        // Subscription patterns (regular amounts, specific keywords)
        if (isSubscriptionPattern(amount, bodyLower)) {
            return PatternInference(5L, 0.72f, "Subscription pattern")
        }

        // Food delivery patterns (time + amount range)
        if (isFoodDeliveryPattern(amount, hour)) {
            return PatternInference(1L, 0.68f, "Food delivery time/amount pattern")
        }

        // Transport patterns (early morning or late evening, small amounts)
        if (isTransportPattern(amount, hour)) {
            return PatternInference(2L, 0.65f, "Transport time/amount pattern")
        }

        // Bill patterns (large round amounts)
        if (isBillPattern(amount, bodyLower)) {
            return PatternInference(6L, 0.70f, "Bill amount pattern")
        }

        // Shopping patterns (large amounts, specific keywords)
        if (isShoppingPattern(amount, bodyLower)) {
            return PatternInference(3L, 0.63f, "Shopping pattern")
        }

        // Grocery patterns (moderate amounts, specific times)
        if (isGroceryPattern(amount, hour)) {
            return PatternInference(9L, 0.62f, "Grocery pattern")
        }

        return null
    }

    /**
     * Check if transaction matches subscription pattern.
     */
    private fun isSubscriptionPattern(amount: Double, smsBody: String): Boolean {
        // Common subscription amounts
        val subscriptionAmounts = listOf(99.0, 149.0, 199.0, 299.0, 399.0, 499.0, 599.0, 699.0, 799.0, 999.0, 1499.0, 1999.0)

        val isSubscriptionAmount = subscriptionAmounts.any { kotlin.math.abs(amount - it) < 1.0 }
        val hasSubscriptionKeyword = smsBody.contains("subscription") ||
                                     smsBody.contains("renewed") ||
                                     smsBody.contains("premium") ||
                                     smsBody.contains("plan activated") ||
                                     smsBody.contains("monthly")

        return isSubscriptionAmount && hasSubscriptionKeyword
    }

    /**
     * Check if transaction matches food delivery pattern.
     */
    private fun isFoodDeliveryPattern(amount: Double, hour: Int): Boolean {
        val isLunchTime = hour in 11..14
        val isDinnerTime = hour in 18..22
        val isFoodAmount = amount in 150.0..2000.0

        return (isLunchTime || isDinnerTime) && isFoodAmount
    }

    /**
     * Check if transaction matches transport pattern.
     */
    private fun isTransportPattern(amount: Double, hour: Int): Boolean {
        val isMorningCommute = hour in 6..10
        val isEveningCommute = hour in 17..23
        val isTransportAmount = amount in 50.0..1000.0

        return (isMorningCommute || isEveningCommute) && isTransportAmount
    }

    /**
     * Check if transaction matches bill payment pattern.
     */
    private fun isBillPattern(amount: Double, smsBody: String): Boolean {
        val isRoundAmount = amount >= 500.0 && amount % 100 == 0.0
        val hasBillKeyword = smsBody.contains("bill") ||
                            smsBody.contains("payment") ||
                            smsBody.contains("due") ||
                            smsBody.contains("paid")

        return isRoundAmount || (amount >= 1000.0 && hasBillKeyword)
    }

    /**
     * Check if transaction matches shopping pattern.
     */
    private fun isShoppingPattern(amount: Double, smsBody: String): Boolean {
        val isLargeAmount = amount >= 1000.0
        val hasShoppingKeyword = smsBody.contains("purchase") ||
                                smsBody.contains("order") ||
                                smsBody.contains("delivery")

        return isLargeAmount && hasShoppingKeyword
    }

    /**
     * Check if transaction matches grocery pattern.
     */
    private fun isGroceryPattern(amount: Double, hour: Int): Boolean {
        val isGroceryTime = hour in 8..12 || hour in 16..20
        val isGroceryAmount = amount in 500.0..5000.0

        return isGroceryTime && isGroceryAmount
    }
}

/**
 * Result of pattern-based inference.
 */
data class PatternInference(
    val categoryId: Long,
    val confidence: Float,
    val reason: String
)
