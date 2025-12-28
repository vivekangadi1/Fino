package com.fino.app.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Represents an upcoming bill from any source (recurring rule, credit card, or pattern suggestion)
 */
data class UpcomingBill(
    val id: String,
    val source: BillSource,
    val merchantName: String,
    val displayName: String,
    val amount: Double,
    val amountVariance: Float?,
    val dueDate: LocalDate,
    val frequency: RecurringFrequency?,
    val categoryId: Long?,
    val status: BillStatus,
    val isPaid: Boolean,
    val isUserConfirmed: Boolean,
    val confidence: Float,
    val creditCardLastFour: String?,
    val sourceId: Long
) {
    companion object {
        /**
         * Generate a unique bill ID from source and source ID
         */
        fun generateBillId(source: BillSource, sourceId: Long): String {
            return "${source.name}_$sourceId"
        }
    }
}

/**
 * Source of the bill
 */
enum class BillSource {
    RECURRING_RULE,      // User-created or confirmed recurring rule
    CREDIT_CARD,         // Auto-detected from credit card due dates
    PATTERN_SUGGESTION   // AI-suggested from transaction patterns
}

/**
 * Status of the bill based on due date
 */
enum class BillStatus {
    OVERDUE,        // Past due date, not paid
    DUE_TODAY,      // Due today
    DUE_TOMORROW,   // Due tomorrow
    DUE_THIS_WEEK,  // Due within 7 days (excluding today/tomorrow)
    UPCOMING,       // Due later this month or next
    PAID;           // Bill has been paid

    companion object {
        /**
         * Calculate bill status based on due date and payment status
         */
        fun calculateStatus(dueDate: LocalDate, isPaid: Boolean): BillStatus {
            if (isPaid) return PAID

            val today = LocalDate.now()
            val daysUntilDue = ChronoUnit.DAYS.between(today, dueDate)

            return when {
                daysUntilDue < 0 -> OVERDUE
                daysUntilDue == 0L -> DUE_TODAY
                daysUntilDue == 1L -> DUE_TOMORROW
                daysUntilDue <= 7 -> DUE_THIS_WEEK
                else -> UPCOMING
            }
        }
    }
}
