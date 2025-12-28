package com.fino.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SharedPreferences wrapper for app-level preferences.
 * Used to track initial SMS scan completion and pattern detection timing.
 */
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * Whether the initial SMS scan has been completed.
     * This scan runs on first app launch after SMS permission is granted.
     * Uses commit() for synchronous write to ensure worker sees the value immediately.
     */
    var hasCompletedInitialScan: Boolean
        get() = prefs.getBoolean(KEY_INITIAL_SCAN_COMPLETE, false)
        set(value) { prefs.edit().putBoolean(KEY_INITIAL_SCAN_COMPLETE, value).commit() }

    /**
     * Timestamp of the last pattern detection run.
     */
    var lastPatternDetectionTime: Long
        get() = prefs.getLong(KEY_LAST_PATTERN_DETECTION, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_PATTERN_DETECTION, value).apply()

    /**
     * Whether the initial SMS scan is currently in progress.
     * Uses commit() for synchronous write.
     */
    var isInitialScanInProgress: Boolean
        get() = prefs.getBoolean(KEY_INITIAL_SCAN_IN_PROGRESS, false)
        set(value) { prefs.edit().putBoolean(KEY_INITIAL_SCAN_IN_PROGRESS, value).commit() }

    /**
     * Number of months to scan for initial SMS import.
     */
    var initialScanMonths: Int
        get() = prefs.getInt(KEY_INITIAL_SCAN_MONTHS, 3)
        set(value) = prefs.edit().putInt(KEY_INITIAL_SCAN_MONTHS, value).apply()

    /**
     * Reset all preferences (for debugging/testing).
     */
    fun reset() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "fino_app_prefs"
        private const val KEY_INITIAL_SCAN_COMPLETE = "initial_scan_complete"
        private const val KEY_LAST_PATTERN_DETECTION = "last_pattern_detection"
        private const val KEY_INITIAL_SCAN_IN_PROGRESS = "initial_scan_in_progress"
        private const val KEY_INITIAL_SCAN_MONTHS = "initial_scan_months"
    }
}
