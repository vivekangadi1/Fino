package com.fino.app.domain.model

/**
 * Hierarchical category for expense classification.
 * Categories can have parent categories to form a tree structure.
 *
 * @property id Unique identifier
 * @property name Display name of the category
 * @property emoji Emoji icon for visual identification
 * @property parentId Foreign key to parent category (null if top-level)
 * @property isSystem Whether this is a system-defined category vs user-created
 * @property budgetLimit Optional monthly budget limit for this category
 * @property sortOrder Display order within the parent
 * @property isActive Soft delete flag - false means category is hidden
 */
data class Category(
    val id: Long = 0,
    val name: String,
    val emoji: String,
    val parentId: Long? = null,
    val isSystem: Boolean = true,
    val budgetLimit: Double? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true
)
