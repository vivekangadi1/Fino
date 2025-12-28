package com.fino.app.service.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.fino.app.data.repository.PatternSuggestionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BroadcastReceiver for handling notification action buttons.
 * Handles Confirm and Dismiss actions for recurring bill suggestions.
 */
@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var patternSuggestionRepository: PatternSuggestionRepository

    @Inject
    lateinit var notificationService: NotificationService

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "NotificationActionReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val suggestionId = intent.getLongExtra(NotificationService.EXTRA_SUGGESTION_ID, -1)
        if (suggestionId == -1L) {
            Log.w(TAG, "No suggestion ID provided in intent")
            return
        }

        Log.d(TAG, "Received action: ${intent.action} for suggestion $suggestionId")

        when (intent.action) {
            NotificationService.ACTION_CONFIRM_SUGGESTION -> {
                scope.launch {
                    try {
                        val ruleId = patternSuggestionRepository.confirmSuggestion(suggestionId)
                        if (ruleId > 0) {
                            Log.d(TAG, "Suggestion $suggestionId confirmed, created rule $ruleId")
                        } else {
                            Log.w(TAG, "Failed to confirm suggestion $suggestionId")
                        }
                        // Cancel the notification
                        notificationService.cancelSuggestionNotification(suggestionId)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error confirming suggestion", e)
                    }
                }
            }
            NotificationService.ACTION_DISMISS_SUGGESTION -> {
                scope.launch {
                    try {
                        patternSuggestionRepository.dismissSuggestion(suggestionId)
                        Log.d(TAG, "Suggestion $suggestionId dismissed")
                        // Cancel the notification
                        notificationService.cancelSuggestionNotification(suggestionId)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error dismissing suggestion", e)
                    }
                }
            }
            else -> {
                Log.w(TAG, "Unknown action: ${intent.action}")
            }
        }
    }
}
