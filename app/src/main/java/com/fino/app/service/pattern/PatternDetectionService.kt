package com.fino.app.service.pattern

import com.fino.app.data.repository.RecurringRuleRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.*
import com.fino.app.ml.matcher.MerchantMatcher
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Service for detecting recurring expense patterns from transaction history.
 * Uses frequency analysis and fuzzy merchant matching to identify patterns.
 */
@Singleton
class PatternDetectionService @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val recurringRuleRepository: RecurringRuleRepository,
    private val merchantMatcher: MerchantMatcher
) {
    companion object {
        const val MIN_OCCURRENCES = 2
        const val MAX_AMOUNT_VARIANCE = 0.2f
        const val MIN_CONFIDENCE = 0.7f
        const val ANALYSIS_MONTHS = 3
        const val FUZZY_MATCH_THRESHOLD = 0.8f

        // Frequency detection thresholds
        const val WEEKLY_MIN_DAYS = 5
        const val WEEKLY_MAX_DAYS = 9
        const val MONTHLY_MIN_DAYS = 25
        const val MONTHLY_MAX_DAYS = 35
        const val YEARLY_MIN_DAYS = 350
        const val YEARLY_MAX_DAYS = 380
    }

    /**
     * Detect recurring patterns from all transactions.
     * Filters out patterns that already have confirmed rules.
     */
    suspend fun detectPatterns(): List<PatternSuggestion> {
        val transactions = transactionRepository.getAllTransactions()
            .filter { it.type == TransactionType.DEBIT }

        if (transactions.isEmpty()) return emptyList()

        val merchantGroups = groupTransactionsByMerchant(transactions)
        val suggestions = mutableListOf<PatternSuggestion>()

        for ((merchantName, merchantTransactions) in merchantGroups) {
            // Skip if already has a confirmed rule
            val existingRule = recurringRuleRepository.findByMerchantPattern(merchantName)
            if (existingRule != null) continue

            // Need minimum occurrences
            if (merchantTransactions.size < MIN_OCCURRENCES) continue

            val dates = merchantTransactions.map { it.transactionDate.toLocalDate() }
            val amounts = merchantTransactions.map { it.amount }

            // Detect frequency pattern
            val frequency = detectFrequency(dates) ?: continue

            // Calculate metrics
            val amountVariance = calculateAmountVariance(amounts)
            val typicalDay = calculateTypicalDayOfPeriod(dates, frequency)
            val intervalConsistency = calculateIntervalConsistency(dates, frequency)
            val confidence = calculateConfidence(merchantTransactions.size, amountVariance, intervalConsistency)

            // Filter by confidence threshold
            if (confidence < MIN_CONFIDENCE) continue

            // Calculate next expected date
            val sortedDates = dates.sorted()
            val lastDate = sortedDates.last()
            val nextExpected = predictNextOccurrence(lastDate, frequency, typicalDay)

            // Get category from most common in transactions
            val categoryId = merchantTransactions
                .mapNotNull { it.categoryId }
                .groupBy { it }
                .maxByOrNull { it.value.size }
                ?.key

            suggestions.add(
                PatternSuggestion(
                    merchantPattern = merchantName,
                    displayName = merchantTransactions.first().merchantNormalized
                        ?: merchantTransactions.first().merchantName,
                    averageAmount = amounts.average(),
                    detectedFrequency = frequency,
                    typicalDayOfPeriod = typicalDay,
                    occurrenceCount = merchantTransactions.size,
                    confidence = confidence,
                    nextExpected = nextExpected,
                    categoryId = categoryId
                )
            )
        }

        return suggestions.sortedByDescending { it.confidence }
    }

    /**
     * Confirm a pattern suggestion and create a recurring rule.
     */
    suspend fun confirmPattern(suggestion: PatternSuggestion): RecurringRule {
        val rule = suggestion.toRecurringRule()
        val id = recurringRuleRepository.insert(rule)
        return rule.copy(id = id)
    }

    /**
     * Dismiss a pattern suggestion (don't create a rule).
     * Could be extended to track dismissed patterns to avoid re-suggesting.
     */
    suspend fun dismissPattern(suggestion: PatternSuggestion) {
        // Currently a no-op, but could store dismissed patterns in the future
    }

    /**
     * Group transactions by normalized merchant name.
     * Uses fuzzy matching to group similar merchant names together.
     */
    fun groupTransactionsByMerchant(transactions: List<Transaction>): Map<String, List<Transaction>> {
        // Filter only DEBIT transactions
        val debitTransactions = transactions.filter { it.type == TransactionType.DEBIT }

        val groups = mutableMapOf<String, MutableList<Transaction>>()
        val merchantMap = mutableMapOf<String, String>() // Original -> Canonical name

        for (transaction in debitTransactions) {
            val normalizedName = transaction.merchantName.uppercase().trim()

            // Check if we already have a similar merchant name
            var canonicalName = merchantMap[normalizedName]

            if (canonicalName == null) {
                // Check existing groups for fuzzy match
                for (existingName in groups.keys) {
                    val similarity = try {
                        merchantMatcher.calculateSimilarity(normalizedName, existingName)
                    } catch (e: Exception) {
                        // If matcher throws, compare directly
                        if (normalizedName == existingName) 1.0f else 0.0f
                    }

                    if (similarity >= FUZZY_MATCH_THRESHOLD) {
                        canonicalName = existingName
                        merchantMap[normalizedName] = existingName
                        break
                    }
                }
            }

            // If no match found, use normalized name as canonical
            if (canonicalName == null) {
                canonicalName = normalizedName
                merchantMap[normalizedName] = normalizedName
            }

            groups.getOrPut(canonicalName) { mutableListOf() }.add(transaction)
        }

        return groups
    }

    /**
     * Detect the recurrence frequency from a list of dates.
     */
    fun detectFrequency(dates: List<LocalDate>): RecurringFrequency? {
        if (dates.size < MIN_OCCURRENCES) return null

        val sortedDates = dates.sorted()

        // Calculate intervals between consecutive dates
        val intervals = mutableListOf<Long>()
        for (i in 1 until sortedDates.size) {
            val days = ChronoUnit.DAYS.between(sortedDates[i - 1], sortedDates[i])
            intervals.add(days)
        }

        if (intervals.isEmpty()) return null

        val avgInterval = intervals.average()

        return when {
            avgInterval >= WEEKLY_MIN_DAYS && avgInterval <= WEEKLY_MAX_DAYS -> RecurringFrequency.WEEKLY
            avgInterval >= MONTHLY_MIN_DAYS && avgInterval <= MONTHLY_MAX_DAYS -> RecurringFrequency.MONTHLY
            avgInterval >= YEARLY_MIN_DAYS && avgInterval <= YEARLY_MAX_DAYS -> RecurringFrequency.YEARLY
            else -> null
        }
    }

    /**
     * Calculate the coefficient of variation for amounts.
     * Returns a value between 0 (no variance) and higher for more variance.
     */
    fun calculateAmountVariance(amounts: List<Double>): Float {
        if (amounts.size <= 1) return 0.0f

        val mean = amounts.average()
        if (mean == 0.0) return 0.0f

        val variance = amounts.map { (it - mean) * (it - mean) }.average()
        val stdDev = sqrt(variance)

        // Coefficient of variation
        return (stdDev / mean).toFloat()
    }

    /**
     * Calculate the typical day of period for the pattern.
     * For monthly: day of month (1-31)
     * For weekly: day of week (1=Monday to 7=Sunday)
     * For yearly: day of month when the transaction typically occurs
     */
    fun calculateTypicalDayOfPeriod(dates: List<LocalDate>, frequency: RecurringFrequency): Int {
        if (dates.isEmpty()) return 1

        val days = when (frequency) {
            RecurringFrequency.WEEKLY -> dates.map { it.dayOfWeek.value }
            RecurringFrequency.MONTHLY, RecurringFrequency.YEARLY -> dates.map { it.dayOfMonth }
        }

        // Return the mode (most frequent day)
        return days.groupBy { it }
            .maxByOrNull { it.value.size }
            ?.key ?: 1
    }

    /**
     * Calculate how consistent the intervals are.
     * Returns a value from 0 to 1, where 1 is perfectly consistent.
     */
    private fun calculateIntervalConsistency(dates: List<LocalDate>, frequency: RecurringFrequency): Float {
        if (dates.size < 2) return 1.0f

        val sortedDates = dates.sorted()
        val intervals = mutableListOf<Long>()

        for (i in 1 until sortedDates.size) {
            intervals.add(ChronoUnit.DAYS.between(sortedDates[i - 1], sortedDates[i]))
        }

        val expectedInterval = when (frequency) {
            RecurringFrequency.WEEKLY -> 7L
            RecurringFrequency.MONTHLY -> 30L
            RecurringFrequency.YEARLY -> 365L
        }

        // Calculate how close each interval is to expected
        val deviations = intervals.map { abs(it - expectedInterval).toDouble() / expectedInterval }
        val avgDeviation = deviations.average()

        // Convert to 0-1 scale where 1 is perfect
        return (1.0 - avgDeviation.coerceIn(0.0, 1.0)).toFloat()
    }

    /**
     * Calculate confidence score based on multiple factors.
     */
    fun calculateConfidence(occurrences: Int, amountVariance: Float, intervalConsistency: Float): Float {
        // Occurrence factor: more occurrences = higher confidence
        // 2 occurrences = 0.6, 6+ occurrences = 1.0
        val occurrenceFactor = (0.6f + (occurrences - 2).coerceIn(0, 4) * 0.1f)

        // Amount consistency factor: lower variance = higher confidence
        // 0% variance = 1.0, 20%+ variance = 0.7
        val amountFactor = 1.0f - (amountVariance.coerceIn(0f, 0.3f) / 0.3f * 0.3f)

        // Interval consistency factor (already 0-1)
        val intervalFactor = intervalConsistency

        // Weighted average
        val confidence = (occurrenceFactor * 0.3f + amountFactor * 0.35f + intervalFactor * 0.35f)

        return confidence.coerceIn(0f, 1f)
    }

    /**
     * Predict the next occurrence date based on the last occurrence.
     */
    fun predictNextOccurrence(lastDate: LocalDate, frequency: RecurringFrequency, dayOfPeriod: Int): LocalDate {
        return when (frequency) {
            RecurringFrequency.WEEKLY -> {
                lastDate.plusWeeks(1)
            }
            RecurringFrequency.MONTHLY -> {
                val nextMonth = lastDate.plusMonths(1)
                val targetDay = dayOfPeriod.coerceAtMost(nextMonth.lengthOfMonth())
                nextMonth.withDayOfMonth(targetDay)
            }
            RecurringFrequency.YEARLY -> {
                val nextYear = lastDate.plusYears(1)
                val targetDay = dayOfPeriod.coerceAtMost(nextYear.lengthOfMonth())
                nextYear.withDayOfMonth(targetDay)
            }
        }
    }
}
