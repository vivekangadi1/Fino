package com.fino.app.domain.model

/**
 * Type of transaction - money going out or coming in
 */
enum class TransactionType {
    DEBIT,  // Money spent/going out
    CREDIT  // Money received/coming in
}

/**
 * Source of the transaction record
 */
enum class TransactionSource {
    SMS,    // Automatically parsed from SMS
    MANUAL, // User entered manually
    EMAIL   // Parsed from email (future)
}

/**
 * Frequency for recurring transactions
 */
enum class RecurringFrequency {
    WEEKLY,
    MONTHLY,
    YEARLY
}

/**
 * Budget health status
 */
enum class BudgetStatus {
    SAFE,       // Under 75% of budget
    WARNING,    // 75-100% of budget
    OVER_BUDGET // Over 100% of budget
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
