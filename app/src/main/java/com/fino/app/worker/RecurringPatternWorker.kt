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
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Background worker that runs pattern detection to find recurring bills.
 * Runs daily or on-demand after batch SMS imports.
 */
@HiltWorker
class RecurringPatternWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val patternDetectionService: PatternDetectionService,
    private val patternSuggestionRepository: PatternSuggestionRepository,
    private val notificationService: NotificationService,
    private val appPreferences: AppPreferences
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "RecurringPatternWorker"
        const val WORK_NAME = "recurring_pattern_detection"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting pattern detection...")

        return try {
            // Detect patterns from transaction history
            val patterns = patternDetectionService.detectPatterns()
            Log.d(TAG, "Found ${patterns.size} patterns")

            var newSuggestionsCount = 0

            for (pattern in patterns) {
                // Try to create a suggestion (will be ignored if already exists)
                val suggestion = patternSuggestionRepository.createFromPatternDetection(pattern)
                if (suggestion != null) {
                    newSuggestionsCount++
                    // Show notification for new suggestion
                    notificationService.showRecurringSuggestionNotification(suggestion)
                    Log.d(TAG, "Created suggestion for: ${pattern.displayName}")
                }
            }

            // Update last detection time
            appPreferences.lastPatternDetectionTime = System.currentTimeMillis()

            // Cleanup old dismissed suggestions
            patternSuggestionRepository.cleanupOldDismissed()

            Log.d(TAG, "Pattern detection complete. New suggestions: $newSuggestionsCount")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Pattern detection failed", e)
            Result.retry()
        }
    }
}
