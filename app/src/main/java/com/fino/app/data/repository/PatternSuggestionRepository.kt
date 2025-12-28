package com.fino.app.data.repository

import com.fino.app.data.local.dao.PatternSuggestionDao
import com.fino.app.data.local.entity.PatternSuggestionEntity
import com.fino.app.data.local.entity.SuggestionSource
import com.fino.app.data.local.entity.SuggestionStatus
import com.fino.app.domain.model.PatternSuggestion
import com.fino.app.domain.model.RecurringFrequency
import com.fino.app.service.parser.ParsedTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing pattern suggestions for automatic recurring bill detection.
 * Suggestions are created when:
 * 1. SMS parser detects a subscription payment (isLikelySubscription = true)
 * 2. Background pattern detection finds recurring patterns
 */
@Singleton
class PatternSuggestionRepository @Inject constructor(
    private val dao: PatternSuggestionDao,
    private val recurringRuleRepository: RecurringRuleRepository
) {
    /**
     * Get all pending suggestions as a Flow for reactive UI updates
     */
    fun getPendingSuggestionsFlow(): Flow<List<PatternSuggestion>> {
        return dao.getPendingSuggestionsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Get count of pending suggestions as a Flow
     */
    fun getPendingCountFlow(): Flow<Int> {
        return dao.getPendingCountFlow()
    }

    /**
     * Get all pending suggestions (one-time fetch)
     */
    suspend fun getPendingSuggestions(): List<PatternSuggestion> {
        return dao.getPendingSuggestions().map { it.toDomain() }
    }

    /**
     * Get a suggestion by ID
     */
    suspend fun getById(id: Long): PatternSuggestion? {
        return dao.getById(id)?.toDomain()
    }

    /**
     * Check if a suggestion exists for the given merchant pattern (not dismissed)
     */
    suspend fun exists(merchantPattern: String): Boolean {
        return dao.existsByMerchantPattern(merchantPattern)
    }

    /**
     * Create a suggestion from an SMS subscription detection.
     * Called when ParsedTransaction.isLikelySubscription = true
     *
     * @return The created suggestion, or null if one already exists
     */
    suspend fun createFromSubscriptionSms(
        parsedTransaction: ParsedTransaction,
        categoryId: Long?
    ): PatternSuggestion? {
        val merchantPattern = parsedTransaction.merchantName.uppercase()

        // Check if suggestion already exists
        if (dao.existsByMerchantPattern(merchantPattern)) {
            return null
        }

        // Check if a recurring rule already exists
        if (recurringRuleRepository.findByMerchantPattern(merchantPattern) != null) {
            return null
        }

        val now = System.currentTimeMillis()
        val nextMonth = LocalDate.now().plusMonths(1)

        val entity = PatternSuggestionEntity(
            merchantPattern = merchantPattern,
            displayName = parsedTransaction.merchantName,
            averageAmount = parsedTransaction.amount,
            frequency = RecurringFrequency.MONTHLY,  // Assume monthly for subscriptions
            typicalDayOfPeriod = parsedTransaction.transactionDate.dayOfMonth,
            occurrenceCount = 1,
            confidence = 0.85f,  // High confidence for known subscriptions
            nextExpected = nextMonth.withDayOfMonth(
                minOf(parsedTransaction.transactionDate.dayOfMonth, nextMonth.lengthOfMonth())
            ).toEpochMillis(),
            categoryId = categoryId,
            status = SuggestionStatus.PENDING,
            source = SuggestionSource.SMS_SUBSCRIPTION,
            createdAt = now
        )

        val id = dao.insert(entity)
        return if (id > 0) {
            dao.getById(id)?.toDomain()
        } else {
            null
        }
    }

    /**
     * Create a suggestion from pattern detection.
     * Called when PatternDetectionService detects a recurring pattern.
     *
     * @return The created suggestion, or null if one already exists
     */
    suspend fun createFromPatternDetection(pattern: PatternSuggestion): PatternSuggestion? {
        val merchantPattern = pattern.merchantPattern.uppercase()

        // Check if suggestion already exists
        if (dao.existsByMerchantPattern(merchantPattern)) {
            return null
        }

        // Check if a recurring rule already exists
        if (recurringRuleRepository.findByMerchantPattern(merchantPattern) != null) {
            return null
        }

        val now = System.currentTimeMillis()

        val entity = PatternSuggestionEntity(
            merchantPattern = merchantPattern,
            displayName = pattern.displayName,
            averageAmount = pattern.averageAmount,
            frequency = pattern.detectedFrequency,
            typicalDayOfPeriod = pattern.typicalDayOfPeriod,
            occurrenceCount = pattern.occurrenceCount,
            confidence = pattern.confidence,
            nextExpected = pattern.nextExpected.toEpochMillis(),
            categoryId = pattern.categoryId,
            status = SuggestionStatus.PENDING,
            source = SuggestionSource.PATTERN_DETECTION,
            createdAt = now
        )

        val id = dao.insert(entity)
        return if (id > 0) {
            dao.getById(id)?.toDomain()
        } else {
            null
        }
    }

    /**
     * Confirm a suggestion, creating a RecurringRule from it.
     *
     * @return The created RecurringRule ID
     */
    suspend fun confirmSuggestion(id: Long): Long {
        val entity = dao.getById(id) ?: return -1
        val suggestion = entity.toDomain()

        // Create a RecurringRule from the suggestion
        val rule = suggestion.toRecurringRule()
        val ruleId = recurringRuleRepository.insert(rule)

        // Update suggestion status to CONFIRMED
        dao.updateStatus(id, SuggestionStatus.CONFIRMED)

        return ruleId
    }

    /**
     * Dismiss a suggestion.
     */
    suspend fun dismissSuggestion(id: Long) {
        dao.updateStatus(id, SuggestionStatus.DISMISSED, System.currentTimeMillis())
    }

    /**
     * Delete a suggestion.
     */
    suspend fun delete(id: Long) {
        dao.delete(id)
    }

    /**
     * Clean up old dismissed suggestions (older than 30 days).
     */
    suspend fun cleanupOldDismissed() {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        dao.cleanupOldDismissed(thirtyDaysAgo)
    }

    // ==================== Mapping Functions ====================

    private fun PatternSuggestionEntity.toDomain(): PatternSuggestion {
        return PatternSuggestion(
            id = id,
            merchantPattern = merchantPattern,
            displayName = displayName,
            averageAmount = averageAmount,
            detectedFrequency = frequency,
            typicalDayOfPeriod = typicalDayOfPeriod,
            occurrenceCount = occurrenceCount,
            confidence = confidence,
            nextExpected = nextExpected.toLocalDate(),
            categoryId = categoryId
        )
    }

    // ==================== Date Conversion Helpers ====================

    private fun LocalDate.toEpochMillis(): Long {
        return this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun Long.toLocalDate(): LocalDate {
        return Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
}
