package com.fino.app.service.sms

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.fino.app.data.repository.CreditCardRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.CreditCard
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionSource
import com.fino.app.service.categorization.SmartCategorizationService
import com.fino.app.service.parser.SmsParser
import com.fino.app.util.MerchantNormalizer
import com.fino.app.worker.RecurringPatternWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scans historical SMS messages from the device inbox and parses transactions.
 */
@Singleton
class SmsScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val smsReader: SmsReader,
    private val transactionRepository: TransactionRepository,
    private val creditCardRepository: CreditCardRepository,
    private val smsParser: SmsParser,
    private val smartCategorizationService: SmartCategorizationService
) {
    companion object {
        private const val TAG = "SmsScanner"

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

    /**
     * Scan all SMS messages for the given month and save parsed transactions.
     */
    suspend fun scanMonth(yearMonth: YearMonth): SmsScanResult {
        var totalSmsScanned = 0
        var transactionsFound = 0
        var transactionsSaved = 0
        var duplicatesSkipped = 0
        var billsUpdated = 0
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

                    // Try to parse as a credit card bill first
                    val parsedBill = smsParser.parseBill(sms.body)
                    if (parsedBill != null) {
                        if (processCreditCardBill(parsedBill)) {
                            billsUpdated++
                            Log.d(TAG, "Updated bill: ${parsedBill.bankName} ${parsedBill.cardLastFour} - ${parsedBill.totalDue} due ${parsedBill.dueDate}")
                        }
                        continue // Bill SMS processed, move to next
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

                    // Auto-categorize using smart categorization service
                    val normalizedMerchant = MerchantNormalizer.normalize(parsed.merchantName)
                    val categorizationResult = smartCategorizationService.categorize(
                        normalizedMerchantName = normalizedMerchant,
                        amount = parsed.amount,
                        transactionTime = parsed.transactionDate,
                        smsBody = sms.body
                    )

                    val categoryId = categorizationResult.categoryId
                    val categoryConfidence = categorizationResult.confidence

                    // Create and save transaction with category
                    val transaction = Transaction(
                        amount = parsed.amount,
                        type = parsed.type,
                        merchantName = parsed.merchantName,
                        merchantNormalized = categorizationResult.suggestedName,
                        categoryId = categoryId,
                        transactionDate = parsed.transactionDate,
                        source = TransactionSource.SMS_SCAN,
                        rawSmsBody = sms.body,
                        smsSender = sms.sender,
                        parsedConfidence = parsed.confidence,
                        needsReview = categoryId == 15L || categoryConfidence < 0.85f,
                        reference = parsed.reference,
                        isRecurring = parsed.isLikelySubscription,
                        bankName = parsed.bankName,
                        paymentMethod = when {
                            parsed.cardLastFour != null -> "CREDIT_CARD"
                            parsed.reference != null -> "UPI"
                            else -> null
                        },
                        cardLastFour = parsed.cardLastFour
                    )

                    val transactionId = transactionRepository.insert(transaction)
                    transactionsSaved++

                    // Track categorization for analytics
                    smartCategorizationService.trackCategorization(
                        transactionId = transactionId,
                        merchantName = parsed.merchantName,
                        result = categorizationResult
                    )

                    if (categoryId != 15L) {
                        Log.d(TAG, "Saved transaction: ${parsed.amount} from ${parsed.merchantName} - ${smartCategorizationService.getCategorizationStats(categorizationResult)}")
                    } else {
                        Log.d(TAG, "Saved transaction: ${parsed.amount} from ${parsed.merchantName} (No category match, needs review)")
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error processing SMS", e)
                    errors++
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning SMS", e)
            errors++
        }

        val result = SmsScanResult(
            totalSmsScanned = totalSmsScanned,
            transactionsFound = transactionsFound,
            transactionsSaved = transactionsSaved,
            duplicatesSkipped = duplicatesSkipped,
            billsUpdated = billsUpdated,
            errors = errors
        )

        // Trigger pattern detection if significant transactions were saved
        if (transactionsSaved >= 5) {
            triggerPatternDetection()
        }

        return result
    }

    /**
     * Trigger background pattern detection after a batch scan.
     * Uses a small delay to let the database settle.
     */
    private fun triggerPatternDetection() {
        Log.d(TAG, "Scheduling pattern detection after batch scan...")
        val workRequest = OneTimeWorkRequestBuilder<RecurringPatternWorker>()
            .setInitialDelay(30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
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

    /**
     * Process a parsed credit card bill and update the card's bill info.
     * If the card doesn't exist in the database, create it.
     * Returns true if the bill was processed successfully.
     */
    private suspend fun processCreditCardBill(bill: com.fino.app.service.parser.ParsedBill): Boolean {
        try {
            val bankName = bill.bankName ?: return false
            val cardLastFour = bill.cardLastFour

            // Skip if no card number available
            if (cardLastFour.isBlank()) {
                Log.d(TAG, "Skipping bill with no card number: $bankName")
                return false
            }

            // Try to find existing card
            var card = creditCardRepository.getByLastFourAndBank(cardLastFour, bankName)

            if (card == null) {
                // Create new card with basic info
                Log.d(TAG, "Creating new credit card: $bankName XX$cardLastFour")
                val newCard = CreditCard(
                    bankName = bankName,
                    cardName = "$bankName Credit Card",
                    lastFourDigits = cardLastFour,
                    creditLimit = 0.0, // Unknown
                    billingCycleDay = 1,
                    dueDateDay = bill.dueDate.dayOfMonth,
                    currentUnbilled = 0.0,
                    previousDue = bill.totalDue,
                    previousDueDate = bill.dueDate,
                    minimumDue = bill.minimumDue,
                    isActive = true,
                    createdAt = LocalDateTime.now()
                )
                creditCardRepository.insert(newCard)
                return true
            }

            // Update existing card's bill info if this bill is more recent
            val existingDueDate = card.previousDueDate
            if (existingDueDate == null || bill.dueDate.isAfter(existingDueDate) || bill.dueDate == existingDueDate) {
                creditCardRepository.updateBillInfo(
                    cardId = card.id,
                    totalDue = bill.totalDue,
                    minimumDue = bill.minimumDue,
                    dueDate = bill.dueDate
                )
                Log.d(TAG, "Updated bill for ${card.bankName} XX${card.lastFourDigits}: ${bill.totalDue} due ${bill.dueDate}")
                return true
            }

            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error processing credit card bill", e)
            return false
        }
    }
}
