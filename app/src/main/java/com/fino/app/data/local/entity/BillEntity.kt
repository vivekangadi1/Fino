package com.fino.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fino.app.domain.model.BillEntitySource
import com.fino.app.domain.model.BillEntityStatus

@Entity(
    tableName = "bills",
    indices = [
        Index("accountId"),
        Index("creditCardId"),
        Index("dueDate"),
        Index("status")
    ]
)
data class BillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long? = null,
    val creditCardId: Long? = null,
    val cycleStart: Long,
    val cycleEnd: Long,
    val dueDate: Long,
    val totalDue: Double,
    val minDue: Double? = null,
    val paidAt: Long? = null,
    val paidAmount: Double? = null,
    val status: BillEntityStatus = BillEntityStatus.PENDING,
    val source: BillEntitySource = BillEntitySource.CC_STATEMENT,
    val payeeVpa: String? = null,
    val payeeName: String? = null,
    val updatedAt: Long
)
