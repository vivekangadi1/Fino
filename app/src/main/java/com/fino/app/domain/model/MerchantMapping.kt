package com.fino.app.domain.model

import java.time.LocalDateTime

/**
 * Learned association between a merchant name and a category.
 * Used to auto-categorize repeat transactions from the same merchant.
 *
 * @property id Unique identifier
 * @property rawMerchantName Merchant name as it appears in SMS (normalized to uppercase)
 * @property normalizedName User-friendly display name
 * @property categoryId Foreign key to the category
 * @property subcategoryId Optional foreign key to subcategory
 * @property confidence Confidence score that increases with confirmed usage (0.0-1.0)
 * @property matchCount Number of times this mapping was confirmed by user
 * @property isFuzzyMatch Whether this mapping was created via fuzzy matching
 * @property createdAt When this mapping was first created
 * @property lastUsedAt When this mapping was last applied to a transaction
 */
data class MerchantMapping(
    val id: Long = 0,
    val rawMerchantName: String,
    val normalizedName: String,
    val categoryId: Long,
    val subcategoryId: Long? = null,
    val confidence: Float = 0.5f,
    val matchCount: Int = 1,
    val isFuzzyMatch: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastUsedAt: LocalDateTime = LocalDateTime.now()
)
