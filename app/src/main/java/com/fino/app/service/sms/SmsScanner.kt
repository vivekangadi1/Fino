package com.fino.app.service.sms

import android.util.Log
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionSource
import com.fino.app.service.parser.SmsParser
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scans historical SMS messages from the device inbox and parses transactions.
 */
@Singleton
class SmsScanner @Inject constructor(
    private val smsReader: SmsReader,
    private val transactionRepository: TransactionRepository,
    private val smsParser: SmsParser
) {
    companion object {
        private const val TAG = "SmsScanner"

        // Bank sender IDs that we care about
        private val BANK_SENDERS = listOf(
            "HDFCBK", "SBIINB", "ICICIB", "AXISBK", "KOTAKB",
            "ILOANS", "PAYTM", "GPAY", "PHONEP", "AMAZON",
            "SWIGGY", "ZOMATO", "UBER", "OLA", "CRED",
            "AIRINDIA", "INDIGO", "SPICEJET"
        )
    }

    /**
     * Scan all SMS messages for the given month and save parsed transactions.
     */
    suspend fun scanMonth(yearMonth: YearMonth): SmsScanResult {
        var totalSmsScanned = 0
        var transactionsFound = 0
        var transactionsSaved = 0
        var duplicatesSkipped = 0
        var errors = 0

        try {
            val messages = smsReader.readSmsForMonth(yearMonth)
            totalSmsScanned = messages.size

            for (sms in messages) {
                try {
                    // Skip non-bank SMS
                    if (!isBankSms(sms.sender)) {
                        continue
                    }

                    // Try to parse the transaction
                    val parsed = smsParser.parse(sms.body) ?: continue
                    transactionsFound++

                    // Check for duplicate
                    if (transactionRepository.existsByRawSmsBody(sms.body)) {
                        duplicatesSkipped++
                        Log.d(TAG, "Skipping duplicate SMS")
                        continue
                    }

                    // Create and save transaction
                    val transaction = Transaction(
                        amount = parsed.amount,
                        type = parsed.type,
                        merchantName = parsed.merchantName,
                        transactionDate = parsed.transactionDate,
                        source = TransactionSource.SMS_SCAN,
                        rawSmsBody = sms.body,
                        smsSender = sms.sender,
                        parsedConfidence = parsed.confidence,
                        needsReview = parsed.confidence < 0.8f,
                        reference = parsed.reference,
                        isRecurring = parsed.isLikelySubscription
                    )

                    transactionRepository.insert(transaction)
                    transactionsSaved++
                    Log.d(TAG, "Saved transaction: ${parsed.amount} from ${parsed.merchantName}")

                } catch (e: Exception) {
                    Log.e(TAG, "Error processing SMS", e)
                    errors++
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning SMS", e)
            errors++
        }

        return SmsScanResult(
            totalSmsScanned = totalSmsScanned,
            transactionsFound = transactionsFound,
            transactionsSaved = transactionsSaved,
            duplicatesSkipped = duplicatesSkipped,
            errors = errors
        )
    }

    /**
     * Check if the SMS sender is from a bank or payment service.
     */
    fun isBankSms(sender: String): Boolean {
        val upperSender = sender.uppercase()
        return BANK_SENDERS.any { upperSender.contains(it) } ||
                upperSender.contains("BANK") ||
                upperSender.contains("UPI") ||
                upperSender.matches(Regex(".*[A-Z]{2}-[A-Z]+.*")) // Pattern like "AD-HDFCBK"
    }
}
