package com.fino.app.service.categorization

import android.util.Log
import com.fino.app.data.repository.MerchantMappingRepository
import com.fino.app.domain.model.MerchantMapping
import com.fino.app.util.MerchantNormalizer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Learns merchant name variations and creates aliases automatically.
 *
 * This service helps the system learn from:
 * 1. Successful fuzzy matches (e.g., "SWIGGY123" matched to "SWIGGY")
 * 2. User corrections (e.g., user manually categorizes unknown merchant)
 * 3. Pattern recognition (e.g., "Amazon Prime" is variant of "Amazon")
 */
@Singleton
class MerchantAliasLearner @Inject constructor(
    private val merchantMappingRepository: MerchantMappingRepository
) {

    companion object {
        private const val TAG = "MerchantAliasLearner"
        private const val ALIAS_CONFIDENCE = 0.95f
    }

    /**
     * Learn from a successful fuzzy match by creating an alias.
     * This way, next time the variant is seen, it will be an exact match.
     *
     * @param merchantVariant The variant merchant name that was matched
     * @param baseMapping The base mapping that was matched to
     */
    suspend fun learnFromFuzzyMatch(
        merchantVariant: String,
        baseMapping: MerchantMapping
    ) {
        val normalizedVariant = MerchantNormalizer.normalize(merchantVariant)

        // Don't create alias if variant is same as base
        if (normalizedVariant == baseMapping.rawMerchantName) {
            return
        }

        // Check if alias already exists
        val existingMapping = merchantMappingRepository.findByRawName(normalizedVariant)
        if (existingMapping != null) {
            Log.d(TAG, "Alias already exists for: $normalizedVariant")
            return
        }

        // Create new alias mapping
        val aliasMapping = MerchantMapping(
            rawMerchantName = normalizedVariant,
            normalizedName = baseMapping.normalizedName,
            categoryId = baseMapping.categoryId,
            confidence = ALIAS_CONFIDENCE,
            matchCount = 0,
            isFuzzyMatch = true,
            createdAt = java.time.LocalDateTime.now(),
            lastUsedAt = java.time.LocalDateTime.now()
        )

        try {
            merchantMappingRepository.insertMapping(aliasMapping)
            Log.d(TAG, "Learned alias: $normalizedVariant -> ${baseMapping.normalizedName} (Category: ${baseMapping.categoryId})")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create alias: $normalizedVariant", e)
        }
    }

    /**
     * Learn from user correction by creating a new merchant mapping.
     *
     * @param merchantName The merchant name from the transaction
     * @param categoryId The category ID the user selected
     * @param displayName Optional display name (defaults to merchantName)
     */
    suspend fun learnFromUserCorrection(
        merchantName: String,
        categoryId: Long,
        displayName: String? = null
    ) {
        val normalizedName = MerchantNormalizer.normalize(merchantName)

        // Check if mapping already exists
        val existingMapping = merchantMappingRepository.findByRawName(normalizedName)
        if (existingMapping != null) {
            // Update existing mapping with user's choice
            if (existingMapping.categoryId != categoryId) {
                Log.d(TAG, "User corrected category for $normalizedName: ${existingMapping.categoryId} -> $categoryId")
                val updatedMapping = existingMapping.copy(
                    categoryId = categoryId,
                    confidence = 1.0f,  // User confirmation = 100% confidence
                    matchCount = existingMapping.matchCount + 1,
                    lastUsedAt = java.time.LocalDateTime.now()
                )
                merchantMappingRepository.updateMapping(updatedMapping)
            }
            return
        }

        // Create new mapping from user correction
        val newMapping = MerchantMapping(
            rawMerchantName = normalizedName,
            normalizedName = displayName ?: MerchantNormalizer.extractBaseName(merchantName),
            categoryId = categoryId,
            confidence = 1.0f,  // User confirmation = 100% confidence
            matchCount = 1,
            isFuzzyMatch = false,
            createdAt = java.time.LocalDateTime.now(),
            lastUsedAt = java.time.LocalDateTime.now()
        )

        try {
            merchantMappingRepository.insertMapping(newMapping)
            Log.d(TAG, "Learned from user: $normalizedName -> Category $categoryId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to learn from user correction: $normalizedName", e)
        }
    }

    /**
     * Suggest potential aliases based on similarity to existing mappings.
     * This can be used to show users suggested aliases for review.
     *
     * @param merchantName The merchant name to find suggestions for
     * @return List of potential base mappings this could be an alias of
     */
    suspend fun suggestPotentialAliases(merchantName: String): List<MerchantMapping> {
        val normalizedName = MerchantNormalizer.normalize(merchantName)
        val allMappings = merchantMappingRepository.findAllMappings()

        val suggestions = allMappings.filter { mapping ->
            val baseName = MerchantNormalizer.extractBaseName(normalizedName)
            val mappingBaseName = MerchantNormalizer.extractBaseName(mapping.rawMerchantName)

            // Suggest if base names are similar
            baseName.contains(mappingBaseName) || mappingBaseName.contains(baseName)
        }

        return suggestions.take(5)  // Return top 5 suggestions
    }

    /**
     * Update match count for a mapping when it's used.
     * This helps track which mappings are most commonly used.
     */
    suspend fun recordMatch(mapping: MerchantMapping) {
        val updatedMapping = mapping.copy(
            matchCount = mapping.matchCount + 1,
            lastUsedAt = java.time.LocalDateTime.now()
        )

        try {
            merchantMappingRepository.updateMapping(updatedMapping)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update match count for: ${mapping.rawMerchantName}", e)
        }
    }
}
