package com.fino.app.domain.model

import java.time.LocalDateTime

/**
 * Family member for tracking who paid for expenses.
 * Reusable across all events.
 *
 * @property id Unique identifier
 * @property name Display name of the family member
 * @property relationship Optional relationship description (e.g., "Self", "Father", "Mother")
 * @property isDefault Whether this is the default payer
 * @property sortOrder Order for display purposes
 * @property createdAt When this record was created
 */
data class FamilyMember(
    val id: Long = 0,
    val name: String,
    val relationship: String? = null,
    val isDefault: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        /**
         * Default family members to suggest
         */
        val DEFAULT_MEMBERS = listOf(
            "Self" to "Self",
            "Father" to "Father",
            "Mother" to "Mother",
            "Brother" to "Brother",
            "Sister" to "Sister",
            "Spouse" to "Spouse",
            "In-Laws" to "In-Laws"
        )
    }
}
