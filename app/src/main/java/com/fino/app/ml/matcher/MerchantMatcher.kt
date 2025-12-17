package com.fino.app.ml.matcher

import com.fino.app.data.repository.MerchantMappingRepository
import com.fino.app.domain.model.MatchType
import com.fino.app.domain.model.MerchantMapping
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Matches merchant names to categories using exact and fuzzy matching.
 */
@Singleton
class MerchantMatcher @Inject constructor(
    private val repository: MerchantMappingRepository
) {

    private val fuzzyMatcher = FuzzyMatcher()

    companion object {
        const val EXACT_MATCH_THRESHOLD = 0.95f
        const val FUZZY_MATCH_THRESHOLD = 0.7f
        const val AUTO_APPLY_THRESHOLD = 0.95f
    }

    /**
     * Find a match for the given merchant name.
     */
    suspend fun findMatch(merchantName: String): MerchantMatchResult {
        val normalizedName = merchantName.uppercase().trim()

        // Try exact match first
        val exactMatch = repository.findByRawName(normalizedName)
        if (exactMatch != null) {
            return MerchantMatchResult(
                matchType = MatchType.EXACT,
                mapping = exactMatch,
                confidence = exactMatch.confidence,
                requiresConfirmation = false
            )
        }

        // Try fuzzy match
        val allMappings = repository.findAllMappings()
        var bestMatch: MerchantMapping? = null
        var bestScore = 0.0f

        for (mapping in allMappings) {
            val score = calculateSimilarity(normalizedName, mapping.rawMerchantName)
            if (score > bestScore && score >= FUZZY_MATCH_THRESHOLD) {
                bestScore = score
                bestMatch = mapping
            }
        }

        return if (bestMatch != null) {
            MerchantMatchResult(
                matchType = MatchType.FUZZY,
                mapping = bestMatch,
                confidence = bestScore,
                requiresConfirmation = bestScore < AUTO_APPLY_THRESHOLD
            )
        } else {
            MerchantMatchResult(
                matchType = MatchType.NONE,
                mapping = null,
                confidence = 0f,
                requiresConfirmation = false
            )
        }
    }

    /**
     * Calculate similarity between two strings.
     */
    fun calculateSimilarity(a: String, b: String): Float {
        return fuzzyMatcher.calculateSimilarity(a, b)
    }

    /**
     * Confirm a fuzzy match and create a new mapping.
     */
    suspend fun confirmFuzzyMatch(merchantName: String, suggestedMapping: MerchantMapping) {
        val newMapping = MerchantMapping(
            rawMerchantName = merchantName.uppercase().trim(),
            normalizedName = suggestedMapping.normalizedName,
            categoryId = suggestedMapping.categoryId,
            subcategoryId = suggestedMapping.subcategoryId,
            confidence = 0.8f,  // Start with good confidence since user confirmed
            isFuzzyMatch = true
        )
        repository.insertMapping(newMapping)
    }

    /**
     * Reject a fuzzy match suggestion.
     * This doesn't create any mapping - the user will need to categorize manually.
     */
    suspend fun rejectFuzzyMatch(merchantName: String, suggestedMapping: MerchantMapping) {
        // Intentionally empty - we don't create a mapping when rejected
        // In the future, could track rejections to avoid suggesting the same match
    }

    /**
     * Create a new merchant mapping after manual categorization.
     */
    suspend fun createMapping(
        merchantName: String,
        displayName: String,
        categoryId: Long,
        subcategoryId: Long? = null
    ) {
        val mapping = MerchantMapping(
            rawMerchantName = merchantName.uppercase().trim(),
            normalizedName = displayName,
            categoryId = categoryId,
            subcategoryId = subcategoryId,
            confidence = 1.0f  // User explicitly set this
        )
        repository.insertMapping(mapping)
    }
}

/**
 * Result of merchant matching.
 */
data class MerchantMatchResult(
    val matchType: MatchType,
    val mapping: MerchantMapping?,
    val confidence: Float,
    val requiresConfirmation: Boolean
)
