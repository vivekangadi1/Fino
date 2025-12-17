package com.fino.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fino.app.domain.model.RecurringFrequency

@Entity(
    tableName = "recurring_rules",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("categoryId"),
        Index("merchantPattern"),
        Index("isActive")
    ]
)
data class RecurringRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val merchantPattern: String,
    val categoryId: Long,
    val expectedAmount: Double,
    val amountVariance: Float = 0.1f,
    val frequency: RecurringFrequency,
    val dayOfPeriod: Int? = null,
    val lastOccurrence: Long? = null,  // Epoch millis (LocalDate)
    val nextExpected: Long? = null,     // Epoch millis (LocalDate)
    val occurrenceCount: Int = 0,
    val isActive: Boolean = true,
    val isUserConfirmed: Boolean = false,
    val createdAt: Long  // Epoch millis
)
