package com.fino.app.service.gmail

/**
 * Intermediate shape produced by GmailBillParser; converted to BillEntity by
 * GmailBillSyncWorker via BillRepository.upsertForCycle.
 */
data class ParsedBill(
    val bank: String,
    val last4: String?,
    val cycleStartMillis: Long?,
    val cycleEndMillis: Long,
    val dueDateMillis: Long,
    val totalDue: Double,
    val minDue: Double?,
    val payeeVpa: String?,
    val payeeName: String?
)
