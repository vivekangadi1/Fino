package com.fino.app.domain.model

/**
 * Type of transaction - money going out, coming in, or saved
 */
enum class TransactionType {
    DEBIT,   // Money spent/going out (expenses)
    CREDIT,  // Money received/coming in (income)
    SAVINGS  // Money set aside (investments, FD, emergency fund)
}

/**
 * Source of the transaction record
 */
enum class TransactionSource {
    SMS,      // Automatically parsed from incoming SMS (realtime)
    SMS_SCAN, // Parsed from historical SMS scan
    MANUAL,   // User entered manually
    EMAIL     // Parsed from email (future)
}

/**
 * Frequency for recurring transactions
 */
enum class RecurringFrequency {
    ONE_TIME,  // Non-recurring, single occurrence
    WEEKLY,
    MONTHLY,
    YEARLY
}

/**
 * Budget health status
 */
enum class BudgetStatus {
    UNDER_BUDGET,      // Under 75% of budget
    APPROACHING_LIMIT, // 75-100% of budget
    OVER_BUDGET        // Over 100% of budget
}

/**
 * Achievement categories
 */
enum class AchievementType {
    STREAK,             // Consecutive day streaks
    TRANSACTION_COUNT,  // Total transactions logged
    CATEGORY_COUNT,     // Transactions in specific category
    BUDGET,             // Budget-related achievements
    UNDER_BUDGET,       // Staying under budget
    CREDIT_CARD,        // Credit card tracking
    RECURRING,          // Recurring expense detection
    SPECIAL             // Special/misc achievements
}

/**
 * Match type for merchant matching
 */
enum class MatchType {
    EXACT,  // Exact merchant name match
    FUZZY,  // Similar merchant name (requires confirmation)
    NONE    // No match found
}

/**
 * Status of an event (trip, wedding, etc.)
 */
enum class EventStatus {
    ACTIVE,     // Event is ongoing
    COMPLETED,  // Event has ended naturally
    CANCELLED   // Event was cancelled by user
}

/**
 * Alert level for budget status
 */
enum class BudgetAlertLevel {
    NORMAL,    // Under 75%
    WARNING,   // 75-99%
    EXCEEDED   // 100%+
}

/**
 * Payment status for event expenses
 */
enum class PaymentStatus {
    PAID,      // Fully paid
    PENDING,   // Payment not yet made
    PARTIAL,   // Partial payment made (advance)
    OVERDUE    // Past due date and not fully paid
}
