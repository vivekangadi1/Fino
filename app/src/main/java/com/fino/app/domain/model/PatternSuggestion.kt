package com.fino.app.domain.model

import java.time.LocalDate

/**
 * Represents a detected recurring pattern from transaction history
 * that can be suggested to the user as a potential recurring bill
 */
data class PatternSuggestion(
    val id: Long = 0,  // Database ID (0 for unsaved suggestions)
    val merchantPattern: String,
    val displayName: String,
    val averageAmount: Double,
    val detectedFrequency: RecurringFrequency,
    val typicalDayOfPeriod: Int,
    val occurrenceCount: Int,
    val confidence: Float,
    val nextExpected: LocalDate,
    val categoryId: Long?
) {
    /**
     * Amount variance as a percentage (0.0 - 1.0)
     * Lower is better - means more consistent amounts
     */
    val amountVariance: Float
        get() = if (averageAmount > 0) 0.1f else 0f // Default 10% variance

    /**
     * Human-readable description of the pattern
     */
    val description: String
        get() = buildString {
            append("Usually paid around the ${ordinalSuffix(typicalDayOfPeriod)}")
            append(" (${occurrenceCount} times)")
        }

    /**
     * Whether this is a high-confidence suggestion
     */
    val isHighConfidence: Boolean
        get() = confidence >= 0.8f

    /**
     * Convert to a RecurringRule when user confirms
     */
    fun toRecurringRule(): RecurringRule {
        return RecurringRule(
            id = 0L, // Will be assigned by database
            merchantPattern = merchantPattern,
            categoryId = categoryId ?: 0L,
            expectedAmount = averageAmount,
            amountVariance = amountVariance,
            frequency = detectedFrequency,
            dayOfPeriod = typicalDayOfPeriod,
            lastOccurrence = null,
            nextExpected = nextExpected,
            occurrenceCount = occurrenceCount,
            isActive = true,
            isUserConfirmed = true,
            createdAt = java.time.LocalDateTime.now()
        )
    }

    companion object {
        /**
         * Get ordinal suffix for a day number (1st, 2nd, 3rd, etc.)
         */
        private fun ordinalSuffix(day: Int): String {
            return when {
                day in 11..13 -> "${day}th"
                day % 10 == 1 -> "${day}st"
                day % 10 == 2 -> "${day}nd"
                day % 10 == 3 -> "${day}rd"
                else -> "${day}th"
            }
        }
    }
}
