package com.fino.app.service.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fino.app.domain.model.PatternSuggestion
import com.fino.app.domain.model.RecurringFrequency
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionType
import com.fino.app.presentation.MainActivity
import com.fino.app.util.AmountFormatter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for showing notifications related to transactions and app events.
 */
@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val CHANNEL_ID_TRANSACTIONS = "fino_transactions"
        const val CHANNEL_ID_ACHIEVEMENTS = "fino_achievements"
        const val CHANNEL_ID_REMINDERS = "fino_reminders"
        const val CHANNEL_ID_SUGGESTIONS = "fino_suggestions"

        const val ACTION_CONFIRM_SUGGESTION = "com.fino.app.CONFIRM_SUGGESTION"
        const val ACTION_DISMISS_SUGGESTION = "com.fino.app.DISMISS_SUGGESTION"
        const val EXTRA_SUGGESTION_ID = "suggestion_id"

        private var notificationId = 1000
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)

            // Transaction channel
            val transactionChannel = NotificationChannel(
                CHANNEL_ID_TRANSACTIONS,
                "Transactions",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for detected transactions"
            }

            // Achievements channel
            val achievementChannel = NotificationChannel(
                CHANNEL_ID_ACHIEVEMENTS,
                "Achievements",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for unlocked achievements"
            }

            // Reminders channel
            val reminderChannel = NotificationChannel(
                CHANNEL_ID_REMINDERS,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Bill payment reminders and alerts"
            }

            // Suggestions channel
            val suggestionsChannel = NotificationChannel(
                CHANNEL_ID_SUGGESTIONS,
                "Recurring Bill Suggestions",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Suggestions for detected recurring bills"
            }

            notificationManager.createNotificationChannels(
                listOf(transactionChannel, achievementChannel, reminderChannel, suggestionsChannel)
            )
        }
    }

    /**
     * Show notification for a detected transaction.
     */
    fun showTransactionNotification(transaction: Transaction) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val isExpense = transaction.type == TransactionType.DEBIT
        val title = if (isExpense) "Expense Detected" else "Income Detected"
        val amountText = AmountFormatter.format(transaction.amount)
        val message = "${transaction.merchantName}: $amountText"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TRANSACTIONS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId++, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted
        }
    }

    /**
     * Show notification for an unlocked achievement.
     */
    fun showAchievementNotification(achievementName: String, emoji: String, xpReward: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "rewards")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ACHIEVEMENTS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$emoji Achievement Unlocked!")
            .setContentText("$achievementName (+$xpReward XP)")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId++, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted
        }
    }

    /**
     * Show reminder notification for bill payment.
     */
    fun showBillReminderNotification(cardName: String, amount: Double, dueDate: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "cards")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val amountText = AmountFormatter.format(amount)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Bill Due Soon")
            .setContentText("$cardName: $amountText due on $dueDate")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId++, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted
        }
    }

    /**
     * Show notification for a detected recurring bill suggestion.
     * Includes Confirm and Dismiss action buttons.
     */
    fun showRecurringSuggestionNotification(suggestion: PatternSuggestion) {
        val suggestionId = suggestion.id
        val uniqueNotificationId = (10000 + suggestionId).toInt()

        // Main intent - opens upcoming bills screen
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "upcoming_bills")
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            uniqueNotificationId,
            mainIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Confirm action intent
        val confirmIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_CONFIRM_SUGGESTION
            putExtra(EXTRA_SUGGESTION_ID, suggestionId)
        }
        val confirmPendingIntent = PendingIntent.getBroadcast(
            context,
            uniqueNotificationId + 1,
            confirmIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Dismiss action intent
        val dismissIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_DISMISS_SUGGESTION
            putExtra(EXTRA_SUGGESTION_ID, suggestionId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            uniqueNotificationId + 2,
            dismissIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val amountText = AmountFormatter.format(suggestion.averageAmount)
        val frequencyText = when (suggestion.detectedFrequency) {
            RecurringFrequency.ONE_TIME -> "one-time"
            RecurringFrequency.WEEKLY -> "week"
            RecurringFrequency.MONTHLY -> "month"
            RecurringFrequency.YEARLY -> "year"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_SUGGESTIONS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Recurring Bill Detected")
            .setContentText("${suggestion.displayName}: $amountText/$frequencyText")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${suggestion.displayName}: $amountText/$frequencyText\nTap to add to your upcoming bills"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(mainPendingIntent)
            .addAction(android.R.drawable.ic_input_add, "Confirm", confirmPendingIntent)
            .addAction(android.R.drawable.ic_delete, "Dismiss", dismissPendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(uniqueNotificationId, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted
        }
    }

    /**
     * Cancel a suggestion notification.
     */
    fun cancelSuggestionNotification(suggestionId: Long) {
        val uniqueNotificationId = (10000 + suggestionId).toInt()
        try {
            NotificationManagerCompat.from(context).cancel(uniqueNotificationId)
        } catch (e: SecurityException) {
            // Ignore
        }
    }
}
