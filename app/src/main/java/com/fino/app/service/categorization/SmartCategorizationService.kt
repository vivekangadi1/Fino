package com.fino.app.service.categorization

import com.fino.app.domain.model.MatchType
import com.fino.app.ml.matcher.MerchantMatcher
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Smart multi-tier categorization service.
 *
 * Categorization tiers (in priority order):
 * 1. Exact merchant match (0.98 confidence)
 * 2. Keyword match (0.85-0.92 confidence)
 * 3. Fuzzy merchant match (0.70-0.95 confidence)
 * 4. Pattern inference (0.62-0.72 confidence)
 * 5. Fallback to "Other" category (0.50 confidence)
 */
@Singleton
class SmartCategorizationService @Inject constructor(
    private val merchantMatcher: MerchantMatcher,
    private val keywordMatcher: KeywordMatcher,
    private val patternAnalyzer: PatternAnalyzer,
    private val merchantAliasLearner: MerchantAliasLearner,
    private val categorizationAnalytics: CategorizationAnalytics
) {

    /**
     * Categorize a transaction using multi-tier approach.
     *
     * @param normalizedMerchantName Normalized merchant name (from MerchantNormalizer)
     * @param amount Transaction amount
     * @param transactionTime Transaction timestamp
     * @param smsBody Full SMS body text
     * @return Categorization result with category, confidence, and method used
     */
    suspend fun categorize(
        normalizedMerchantName: String,
        amount: Double,
        transactionTime: LocalDateTime,
        smsBody: String
    ): CategorizationResult {

        // Tier 1: Exact merchant match (highest confidence)
        val merchantMatch = merchantMatcher.findMatch(normalizedMerchantName)
        if (merchantMatch.matchType == MatchType.EXACT && merchantMatch.mapping != null) {
            // Record match usage for analytics
            merchantAliasLearner.recordMatch(merchantMatch.mapping)

            return CategorizationResult(
                categoryId = merchantMatch.mapping.categoryId,
                confidence = 0.98f,
                method = "Exact Match",
                suggestedName = merchantMatch.mapping.normalizedName,
                tier = 1
            )
        }

        // Tier 2: Keyword match (high confidence)
        val keywordMatch = keywordMatcher.matchKeywords(normalizedMerchantName, smsBody)
        if (keywordMatch != null) {
            return CategorizationResult(
                categoryId = keywordMatch.categoryId,
                confidence = keywordMatch.confidence,
                method = "Keyword Match (${keywordMatch.matchedKeyword})",
                suggestedName = normalizedMerchantName,
                tier = 2
            )
        }

        // Tier 3: Fuzzy merchant match (medium-high confidence)
        if (merchantMatch.matchType == MatchType.FUZZY && merchantMatch.mapping != null) {
            // Learn from fuzzy match - create alias for future exact matches
            merchantAliasLearner.learnFromFuzzyMatch(
                merchantVariant = normalizedMerchantName,
                baseMapping = merchantMatch.mapping
            )

            // Adjust confidence slightly lower than exact match
            val adjustedConfidence = merchantMatch.confidence * 0.9f
            return CategorizationResult(
                categoryId = merchantMatch.mapping.categoryId,
                confidence = adjustedConfidence,
                method = "Fuzzy Match",
                suggestedName = merchantMatch.mapping.normalizedName,
                tier = 3
            )
        }

        // Tier 4: Pattern-based inference (medium confidence)
        val patternInference = patternAnalyzer.inferFromContext(amount, transactionTime, smsBody)
        if (patternInference != null) {
            return CategorizationResult(
                categoryId = patternInference.categoryId,
                confidence = patternInference.confidence,
                method = "Pattern Inference (${patternInference.reason})",
                suggestedName = normalizedMerchantName,
                tier = 4
            )
        }

        // Tier 5: Fallback to "Other" category (low confidence)
        return CategorizationResult(
            categoryId = 15L,  // "Other" category
            confidence = 0.50f,
            method = "Default Fallback",
            suggestedName = normalizedMerchantName,
            tier = 5
        )
    }

    /**
     * Get categorization statistics for debugging/analytics.
     */
    fun getCategorizationStats(result: CategorizationResult): String {
        return "Tier ${result.tier}: ${result.method} (${result.confidence * 100}% confidence)"
    }

    /**
     * Track categorization analytics after transaction is saved.
     * Call this after transactionRepository.insert() to record the categorization event.
     */
    fun trackCategorization(
        transactionId: Long,
        merchantName: String,
        result: CategorizationResult
    ) {
        categorizationAnalytics.trackCategorization(
            transactionId = transactionId,
            merchantName = merchantName,
            categoryId = result.categoryId,
            confidence = result.confidence,
            tier = result.tier
        )
    }

    /**
     * Handle user correction when they manually change a transaction's category.
     * This triggers:
     * 1. Analytics tracking for the correction
     * 2. Learning from the user's choice to improve future categorizations
     *
     * Call this from ViewModels when user manually changes category.
     *
     * @param transactionId ID of the transaction being corrected
     * @param merchantName Merchant name from the transaction
     * @param originalCategoryId Original auto-assigned category (null if never categorized)
     * @param correctedCategoryId Category ID the user selected
     * @param displayName Optional display name for the merchant
     */
    suspend fun handleUserCorrection(
        transactionId: Long,
        merchantName: String,
        originalCategoryId: Long?,
        correctedCategoryId: Long,
        displayName: String? = null
    ) {
        // Track correction in analytics
        categorizationAnalytics.trackUserCorrection(
            transactionId = transactionId,
            merchantName = merchantName,
            originalCategoryId = originalCategoryId,
            correctedCategoryId = correctedCategoryId
        )

        // Learn from user's choice
        merchantAliasLearner.learnFromUserCorrection(
            merchantName = merchantName,
            categoryId = correctedCategoryId,
            displayName = displayName
        )
    }
}

/**
 * Result of smart categorization.
 *
 * @param categoryId The assigned category ID
 * @param confidence Confidence score (0.0 to 1.0)
 * @param method The categorization method used
 * @param suggestedName Suggested merchant display name
 * @param tier Which tier matched (1-5)
 */
data class CategorizationResult(
    val categoryId: Long,
    val confidence: Float,
    val method: String,
    val suggestedName: String,
    val tier: Int
)
