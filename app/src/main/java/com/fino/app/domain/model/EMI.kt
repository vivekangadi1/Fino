package com.fino.app.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * EMI (Equated Monthly Installment) tracking for credit card and store purchases.
 */
data class EMI(
    val id: Long = 0,
    val creditCardId: Long? = null,         // Null for non-CC EMI (bank EMI)
    val description: String,                 // "iPhone 15 Pro", "Laptop EMI"
    val merchantName: String? = null,        // Store where purchased
    val originalAmount: Double,              // Total purchase amount
    val monthlyAmount: Double,               // EMI amount per month
    val tenure: Int,                         // Total months
    val paidCount: Int = 0,                  // Number of EMIs paid
    val startDate: LocalDate,
    val endDate: LocalDate,                  // Calculated: startDate + tenure months
    val nextDueDate: LocalDate,
    val interestRate: Float? = null,         // Interest rate if applicable
    val processingFee: Double? = null,
    val status: EMIStatus = EMIStatus.ACTIVE,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    val remainingCount: Int
        get() = tenure - paidCount

    val remainingAmount: Double
        get() = monthlyAmount * remainingCount

    val paidAmount: Double
        get() = monthlyAmount * paidCount

    val progressPercent: Float
        get() = if (tenure > 0) (paidCount.toFloat() / tenure) else 0f

    val isCompleted: Boolean
        get() = status == EMIStatus.COMPLETED || paidCount >= tenure
}

enum class EMIStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED
}

/**
 * Loan tracking for bank loans (home, car, personal, education).
 */
data class Loan(
    val id: Long = 0,
    val type: LoanType,
    val bankName: String,
    val accountNumber: String? = null,       // Loan account number
    val description: String,                 // "Home Loan - Dream House"
    val principalAmount: Double,             // Original loan amount
    val interestRate: Float,                 // Annual interest rate
    val monthlyEMI: Double,
    val tenure: Int,                         // Total months
    val paidCount: Int = 0,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val nextDueDate: LocalDate,
    val outstandingPrincipal: Double? = null,
    val status: LoanStatus = LoanStatus.ACTIVE,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    val remainingCount: Int
        get() = tenure - paidCount

    val remainingAmount: Double
        get() = monthlyEMI * remainingCount

    val paidAmount: Double
        get() = monthlyEMI * paidCount

    val progressPercent: Float
        get() = if (tenure > 0) (paidCount.toFloat() / tenure) else 0f

    val isCompleted: Boolean
        get() = status == LoanStatus.COMPLETED || paidCount >= tenure
}

enum class LoanType(val displayName: String, val emoji: String) {
    HOME("Home Loan", "🏠"),
    CAR("Car Loan", "🚗"),
    PERSONAL("Personal Loan", "💰"),
    EDUCATION("Education Loan", "🎓"),
    GOLD("Gold Loan", "🪙"),
    OTHER("Other Loan", "📄")
}

enum class LoanStatus {
    ACTIVE,
    COMPLETED,
    CLOSED,
    FORECLOSED
}
