package com.fino.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "event_types",
    indices = [
        Index("isSystem"),
        Index("isActive"),
        Index("sortOrder")
    ]
)
data class EventTypeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val emoji: String,
    val isSystem: Boolean = false,
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long  // Epoch millis
)
