package com.fino.app.service.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.fino.app.data.repository.PatternSuggestionRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionSource
import com.fino.app.service.categorization.SmartCategorizationService
import com.fino.app.service.notification.NotificationService
import com.fino.app.service.parser.ParsedTransaction
import com.fino.app.service.parser.SmsParser
import com.fino.app.util.MerchantNormalizer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BroadcastReceiver that listens for incoming SMS messages.
 * Parses bank transaction SMS and saves them to the database.
 */
@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var notificationService: NotificationService

    @Inject
    lateinit var smsParser: SmsParser

    @Inject
    lateinit var smartCategorizationService: SmartCategorizationService

    @Inject
    lateinit var patternSuggestionRepository: PatternSuggestionRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "SmsReceiver"

        // Bank sender IDs that we care about
        private val BANK_SENDERS = listOf(
            // Banks
            "HDFCBK", "SBIINB", "ICICIB", "ICICIT", "AXISBK", "KOTAKB",
            "SBICARD", "ILOANS", "BOBTXN",
            // Payment apps
            "PAYTM", "GPAY", "PHONEP", "AMAZON",
            // Food & Transport
            "SWIGGY", "ZOMATO", "UBER", "OLA", "CRED",
            // Travel
            "AIRINDIA", "INDIGO", "SPICEJET",
            // Subscription services
            "JIOHTT", "SPOTIFY", "NETFLIX", "HOTSTAR", "DISNEY", "GOOGLE"
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) {
            return
        }

        for (smsMessage in messages) {
            val sender = smsMessage.displayOriginatingAddress ?: continue
            val body = smsMessage.messageBody ?: continue

            Log.d(TAG, "SMS received from: $sender")

            // Check if this is from a bank/payment service
            if (!isBankSms(sender)) {
                Log.d(TAG, "Ignoring non-bank SMS from: $sender")
                continue
            }

            // Try to parse the transaction
            val parsedTransaction = smsParser.parse(body)

            if (parsedTransaction != null) {
                Log.d(TAG, "Parsed transaction: ${parsedTransaction.amount} from ${parsedTransaction.merchantName}")

                // Save to database
                scope.launch {
                    try {
                        // Auto-categorize using smart categorization service
                        val normalizedMerchant = MerchantNormalizer.normalize(parsedTransaction.merchantName)
                        val categorizationResult = smartCategorizationService.categorize(
                            normalizedMerchantName = normalizedMerchant,
                            amount = parsedTransaction.amount,
                            transactionTime = parsedTransaction.transactionDate,
                            smsBody = body
                        )

                        val categoryId = categorizationResult.categoryId
                        val categoryConfidence = categorizationResult.confidence

                        val transaction = Transaction(
                            amount = parsedTransaction.amount,
                            type = parsedTransaction.type,
                            merchantName = parsedTransaction.merchantName,
                            merchantNormalized = categorizationResult.suggestedName,
                            categoryId = categoryId,
                            transactionDate = parsedTransaction.transactionDate,
                            source = TransactionSource.SMS,
                            rawSmsBody = body,
                            smsSender = sender,
                            parsedConfidence = parsedTransaction.confidence,
                            needsReview = categoryId == 15L || categoryConfidence < 0.85f,
                            reference = parsedTransaction.reference,
                            isRecurring = parsedTransaction.isLikelySubscription,
                            bankName = parsedTransaction.bankName,
                            paymentMethod = when {
                                parsedTransaction.cardLastFour != null -> "CREDIT_CARD"
                                parsedTransaction.reference != null -> "UPI"
                                else -> null
                            },
                            cardLastFour = parsedTransaction.cardLastFour
                        )

                        val id = transactionRepository.insert(transaction)

                        // Track categorization for analytics
                        smartCategorizationService.trackCategorization(
                            transactionId = id,
                            merchantName = parsedTransaction.merchantName,
                            result = categorizationResult
                        )

                        if (categoryId != 15L) {
                            Log.d(TAG, "Transaction saved with ID: $id - ${smartCategorizationService.getCategorizationStats(categorizationResult)}")
                        } else {
                            Log.d(TAG, "Transaction saved with ID: $id (No category match, needs review)")
                        }

                        // Show notification
                        notificationService.showTransactionNotification(transaction)

                        // If this is a subscription, create a suggestion for recurring bill
                        if (parsedTransaction.isLikelySubscription) {
                            createRecurringSuggestion(parsedTransaction, categoryId)
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Error saving transaction", e)
                    }
                }
            } else {
                Log.d(TAG, "Could not parse transaction from SMS")
            }
        }
    }

    /**
     * Creates a recurring bill suggestion for subscription transactions.
     */
    private suspend fun createRecurringSuggestion(
        parsedTransaction: ParsedTransaction,
        categoryId: Long?
    ) {
        try {
            val suggestion = patternSuggestionRepository.createFromSubscriptionSms(
                parsedTransaction,
                categoryId
            )

            if (suggestion != null) {
                Log.d(TAG, "Created recurring bill suggestion for: ${parsedTransaction.merchantName}")
                notificationService.showRecurringSuggestionNotification(suggestion)
            } else {
                Log.d(TAG, "Subscription already tracked: ${parsedTransaction.merchantName}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating recurring suggestion", e)
        }
    }

    private fun isBankSms(sender: String): Boolean {
        val upperSender = sender.uppercase()
        return BANK_SENDERS.any { upperSender.contains(it) } ||
                upperSender.contains("BANK") ||
                upperSender.contains("UPI") ||
                upperSender.matches(Regex(".*[A-Z]{2}-[A-Z]+.*")) // Pattern like "AD-HDFCBK"
    }
}
