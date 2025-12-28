package com.fino.app.data.repository

import com.fino.app.data.local.dao.RecurringRuleDao
import com.fino.app.data.local.entity.RecurringRuleEntity
import com.fino.app.domain.model.RecurringRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing recurring rules (subscriptions, EMIs, etc.)
 * Wraps the DAO and handles entity-domain model mapping
 */
@Singleton
class RecurringRuleRepository @Inject constructor(
    private val dao: RecurringRuleDao
) {
    /**
     * Get all active recurring rules as a Flow
     */
    fun getActiveRulesFlow(): Flow<List<RecurringRule>> {
        return dao.getActiveRulesFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Get all active recurring rules (one-time fetch)
     */
    suspend fun getActiveRules(): List<RecurringRule> {
        return dao.getActiveRules().map { it.toDomain() }
    }

    /**
     * Get rules with next expected date within the given range
     */
    suspend fun getUpcomingRules(startDate: LocalDate, endDate: LocalDate): List<RecurringRule> {
        return dao.getUpcomingRules(
            startDate.toEpochMillis(),
            endDate.toEpochMillis()
        ).map { it.toDomain() }
    }

    /**
     * Get a rule by its ID
     */
    suspend fun getById(id: Long): RecurringRule? {
        return dao.getById(id)?.toDomain()
    }

    /**
     * Find a rule by merchant pattern (adds wildcards for LIKE query)
     */
    suspend fun findByMerchantPattern(pattern: String): RecurringRule? {
        return dao.findByMerchantPattern("%$pattern%")?.toDomain()
    }

    /**
     * Insert a new recurring rule
     * @return The generated ID for the new rule
     */
    suspend fun insert(rule: RecurringRule): Long {
        return dao.insert(rule.toEntity())
    }

    /**
     * Update an existing recurring rule
     */
    suspend fun update(rule: RecurringRule) {
        dao.update(rule.toEntity())
    }

    /**
     * Delete a recurring rule
     */
    suspend fun delete(rule: RecurringRule) {
        dao.delete(rule.toEntity())
    }

    /**
     * Record an occurrence of this recurring expense
     * Updates last occurrence, next expected, and increments count
     */
    suspend fun recordOccurrence(id: Long, date: LocalDate, nextDate: LocalDate) {
        dao.recordOccurrence(
            id = id,
            date = date.toEpochMillis(),
            nextDate = nextDate.toEpochMillis()
        )
    }

    /**
     * Get the count of active recurring rules
     */
    suspend fun getActiveRuleCount(): Int {
        return dao.getActiveRuleCount()
    }

    /**
     * Deactivate a recurring rule (soft delete)
     * Used for one-time bills after they're paid, or when user wants to stop tracking
     */
    suspend fun deactivate(id: Long) {
        dao.deactivate(id)
    }

    /**
     * Get active rules with next expected date falling within the given month
     */
    suspend fun getActiveRulesWithNextExpected(month: java.time.YearMonth): List<RecurringRule> {
        val startDate = month.atDay(1)
        val endDate = month.atEndOfMonth()
        return dao.getUpcomingRules(
            startDate.toEpochMillis(),
            endDate.toEpochMillis()
        ).map { it.toDomain() }
    }

    // ==================== Mapping Functions ====================

    /**
     * Convert entity to domain model
     */
    private fun RecurringRuleEntity.toDomain(): RecurringRule {
        return RecurringRule(
            id = id,
            merchantPattern = merchantPattern,
            categoryId = categoryId,
            expectedAmount = expectedAmount,
            amountVariance = amountVariance,
            frequency = frequency,
            dayOfPeriod = dayOfPeriod,
            lastOccurrence = lastOccurrence?.toLocalDate(),
            nextExpected = nextExpected?.toLocalDate(),
            occurrenceCount = occurrenceCount,
            isActive = isActive,
            isUserConfirmed = isUserConfirmed,
            createdAt = createdAt.toLocalDateTime()
        )
    }

    /**
     * Convert domain model to entity
     */
    private fun RecurringRule.toEntity(): RecurringRuleEntity {
        return RecurringRuleEntity(
            id = id,
            merchantPattern = merchantPattern,
            categoryId = categoryId,
            expectedAmount = expectedAmount,
            amountVariance = amountVariance,
            frequency = frequency,
            dayOfPeriod = dayOfPeriod,
            lastOccurrence = lastOccurrence?.toEpochMillis(),
            nextExpected = nextExpected?.toEpochMillis(),
            occurrenceCount = occurrenceCount,
            isActive = isActive,
            isUserConfirmed = isUserConfirmed,
            createdAt = createdAt.toEpochMillis()
        )
    }

    // ==================== Date Conversion Helpers ====================

    private fun LocalDate.toEpochMillis(): Long {
        return this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun LocalDateTime.toEpochMillis(): Long {
        return this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun Long.toLocalDate(): LocalDate {
        return Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    private fun Long.toLocalDateTime(): LocalDateTime {
        return Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }
}
