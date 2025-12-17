package com.fino.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "merchant_mappings",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("rawMerchantName", unique = true),
        Index("categoryId"),
        Index("confidence")
    ]
)
data class MerchantMappingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rawMerchantName: String,
    val normalizedName: String,
    val categoryId: Long,
    val subcategoryId: Long? = null,
    val confidence: Float = 0.5f,
    val matchCount: Int = 1,
    val isFuzzyMatch: Boolean = false,
    val createdAt: Long,   // Epoch millis
    val lastUsedAt: Long   // Epoch millis
)
