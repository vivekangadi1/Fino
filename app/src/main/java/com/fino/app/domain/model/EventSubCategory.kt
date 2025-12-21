package com.fino.app.domain.model

import java.time.LocalDateTime

/**
 * Sub-category for event expenses (e.g., Decoration, Catering, Clothes for a wedding).
 * Each event can have multiple sub-categories with individual budgets.
 *
 * @property id Unique identifier
 * @property eventId Foreign key to the parent Event
 * @property name Display name of the sub-category
 * @property emoji Visual emoji for the sub-category
 * @property budgetAmount Optional budget limit for this sub-category
 * @property sortOrder Order for display purposes
 * @property createdAt When this record was created
 */
data class EventSubCategory(
    val id: Long = 0,
    val eventId: Long,
    val name: String,
    val emoji: String = "📦",
    val budgetAmount: Double? = null,
    val sortOrder: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Whether this sub-category has a budget set
     */
    val hasBudget: Boolean
        get() = budgetAmount != null && budgetAmount > 0

    companion object {
        /**
         * Default sub-categories suggested for different event types
         */
        val WEDDING_DEFAULTS = listOf(
            "Venue" to "🏛️",
            "Catering" to "🍽️",
            "Decoration" to "💐",
            "Photography" to "📸",
            "Clothes" to "👗",
            "Jewelry" to "💍",
            "Room/Stay" to "🏨",
            "Mehendi" to "🎨",
            "Pooja/Rituals" to "🪔",
            "Gifts" to "🎁",
            "Transport" to "🚗",
            "Miscellaneous" to "📦"
        )

        val TRIP_DEFAULTS = listOf(
            "Transport" to "✈️",
            "Accommodation" to "🏨",
            "Food" to "🍽️",
            "Activities" to "🎢",
            "Shopping" to "🛍️",
            "Miscellaneous" to "📦"
        )

        val RENOVATION_DEFAULTS = listOf(
            "Materials" to "🧱",
            "Labor" to "👷",
            "Electrical" to "💡",
            "Plumbing" to "🚿",
            "Painting" to "🎨",
            "Furniture" to "🪑",
            "Miscellaneous" to "📦"
        )

        val PARTY_DEFAULTS = listOf(
            "Venue" to "🏛️",
            "Food" to "🍽️",
            "Drinks" to "🍹",
            "Decoration" to "🎈",
            "Entertainment" to "🎵",
            "Miscellaneous" to "📦"
        )

        /**
         * Get default sub-categories for a given event type name
         */
        fun getDefaultsForEventType(eventTypeName: String): List<Pair<String, String>> {
            return when (eventTypeName.lowercase()) {
                "wedding", "marriage" -> WEDDING_DEFAULTS
                "trip", "vacation", "travel" -> TRIP_DEFAULTS
                "renovation", "home improvement" -> RENOVATION_DEFAULTS
                "party", "celebration", "birthday" -> PARTY_DEFAULTS
                else -> listOf("General" to "📦", "Miscellaneous" to "📦")
            }
        }
    }
}
