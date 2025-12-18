package com.fino.app.service.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionSource
import com.fino.app.service.notification.NotificationService
import com.fino.app.service.parser.SmsParser
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

    private val smsParser = SmsParser()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "SmsReceiver"

        // Bank sender IDs that we care about
        private val BANK_SENDERS = listOf(
            "HDFCBK", "SBIINB", "ICICIB", "AXISBK", "KOTAKB",
            "ILOANS", "PAYTM", "GPAY", "PHONEP", "AMAZON",
            "SWIGGY", "ZOMATO", "UBER", "OLA", "CRED",
            "AIRINDIA", "INDIGO", "SPICEJET"
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
                        val transaction = Transaction(
                            amount = parsedTransaction.amount,
                            type = parsedTransaction.type,
                            merchantName = parsedTransaction.merchantName,
                            transactionDate = parsedTransaction.transactionDate,
                            source = TransactionSource.SMS,
                            rawSmsBody = body,
                            smsSender = sender,
                            parsedConfidence = parsedTransaction.confidence,
                            needsReview = parsedTransaction.confidence < 0.8f,
                            reference = parsedTransaction.reference,
                            isRecurring = parsedTransaction.isLikelySubscription
                        )

                        val id = transactionRepository.insert(transaction)
                        Log.d(TAG, "Transaction saved with ID: $id")

                        // Show notification
                        notificationService.showTransactionNotification(transaction)

                    } catch (e: Exception) {
                        Log.e(TAG, "Error saving transaction", e)
                    }
                }
            } else {
                Log.d(TAG, "Could not parse transaction from SMS")
            }
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
