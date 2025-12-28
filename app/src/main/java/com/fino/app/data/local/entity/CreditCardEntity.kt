package com.fino.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "credit_cards",
    indices = [
        Index("lastFourDigits"),
        Index("bankName"),
        Index("isActive")
    ]
)
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bankName: String,
    val cardName: String? = null,
    val lastFourDigits: String,
    val creditLimit: Double? = null,
    val billingCycleDay: Int? = null,
    val dueDateDay: Int? = null,
    val currentUnbilled: Double = 0.0,
    val previousDue: Double = 0.0,
    val previousDueDate: Long? = null, // Epoch millis (LocalDate)
    val minimumDue: Double? = null,
    val isActive: Boolean = true,
    val createdAt: Long,  // Epoch millis
    // Payment tracking fields
    val isPaid: Boolean = false,
    val paidDate: Long? = null,           // Epoch millis when paid
    val paidAmount: Double? = null,       // Actual amount paid
    // User override fields for manual adjustments
    val userAdjustedDue: Double? = null,  // User-overridden due amount
    val userAdjustedDueDate: Long? = null // User-overridden due date
)
