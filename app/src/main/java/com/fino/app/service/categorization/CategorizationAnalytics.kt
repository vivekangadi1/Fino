package com.fino.app.service.categorization

import android.util.Log
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.Transaction
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for tracking and analyzing categorization quality.
 *
 * This service helps monitor:
 * - Auto-categorization accuracy rates
 * - Tier distribution (which tiers are used most)
 * - User correction patterns
 * - Category distribution
 * - Merchant learning effectiveness
 */
@Singleton
class CategorizationAnalytics @Inject constructor(
    private val transactionRepository: TransactionRepository
) {

    companion object {
        private const val TAG = "CategorizationAnalytics"
    }

    // In-memory storage for categorization events (could be persisted to DB in future)
    private val categorizationEvents = mutableListOf<CategorizationEvent>()

    /**
     * Track a categorization event when a transaction is auto-categorized.
     */
    fun trackCategorization(
        transactionId: Long,
        merchantName: String,
        categoryId: Long,
        confidence: Float,
        tier: Int
    ) {
        val event = CategorizationEvent(
            transactionId = transactionId,
            merchantName = merchantName,
            originalCategoryId = categoryId,
            correctedCategoryId = categoryId,  // Same as original initially
            confidence = confidence,
            tier = tier
        )
        categorizationEvents.add(event)
        Log.d(TAG, "Tracked categorization: $merchantName -> Category $categoryId (Tier $tier, ${confidence * 100}%)")
    }

    /**
     * Track when a user corrects a categorization.
     */
    fun trackUserCorrection(
        transactionId: Long,
        merchantName: String,
        originalCategoryId: Long?,
        correctedCategoryId: Long
    ) {
        // Find existing event or create new one
        val existingEvent = categorizationEvents.find { it.transactionId == transactionId }
        if (existingEvent != null) {
            // Update existing event
            val index = categorizationEvents.indexOf(existingEvent)
            categorizationEvents[index] = existingEvent.copy(
                correctedCategoryId = correctedCategoryId
            )
        } else {
            // Create new event for manual categorization
            val event = CategorizationEvent(
                transactionId = transactionId,
                merchantName = merchantName,
                originalCategoryId = originalCategoryId,
                correctedCategoryId = correctedCategoryId,
                confidence = if (originalCategoryId == null) 0f else 1.0f,
                tier = 0  // Manual categorization
            )
            categorizationEvents.add(event)
        }

        Log.d(TAG, "User correction: $merchantName - $originalCategoryId -> $correctedCategoryId")
    }

    /**
     * Calculate categorization metrics for all time.
     */
    suspend fun calculateMetrics(): CategorizationMetrics {
        val allTransactions = transactionRepository.getAllTransactions()
        return calculateMetricsForTransactions(allTransactions)
    }

    /**
     * Calculate categorization metrics for a specific time period.
     */
    suspend fun calculateMetricsForPeriod(
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): CategorizationMetrics {
        val startMillis = startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val transactions = transactionRepository.getAllTransactions()
            .filter { transaction ->
                val txTime = transaction.transactionDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                txTime in startMillis..endMillis
            }

        return calculateMetricsForTransactions(transactions)
    }

    /**
     * Helper to calculate metrics from a list of transactions.
     */
    private fun calculateMetricsForTransactions(transactions: List<Transaction>): CategorizationMetrics {
        val totalTransactions = transactions.size
        val autoCategorized = transactions.count { it.categoryId != null && it.categoryId != 15L }
        val needsReview = transactions.count { it.needsReview }

        // Count user corrections from tracked events
        val userCorrected = categorizationEvents.count { event ->
            event.originalCategoryId != null &&
            event.originalCategoryId != event.correctedCategoryId &&
            transactions.any { it.id == event.transactionId }
        }

        // Calculate accuracy (auto-categorized correctly / total auto-categorized)
        val accuracyRate = if (autoCategorized > 0) {
            ((autoCategorized - userCorrected).toFloat() / autoCategorized)
        } else {
            0f
        }

        // Calculate review rate
        val reviewRate = if (totalTransactions > 0) {
            (needsReview.toFloat() / totalTransactions)
        } else {
            0f
        }

        // Category distribution
        val categoryDistribution = transactions
            .filter { it.categoryId != null }
            .groupBy { it.categoryId!! }
            .mapValues { it.value.size }

        // Tier distribution (from tracked events)
        val tierDistribution = categorizationEvents
            .filter { event -> transactions.any { it.id == event.transactionId } }
            .groupBy { it.tier }
            .mapValues { it.value.size }

        return CategorizationMetrics(
            totalTransactions = totalTransactions,
            autoCategorized = autoCategorized,
            needsReview = needsReview,
            userCorrected = userCorrected,
            accuracyRate = accuracyRate,
            reviewRate = reviewRate,
            categoryDistribution = categoryDistribution,
            tierDistribution = tierDistribution
        )
    }

    /**
     * Get categorization statistics for a specific period.
     */
    suspend fun getStatsForPeriod(period: String): CategorizationStats {
        val (startTime, endTime) = when (period) {
            "Today" -> {
                val now = LocalDateTime.now()
                val start = now.toLocalDate().atStartOfDay()
                start to now
            }
            "This Week" -> {
                val now = LocalDateTime.now()
                val start = now.minusDays(7)
                start to now
            }
            "This Month" -> {
                val now = LocalDateTime.now()
                val start = now.minusMonths(1)
                start to now
            }
            "All Time" -> {
                LocalDateTime.of(2020, 1, 1, 0, 0) to LocalDateTime.now()
            }
            else -> return CategorizationStats(
                period = period,
                metrics = CategorizationMetrics.empty(),
                topMerchants = emptyList(),
                tierBreakdown = emptyMap()
            )
        }

        val metrics = calculateMetricsForPeriod(startTime, endTime)
        val topMerchants = getTopMerchants(startTime, endTime)
        val tierBreakdown = metrics.tierDistribution.mapKeys { (tier, _) ->
            when (tier) {
                1 -> "Tier 1: Exact Match"
                2 -> "Tier 2: Keyword Match"
                3 -> "Tier 3: Fuzzy Match"
                4 -> "Tier 4: Pattern Inference"
                5 -> "Tier 5: Default Fallback"
                else -> "Manual Categorization"
            }
        }

        return CategorizationStats(
            period = period,
            metrics = metrics,
            topMerchants = topMerchants,
            tierBreakdown = tierBreakdown
        )
    }

    /**
     * Get top merchants by transaction count for a time period.
     */
    private suspend fun getTopMerchants(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        limit: Int = 10
    ): List<MerchantStat> {
        val startMillis = startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val transactions = transactionRepository.getAllTransactions()
            .filter { transaction ->
                val txTime = transaction.transactionDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                txTime in startMillis..endMillis
            }

        return transactions
            .groupBy { it.merchantNormalized ?: it.merchantName }
            .map { (merchantName, txList) ->
                val events = categorizationEvents.filter { event ->
                    txList.any { it.id == event.transactionId }
                }
                val avgConfidence = if (events.isNotEmpty()) {
                    events.map { it.confidence }.average().toFloat()
                } else {
                    0f
                }
                val mostCommonTier = events.groupBy { it.tier }
                    .maxByOrNull { it.value.size }?.key ?: 0

                MerchantStat(
                    merchantName = merchantName,
                    categoryId = txList.first().categoryId ?: 15L,
                    transactionCount = txList.size,
                    totalAmount = txList.sumOf { it.amount },
                    averageConfidence = avgConfidence,
                    tier = mostCommonTier
                )
            }
            .sortedByDescending { it.transactionCount }
            .take(limit)
    }

    /**
     * Get summary statistics as a human-readable string.
     */
    suspend fun getSummary(): String {
        val metrics = calculateMetrics()
        return buildString {
            appendLine("=== Categorization Analytics ===")
            appendLine("Total Transactions: ${metrics.totalTransactions}")
            appendLine("Auto-Categorized: ${metrics.autoCategorized}")
            appendLine("Needs Review: ${metrics.needsReview}")
            appendLine("User Corrections: ${metrics.userCorrected}")
            appendLine("Accuracy Rate: ${String.format("%.1f%%", metrics.effectiveAccuracy())}")
            appendLine("Review Rate: ${String.format("%.1f%%", metrics.reviewPercentage())}")
            appendLine("Tier 1 (Exact Match): ${String.format("%.1f%%", metrics.tier1Percentage())}")
            appendLine()
            appendLine("Tier Distribution:")
            metrics.tierDistribution.toSortedMap().forEach { (tier, count) ->
                val tierName = when (tier) {
                    1 -> "Exact Match"
                    2 -> "Keyword Match"
                    3 -> "Fuzzy Match"
                    4 -> "Pattern Inference"
                    5 -> "Default Fallback"
                    else -> "Manual"
                }
                val percentage = (count.toFloat() / metrics.totalTransactions) * 100
                appendLine("  Tier $tier ($tierName): $count (${String.format("%.1f%%", percentage)})")
            }
        }
    }
}
