package com.fino.app.service.sms

/**
 * Result of scanning SMS messages for transactions and bills.
 */
data class SmsScanResult(
    val totalSmsScanned: Int = 0,
    val transactionsFound: Int = 0,
    val transactionsSaved: Int = 0,
    val duplicatesSkipped: Int = 0,
    val billsUpdated: Int = 0,
    val errors: Int = 0
)
