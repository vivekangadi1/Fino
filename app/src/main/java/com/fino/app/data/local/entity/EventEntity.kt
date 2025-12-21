package com.fino.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fino.app.domain.model.EventStatus

@Entity(
    tableName = "events",
    indices = [
        Index("eventTypeId"),
        Index("status"),
        Index("startDate"),
        Index("endDate"),
        Index("isActive")
    ]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val emoji: String,
    val eventTypeId: Long,            // Soft FK to event_types table
    val budgetAmount: Double? = null,
    val alertAt75: Boolean = true,
    val alertAt100: Boolean = true,
    val startDate: Long,              // Epoch millis
    val endDate: Long? = null,        // Epoch millis, null = ongoing
    val status: EventStatus = EventStatus.ACTIVE,
    val isActive: Boolean = true,
    val createdAt: Long,              // Epoch millis
    val updatedAt: Long               // Epoch millis
)
