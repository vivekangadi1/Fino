package com.fino.app.service.prediction

import com.fino.app.data.repository.RecurringRuleRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.RecurringFrequency
import com.fino.app.domain.model.RecurringRule
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionType
import com.fino.app.service.pattern.PatternDetectionService
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Predicted expense for upcoming month
 */
data class PredictedExpense(
    val merchantName: String,
    val displayName: String,
    val amount: Double,
    val expectedDate: LocalDate,
    val frequency: RecurringFrequency,
    val confidence: Float,
    val source: PredictionSource,
    val categoryId: Long? = null
)

enum class PredictionSource {
    CONFIRMED_RULE,     // From user-confirmed recurring rule
    DETECTED_PATTERN,   // From auto-detected pattern
    HISTORICAL_AVERAGE  // From historical spending average
}

/**
 * New subscription detected in recent months
 */
data class NewSubscription(
    val merchantName: String,
    val displayName: String,
    val amount: Double,
    val firstSeenDate: LocalDate,
    val occurrenceCount: Int,
    val detectedFrequency: RecurringFrequency?,
    val confidence: Float
)

/**
 * Dormant subscription alert
 */
data class DormantSubscription(
    val merchantName: String,
    val displayName: String,
    val expectedAmount: Double,
    val lastTransactionDate: LocalDate,
    val missedPayments: Int,
    val status: DormantStatus
)

enum class DormantStatus {
    POSSIBLY_CANCELLED,   // 2+ missed payments - may have been cancelled
    PAYMENT_ISSUE,        // 1 missed payment - may be a payment issue
    INACTIVE              // No recent activity
}

/**
 * Service for analyzing recurring patterns and predicting future expenses.
 * Complements PatternDetectionService with prediction and alerting capabilities.
 */
@Singleton
class RecurringPredictionService @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val recurringRuleRepository: RecurringRuleRepository,
    private val patternDetectionService: PatternDetectionService
) {
    companion object {
        const val ANALYSIS_MONTHS = 6
        const val NEW_SUBSCRIPTION_WINDOW_MONTHS = 2
        const val DORMANT_THRESHOLD_MISSED_PAYMENTS = 2
    }

    /**
     * Predict all expenses for the next month.
     * Combines confirmed recurring rules with detected patterns.
     */
    suspend fun predictNextMonthExpenses(): List<PredictedExpense> {
        val nextMonth = YearMonth.now().plusMonths(1)
        val predictions = mutableListOf<PredictedExpense>()

        // 1. Add confirmed recurring rules
        val activeRules = recurringRuleRepository.getActiveRulesWithNextExpected(nextMonth)
        for (rule in activeRules) {
            if (rule.nextExpected != null && YearMonth.from(rule.nextExpected) == nextMonth) {
                predictions.add(
                    PredictedExpense(
                        merchantName = rule.merchantPattern,
                        displayName = rule.merchantPattern,
                        amount = rule.expectedAmount,
                        expectedDate = rule.nextExpected,
                        frequency = rule.frequency,
                        confidence = 0.95f,
                        source = PredictionSource.CONFIRMED_RULE,
                        categoryId = rule.categoryId
                    )
                )
            }
        }

        // 2. Add detected patterns (not yet confirmed)
        val patterns = patternDetectionService.detectPatterns()
        for (pattern in patterns) {
            // Skip if already covered by confirmed rule
            val matchingRule = activeRules.find {
                it.merchantPattern.equals(pattern.merchantPattern, ignoreCase = true)
            }
            if (matchingRule != null) continue

            if (pattern.nextExpected != null && YearMonth.from(pattern.nextExpected) == nextMonth) {
                predictions.add(
                    PredictedExpense(
                        merchantName = pattern.merchantPattern,
                        displayName = pattern.displayName,
                        amount = pattern.averageAmount,
                        expectedDate = pattern.nextExpected,
                        frequency = pattern.detectedFrequency,
                        confidence = pattern.confidence,
                        source = PredictionSource.DETECTED_PATTERN,
                        categoryId = pattern.categoryId
                    )
                )
            }
        }

        return predictions.sortedBy { it.expectedDate }
    }

    /**
     * Identify new subscriptions/recurring expenses from the last 2 months.
     * Helps users spot new recurring charges they may not be aware of.
     */
    suspend fun identifyNewSubscriptions(): List<NewSubscription> {
        val transactions = transactionRepository.getAllTransactions()
            .filter { it.type == TransactionType.DEBIT }

        val now = LocalDate.now()
        val windowStart = now.minusMonths(NEW_SUBSCRIPTION_WINDOW_MONTHS.toLong())
        val historyStart = now.minusMonths(ANALYSIS_MONTHS.toLong())

        // Group by normalized merchant
        val merchantGroups = patternDetectionService.groupTransactionsByMerchant(transactions)

        val newSubscriptions = mutableListOf<NewSubscription>()

        for ((merchantName, merchantTransactions) in merchantGroups) {
            // Get transactions in the window
            val recentTransactions = merchantTransactions.filter {
                val date = it.transactionDate.toLocalDate()
                date.isAfter(windowStart) || date.isEqual(windowStart)
            }

            // Get transactions before the window
            val olderTransactions = merchantTransactions.filter {
                val date = it.transactionDate.toLocalDate()
                date.isBefore(windowStart) && (date.isAfter(historyStart) || date.isEqual(historyStart))
            }

            // If there are recent transactions but no older ones, it's a new subscription
            if (recentTransactions.size >= 2 && olderTransactions.isEmpty()) {
                val dates = recentTransactions.map { it.transactionDate.toLocalDate() }
                val amounts = recentTransactions.map { it.amount }
                val frequency = patternDetectionService.detectFrequency(dates)

                // Calculate confidence based on consistency
                val amountVariance = patternDetectionService.calculateAmountVariance(amounts)
                val confidence = if (frequency != null) {
                    patternDetectionService.calculateConfidence(
                        recentTransactions.size,
                        amountVariance,
                        0.8f // Assume reasonable interval consistency for new subscriptions
                    )
                } else {
                    0.6f
                }

                newSubscriptions.add(
                    NewSubscription(
                        merchantName = merchantName,
                        displayName = recentTransactions.first().merchantNormalized
                            ?: recentTransactions.first().merchantName,
                        amount = amounts.average(),
                        firstSeenDate = dates.minOrNull() ?: now,
                        occurrenceCount = recentTransactions.size,
                        detectedFrequency = frequency,
                        confidence = confidence
                    )
                )
            }
        }

        return newSubscriptions.sortedByDescending { it.confidence }
    }

    /**
     * Flag dormant subscriptions - recurring expenses that have stopped unexpectedly.
     * Helps users identify cancelled subscriptions or payment issues.
     */
    suspend fun flagDormantSubscriptions(): List<DormantSubscription> {
        val activeRules = recurringRuleRepository.getActiveRules()
        val transactions = transactionRepository.getAllTransactions()
            .filter { it.type == TransactionType.DEBIT }

        val now = LocalDate.now()
        val dormantSubscriptions = mutableListOf<DormantSubscription>()

        for (rule in activeRules) {
            // Find transactions matching this rule
            val matchingTransactions = transactions.filter {
                val normalizedMerchant = it.merchantName.uppercase().trim()
                normalizedMerchant.contains(rule.merchantPattern.uppercase()) ||
                    rule.merchantPattern.uppercase().contains(normalizedMerchant)
            }.sortedByDescending { it.transactionDate }

            if (matchingTransactions.isEmpty()) {
                // No transactions found at all - use createdAt as startDate proxy
                val startDate = rule.lastOccurrence ?: rule.createdAt.toLocalDate()
                dormantSubscriptions.add(
                    DormantSubscription(
                        merchantName = rule.merchantPattern,
                        displayName = rule.merchantPattern, // Use merchantPattern as display name
                        expectedAmount = rule.expectedAmount,
                        lastTransactionDate = startDate,
                        missedPayments = calculateMissedPayments(startDate, rule.frequency, now),
                        status = DormantStatus.INACTIVE
                    )
                )
                continue
            }

            val lastTransaction = matchingTransactions.first()
            val lastTransactionDate = lastTransaction.transactionDate.toLocalDate()

            // Calculate expected next date based on frequency
            val expectedNextDate = calculateExpectedNextDate(lastTransactionDate, rule.frequency)

            // Check if payment is overdue
            if (now.isAfter(expectedNextDate)) {
                val missedPayments = calculateMissedPayments(lastTransactionDate, rule.frequency, now)

                if (missedPayments >= DORMANT_THRESHOLD_MISSED_PAYMENTS) {
                    dormantSubscriptions.add(
                        DormantSubscription(
                            merchantName = rule.merchantPattern,
                            displayName = rule.merchantPattern,
                            expectedAmount = rule.expectedAmount,
                            lastTransactionDate = lastTransactionDate,
                            missedPayments = missedPayments,
                            status = DormantStatus.POSSIBLY_CANCELLED
                        )
                    )
                } else if (missedPayments >= 1) {
                    dormantSubscriptions.add(
                        DormantSubscription(
                            merchantName = rule.merchantPattern,
                            displayName = rule.merchantPattern,
                            expectedAmount = rule.expectedAmount,
                            lastTransactionDate = lastTransactionDate,
                            missedPayments = missedPayments,
                            status = DormantStatus.PAYMENT_ISSUE
                        )
                    )
                }
            }
        }

        return dormantSubscriptions.sortedByDescending { it.missedPayments }
    }

    /**
     * Get a summary of recurring expense health.
     */
    suspend fun getRecurringHealthSummary(): RecurringHealthSummary {
        val predictions = predictNextMonthExpenses()
        val newSubscriptions = identifyNewSubscriptions()
        val dormantSubscriptions = flagDormantSubscriptions()

        val totalPredicted = predictions.sumOf { it.amount }
        val confirmedTotal = predictions.filter { it.source == PredictionSource.CONFIRMED_RULE }
            .sumOf { it.amount }
        val detectedTotal = predictions.filter { it.source == PredictionSource.DETECTED_PATTERN }
            .sumOf { it.amount }

        return RecurringHealthSummary(
            nextMonthPredictedTotal = totalPredicted,
            confirmedRecurringTotal = confirmedTotal,
            detectedPatternTotal = detectedTotal,
            predictedExpenseCount = predictions.size,
            newSubscriptionCount = newSubscriptions.size,
            dormantSubscriptionCount = dormantSubscriptions.size,
            potentialSavings = dormantSubscriptions
                .filter { it.status == DormantStatus.POSSIBLY_CANCELLED }
                .sumOf { it.expectedAmount }
        )
    }

    private fun calculateExpectedNextDate(lastDate: LocalDate, frequency: RecurringFrequency): LocalDate {
        return when (frequency) {
            RecurringFrequency.ONE_TIME -> lastDate // One-time doesn't repeat
            RecurringFrequency.WEEKLY -> lastDate.plusWeeks(1)
            RecurringFrequency.MONTHLY -> lastDate.plusMonths(1)
            RecurringFrequency.YEARLY -> lastDate.plusYears(1)
        }
    }

    private fun calculateMissedPayments(lastDate: LocalDate, frequency: RecurringFrequency, now: LocalDate): Int {
        val daysSinceLastPayment = ChronoUnit.DAYS.between(lastDate, now)

        val expectedInterval = when (frequency) {
            RecurringFrequency.ONE_TIME -> Long.MAX_VALUE // Never considered missed
            RecurringFrequency.WEEKLY -> 7L
            RecurringFrequency.MONTHLY -> 30L
            RecurringFrequency.YEARLY -> 365L
        }

        // Add grace period of 5 days
        val gracePeriod = 5L
        val adjustedDays = (daysSinceLastPayment - gracePeriod).coerceAtLeast(0)

        return (adjustedDays / expectedInterval).toInt()
    }
}

/**
 * Summary of recurring expense health
 */
data class RecurringHealthSummary(
    val nextMonthPredictedTotal: Double,
    val confirmedRecurringTotal: Double,
    val detectedPatternTotal: Double,
    val predictedExpenseCount: Int,
    val newSubscriptionCount: Int,
    val dormantSubscriptionCount: Int,
    val potentialSavings: Double
)
