package com.fino.app.domain.model

import java.time.LocalDateTime

/**
 * Type of event (built-in or user-defined)
 *
 * @property id Unique identifier
 * @property name Display name
 * @property emoji Visual emoji
 * @property isSystem Whether this is a built-in type
 * @property sortOrder Display order
 * @property isActive Whether this type is active
 * @property createdAt When this type was created
 */
data class EventType(
    val id: Long = 0,
    val name: String,
    val emoji: String,
    val isSystem: Boolean = false,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        /**
         * Built-in system event types
         */
        val SYSTEM_TYPES = listOf(
            EventType(id = 1, name = "Trip", emoji = "\u2708\uFE0F", isSystem = true, sortOrder = 1),
            EventType(id = 2, name = "Wedding", emoji = "\uD83D\uDC92", isSystem = true, sortOrder = 2),
            EventType(id = 3, name = "Renovation", emoji = "\uD83C\uDFE0", isSystem = true, sortOrder = 3),
            EventType(id = 4, name = "Party", emoji = "\uD83C\uDF89", isSystem = true, sortOrder = 4),
            EventType(id = 5, name = "Festival", emoji = "\uD83E\uDE94", isSystem = true, sortOrder = 5),
            EventType(id = 6, name = "Medical", emoji = "\uD83C\uDFE5", isSystem = true, sortOrder = 6),
            EventType(id = 7, name = "Education", emoji = "\uD83C\uDF93", isSystem = true, sortOrder = 7),
            EventType(id = 8, name = "Shopping Spree", emoji = "\uD83D\uDECD\uFE0F", isSystem = true, sortOrder = 8),
            EventType(id = 9, name = "Other", emoji = "\uD83D\uDCCC", isSystem = true, sortOrder = 99)
        )
    }
}
