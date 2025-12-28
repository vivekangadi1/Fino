package com.fino.app.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fino.app.data.preferences.AppPreferences
import com.fino.app.data.repository.PatternSuggestionRepository
import com.fino.app.service.notification.NotificationService
import com.fino.app.service.pattern.PatternDetectionService
import com.fino.app.service.sms.SmsScanner
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.YearMonth

/**
 * Background worker that performs initial SMS scan on first app launch.
 * Scans the last 3 months of SMS messages and runs pattern detection.
 */
@HiltWorker
class InitialSmsScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val smsScanner: SmsScanner,
    private val patternDetectionService: PatternDetectionService,
    private val patternSuggestionRepository: PatternSuggestionRepository,
    private val notificationService: NotificationService,
    private val appPreferences: AppPreferences
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "InitialSmsScanWorker"
        const val WORK_NAME = "initial_sms_scan"
    }

    override suspend fun doWork(): Result {
        // Skip if already completed
        if (appPreferences.hasCompletedInitialScan) {
            Log.d(TAG, "Initial scan already completed, skipping")
            return Result.success()
        }

        // Mark as in progress
        appPreferences.isInitialScanInProgress = true

        Log.d(TAG, "Starting initial SMS scan...")

        return try {
            val monthsToScan = appPreferences.initialScanMonths
            val currentMonth = YearMonth.now()

            var totalTransactionsSaved = 0

            // Scan last N months
            for (i in 0 until monthsToScan) {
                val month = currentMonth.minusMonths(i.toLong())
                Log.d(TAG, "Scanning month: $month")

                val result = smsScanner.scanMonth(month)
                totalTransactionsSaved += result.transactionsSaved

                Log.d(TAG, "Month $month: scanned=${result.totalSmsScanned}, " +
                        "found=${result.transactionsFound}, " +
                        "saved=${result.transactionsSaved}, " +
                        "duplicates=${result.duplicatesSkipped}")
            }

            Log.d(TAG, "SMS scan complete. Total transactions saved: $totalTransactionsSaved")

            // Run pattern detection if we found significant transactions
            if (totalTransactionsSaved >= 3) {
                Log.d(TAG, "Running pattern detection...")
                val patterns = patternDetectionService.detectPatterns()
                Log.d(TAG, "Found ${patterns.size} patterns")

                var suggestionsCreated = 0
                for (pattern in patterns) {
                    val suggestion = patternSuggestionRepository.createFromPatternDetection(pattern)
                    if (suggestion != null) {
                        suggestionsCreated++
                        // Don't spam notifications during initial scan
                        // User can see suggestions in Upcoming Bills screen
                    }
                }

                if (suggestionsCreated > 0) {
                    Log.d(TAG, "Created $suggestionsCreated pattern suggestions")
                }
            }

            // Mark scan as complete
            appPreferences.hasCompletedInitialScan = true
            appPreferences.isInitialScanInProgress = false
            appPreferences.lastPatternDetectionTime = System.currentTimeMillis()

            Log.d(TAG, "Initial SMS scan and pattern detection complete")
            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "Initial SMS scan failed: ${e.message}", e)
            appPreferences.isInitialScanInProgress = false
            // Return failure instead of retry so UI can show error
            Result.failure(
                androidx.work.Data.Builder()
                    .putString("error", e.message ?: "Unknown error")
                    .build()
            )
        }
    }
}
