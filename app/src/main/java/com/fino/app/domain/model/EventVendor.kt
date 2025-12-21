package com.fino.app.domain.model

import java.time.LocalDateTime

/**
 * Vendor for event expenses (e.g., Caterer, Decorator, Photographer).
 * Tracks vendor contact info, quoted amounts, and payment status.
 *
 * @property id Unique identifier
 * @property eventId Foreign key to the parent Event
 * @property subCategoryId Optional foreign key to EventSubCategory
 * @property name Display name of the vendor
 * @property description Optional description of services
 * @property phone Contact phone number
 * @property email Contact email address
 * @property quotedAmount Amount quoted by the vendor
 * @property notes Additional notes about the vendor
 * @property createdAt When this record was created
 * @property updatedAt When this record was last updated
 */
data class EventVendor(
    val id: Long = 0,
    val eventId: Long,
    val subCategoryId: Long? = null,
    val name: String,
    val description: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val quotedAmount: Double? = null,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Whether this vendor has contact info
     */
    val hasContactInfo: Boolean
        get() = !phone.isNullOrBlank() || !email.isNullOrBlank()

    /**
     * Whether this vendor has a quoted amount
     */
    val hasQuote: Boolean
        get() = quotedAmount != null && quotedAmount > 0
}
