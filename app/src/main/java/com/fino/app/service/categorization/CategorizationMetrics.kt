package com.fino.app.service.categorization

/**
 * Metrics for tracking categorization quality and performance.
 */
data class CategorizationMetrics(
    val totalTransactions: Int,
    val autoCategorized: Int,
    val needsReview: Int,
    val userCorrected: Int,
    val accuracyRate: Float,  // (autoCategorized - userCorrected) / autoCategorized
    val reviewRate: Float,     // needsReview / totalTransactions
    val categoryDistribution: Map<Long, Int>,
    val tierDistribution: Map<Int, Int>  // Which tier matched (1-5)
) {
    companion object {
        fun empty() = CategorizationMetrics(
            totalTransactions = 0,
            autoCategorized = 0,
            needsReview = 0,
            userCorrected = 0,
            accuracyRate = 0f,
            reviewRate = 0f,
            categoryDistribution = emptyMap(),
            tierDistribution = emptyMap()
        )
    }

    /**
     * Calculate effective accuracy (accounts for user corrections).
     */
    fun effectiveAccuracy(): Float {
        return if (autoCategorized > 0) {
            ((autoCategorized - userCorrected).toFloat() / autoCategorized) * 100f
        } else {
            0f
        }
    }

    /**
     * Get percentage of transactions that needed review.
     */
    fun reviewPercentage(): Float {
        return if (totalTransactions > 0) {
            (needsReview.toFloat() / totalTransactions) * 100f
        } else {
            0f
        }
    }

    /**
     * Get tier 1 (exact match) percentage.
     */
    fun tier1Percentage(): Float {
        val tier1Count = tierDistribution[1] ?: 0
        return if (totalTransactions > 0) {
            (tier1Count.toFloat() / totalTransactions) * 100f
        } else {
            0f
        }
    }
}

/**
 * Categorization event for tracking user corrections.
 */
data class CategorizationEvent(
    val transactionId: Long,
    val merchantName: String,
    val originalCategoryId: Long?,
    val correctedCategoryId: Long,
    val confidence: Float,
    val tier: Int,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Categorization stats for a specific time period.
 */
data class CategorizationStats(
    val period: String,  // "Today", "This Week", "This Month", "All Time"
    val metrics: CategorizationMetrics,
    val topMerchants: List<MerchantStat>,
    val tierBreakdown: Map<String, Int>
)

/**
 * Statistics for a specific merchant.
 */
data class MerchantStat(
    val merchantName: String,
    val categoryId: Long,
    val transactionCount: Int,
    val totalAmount: Double,
    val averageConfidence: Float,
    val tier: Int
)
