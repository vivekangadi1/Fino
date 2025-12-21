package com.fino.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for event vendors.
 * Vendors are service providers for events (e.g., Caterer, Photographer).
 */
@Entity(
    tableName = "event_vendors",
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EventSubCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["subCategoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("eventId"),
        Index("subCategoryId")
    ]
)
data class EventVendorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventId: Long,
    val subCategoryId: Long? = null,
    val name: String,
    val description: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val quotedAmount: Double? = null,
    val notes: String? = null,
    val createdAt: Long,   // Epoch millis
    val updatedAt: Long    // Epoch millis
)
