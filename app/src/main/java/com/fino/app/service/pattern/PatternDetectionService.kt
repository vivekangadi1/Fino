package com.fino.app.service.pattern

import com.fino.app.data.repository.RecurringRuleRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.*
import com.fino.app.ml.matcher.MerchantMatcher
import com.fino.app.service.parser.UpiTransactionParser
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
        const val MIN_OCCURRENCES = 2 // Lowered from 3 to detect patterns earlier
        const val MAX_AMOUNT_VARIANCE = 0.05f // 5% tolerance for fixed amount bills
        const val VARIABLE_BILL_MAX_VARIANCE = 0.5f // 50% tolerance for variable bills (utilities)
        const val MIN_CONFIDENCE = 0.55f // Lowered from 0.65 to include more suggestions
        const val ANALYSIS_MONTHS = 6 // Increased from 3 for better pattern detection
        const val FUZZY_MATCH_THRESHOLD = 0.8f

        // Frequency detection thresholds
        const val WEEKLY_MIN_DAYS = 5
        const val WEEKLY_MAX_DAYS = 9
        const val MONTHLY_MIN_DAYS = 25
        const val MONTHLY_MAX_DAYS = 35
        const val YEARLY_MIN_DAYS = 350
        const val YEARLY_MAX_DAYS = 380

        // Auto-confirmation thresholds
        const val AUTO_CONFIRM_CONFIDENCE_THRESHOLD = 0.90f
        const val AUTO_CONFIRM_MIN_OCCURRENCES = 4
        const val AUTO_CONFIRM_KNOWN_SUBSCRIPTION_MIN_OCCURRENCES = 2

        // Known variable bill merchants (utilities that vary in amount)
        val VARIABLE_BILL_PATTERNS = listOf(
            "electricity", "electric", "bescom", "mseb", "tpddl", "bses", "cesc",
            "water", "gas", "internet", "broadband", "airtel", "jio", "vi ", "bsnl",
            "mobile", "postpaid", "prepaid", "recharge"
        )
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

            // Determine if this is a variable bill (like utilities)
            val isVariableBill = isVariableBillMerchant(merchantName)
            val allowedVariance = if (isVariableBill) VARIABLE_BILL_MAX_VARIANCE else MAX_AMOUNT_VARIANCE

            // For variable bills, we're more lenient on amount variance but stricter on timing
            val confidence = if (isVariableBill) {
                calculateVariableBillConfidence(merchantTransactions.size, intervalConsistency)
            } else {
                calculateConfidence(merchantTransactions.size, amountVariance, intervalConsistency)
            }

            // Filter by confidence threshold
            if (confidence < MIN_CONFIDENCE) continue

            // For fixed bills, also check amount variance
            if (!isVariableBill && amountVariance > allowedVariance) continue

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

            val suggestion = PatternSuggestion(
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

            // Check if pattern should be auto-confirmed
            val isKnownSubscription = UpiTransactionParser.isKnownSubscription(merchantName)
            val shouldAutoConfirm = confidence >= AUTO_CONFIRM_CONFIDENCE_THRESHOLD && (
                merchantTransactions.size >= AUTO_CONFIRM_MIN_OCCURRENCES ||
                (isKnownSubscription && merchantTransactions.size >= AUTO_CONFIRM_KNOWN_SUBSCRIPTION_MIN_OCCURRENCES)
            )

            if (shouldAutoConfirm) {
                // Auto-confirm by creating a recurring rule directly
                try {
                    confirmPattern(suggestion)
                    // Don't add to suggestions since it's now a confirmed rule
                } catch (e: Exception) {
                    // If auto-confirm fails, add to suggestions for manual review
                    suggestions.add(suggestion)
                }
            } else {
                suggestions.add(suggestion)
            }
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
    @Suppress("UNUSED_PARAMETER")
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
     * For one-time: not applicable, returns 1
     */
    fun calculateTypicalDayOfPeriod(dates: List<LocalDate>, frequency: RecurringFrequency): Int {
        if (dates.isEmpty()) return 1

        val days = when (frequency) {
            RecurringFrequency.ONE_TIME -> return 1 // Not applicable for one-time bills
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
            RecurringFrequency.ONE_TIME -> return 1.0f // Not applicable for one-time
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
     * Check if merchant name matches known variable bill patterns (utilities).
     */
    private fun isVariableBillMerchant(merchantName: String): Boolean {
        val lowerName = merchantName.lowercase()
        return VARIABLE_BILL_PATTERNS.any { pattern -> lowerName.contains(pattern) }
    }

    /**
     * Calculate confidence for variable bills (timing-based, ignores amount variance).
     */
    private fun calculateVariableBillConfidence(occurrences: Int, intervalConsistency: Float): Float {
        // Occurrence factor: more occurrences = higher confidence
        // 3 occurrences = 0.65, 6+ occurrences = 0.95
        val occurrenceFactor = (0.65f + (occurrences - 3).coerceIn(0, 3) * 0.1f)

        // Interval consistency is more important for variable bills
        val intervalFactor = intervalConsistency

        // Weighted average - interval consistency is heavily weighted
        return (occurrenceFactor * 0.35f + intervalFactor * 0.65f).coerceIn(0f, 1f)
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
            RecurringFrequency.ONE_TIME -> {
                // One-time bills don't repeat - return last date
                lastDate
            }
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
