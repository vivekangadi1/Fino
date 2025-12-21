package com.fino.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fino.app.domain.model.PaymentStatus
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
        Index("needsReview"),
        Index("bankName"),
        Index("paymentMethod"),
        Index("eventId"),
        Index("eventSubCategoryId"),
        Index("eventVendorId"),
        Index("paymentStatus")
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
    val reference: String? = null,
    val bankName: String? = null,        // "HDFC", "ICICI", "SBI", "AXIS"
    val paymentMethod: String? = null,   // "UPI", "CREDIT_CARD"
    val cardLastFour: String? = null,    // Last 4 digits for credit cards
    val eventId: Long? = null,           // Soft FK to events table

    // Event expense tracking fields
    val eventSubCategoryId: Long? = null,   // Soft FK to event_sub_categories
    val eventVendorId: Long? = null,        // Soft FK to event_vendors
    val paidBy: String? = null,             // Who paid for this expense
    val isAdvancePayment: Boolean = false,  // Whether this is an advance/partial payment
    val dueDate: Long? = null,              // Due date for pending payments (epoch millis)
    val expenseNotes: String? = null,       // Additional notes about the expense
    val paymentStatus: PaymentStatus = PaymentStatus.PAID  // Payment status
)
