package com.fino.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fino.app.domain.model.TransactionSource
import com.fino.app.domain.model.TransactionType

// Removed foreign key constraints in favor of soft references
// This allows transactions to be saved even if referenced entities don't exist
// Application layer handles relationship validation and graceful fallbacks
@Entity(
    tableName = "transactions",
    indices = [
        Index("categoryId"),
        Index("creditCardId"),
        Index("recurringRuleId"),
        Index("transactionDate"),
        Index("merchantName"),
        Index("needsReview")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val merchantName: String,
    val merchantNormalized: String? = null,
    val categoryId: Long? = null,
    val subcategoryId: Long? = null,
    val creditCardId: Long? = null,
    val isRecurring: Boolean = false,
    val recurringRuleId: Long? = null,
    val rawSmsBody: String? = null,
    val smsSender: String? = null,
    val parsedConfidence: Float = 0f,
    val needsReview: Boolean = true,
    val transactionDate: Long,  // Epoch millis
    val createdAt: Long,        // Epoch millis
    val source: TransactionSource = TransactionSource.SMS,
    val reference: String? = null
)
