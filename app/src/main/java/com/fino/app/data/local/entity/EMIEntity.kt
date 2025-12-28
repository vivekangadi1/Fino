package com.fino.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fino.app.domain.model.EMIStatus

@Entity(tableName = "emis")
data class EMIEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val creditCardId: Long? = null,
    val description: String,
    val merchantName: String? = null,
    val originalAmount: Double,
    val monthlyAmount: Double,
    val tenure: Int,
    val paidCount: Int = 0,
    val startDate: Long,
    val endDate: Long,
    val nextDueDate: Long,
    val interestRate: Float? = null,
    val processingFee: Double? = null,
    val status: EMIStatus = EMIStatus.ACTIVE,
    val notes: String? = null,
    val createdAt: Long
)
