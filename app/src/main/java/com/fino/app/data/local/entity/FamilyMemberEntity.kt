package com.fino.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for family members.
 * Used to track who paid for expenses across all events.
 */
@Entity(tableName = "family_members")
data class FamilyMemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val relationship: String? = null,
    val isDefault: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long  // Epoch millis
)
