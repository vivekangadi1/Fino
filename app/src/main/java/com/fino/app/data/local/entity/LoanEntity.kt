package com.fino.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fino.app.domain.model.LoanStatus
import com.fino.app.domain.model.LoanType

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: LoanType,
    val bankName: String,
    val accountNumber: String? = null,
    val description: String,
    val principalAmount: Double,
    val interestRate: Float,
    val monthlyEMI: Double,
    val tenure: Int,
    val paidCount: Int = 0,
    val startDate: Long,
    val endDate: Long,
    val nextDueDate: Long,
    val outstandingPrincipal: Double? = null,
    val status: LoanStatus = LoanStatus.ACTIVE,
    val notes: String? = null,
    val createdAt: Long
)
