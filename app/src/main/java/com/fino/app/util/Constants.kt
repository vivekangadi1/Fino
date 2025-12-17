package com.fino.app.util

/**
 * Application-wide constants.
 */
object Constants {

    // Matching thresholds
    const val EXACT_MATCH_THRESHOLD = 0.95f
    const val FUZZY_MATCH_THRESHOLD = 0.7f
    const val AUTO_APPLY_THRESHOLD = 0.95f

    // XP Rewards
    object XpRewards {
        const val CATEGORIZE_TRANSACTION = 5
        const val CONFIRM_FUZZY_MATCH = 10
        const val REJECT_FUZZY_MATCH = 2
        const val SET_BUDGET = 20
        const val ADD_CREDIT_CARD = 25
        const val COMPLETE_ONBOARDING = 50
        const val MANUAL_ENTRY = 10
        const val EXPORT_DATA = 15
        const val STAY_UNDER_BUDGET = 30
        const val IDENTIFY_RECURRING = 20
        const val FIRST_TRANSACTION = 25
        const val SET_SAVINGS_GOAL = 20
    }

    // Budget thresholds
    const val BUDGET_WARNING_THRESHOLD = 0.75f
    const val BUDGET_DANGER_THRESHOLD = 1.0f

    // Streak grace period (days missed before reset)
    const val STREAK_GRACE_PERIOD_DAYS = 1

    // Recurring detection
    const val MIN_OCCURRENCES_FOR_RECURRING = 3
    const val AMOUNT_VARIANCE_TOLERANCE = 0.05f // 5%

    // Database
    const val DATABASE_NAME = "fino_database"
    const val DATABASE_VERSION = 1

    // Preferences
    const val PREFS_NAME = "fino_prefs"
    const val PREF_ONBOARDING_COMPLETE = "onboarding_complete"
    const val PREF_BIOMETRIC_ENABLED = "biometric_enabled"
    const val PREF_DARK_MODE = "dark_mode"
    const val PREF_NOTIFICATION_ENABLED = "notification_enabled"

    // Notifications
    const val NOTIFICATION_CHANNEL_ID = "fino_notifications"
    const val NOTIFICATION_CHANNEL_NAME = "Fino Alerts"
}
