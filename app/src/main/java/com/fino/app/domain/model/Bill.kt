package com.fino.app.domain.model

import com.fino.app.data.local.entity.BillEntity

enum class BillEntityStatus {
    PENDING,
    PAID,
    PARTIAL,
    OVERDUE
}

enum class BillEntitySource {
    CC_STATEMENT,
    EMAIL,
    AA,
    MANUAL
}

data class Bill(
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
    val updatedAt: Long = System.currentTimeMillis()
)

fun BillEntity.toDomain(): Bill = Bill(
    id = id,
    accountId = accountId,
    creditCardId = creditCardId,
    cycleStart = cycleStart,
    cycleEnd = cycleEnd,
    dueDate = dueDate,
    totalDue = totalDue,
    minDue = minDue,
    paidAt = paidAt,
    paidAmount = paidAmount,
    status = status,
    source = source,
    payeeVpa = payeeVpa,
    payeeName = payeeName,
    updatedAt = updatedAt
)

fun Bill.toEntity(): BillEntity = BillEntity(
    id = id,
    accountId = accountId,
    creditCardId = creditCardId,
    cycleStart = cycleStart,
    cycleEnd = cycleEnd,
    dueDate = dueDate,
    totalDue = totalDue,
    minDue = minDue,
    paidAt = paidAt,
    paidAmount = paidAmount,
    status = status,
    source = source,
    payeeVpa = payeeVpa,
    payeeName = payeeName,
    updatedAt = updatedAt
)
