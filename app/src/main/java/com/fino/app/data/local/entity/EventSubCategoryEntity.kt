package com.fino.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for event sub-categories.
 * Sub-categories allow detailed expense tracking within an event (e.g., Catering, Decoration).
 */
@Entity(
    tableName = "event_sub_categories",
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("eventId")
    ]
)
data class EventSubCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventId: Long,
    val name: String,
    val emoji: String = "📦",
    val budgetAmount: Double? = null,
    val sortOrder: Int = 0,
    val createdAt: Long  // Epoch millis
)
