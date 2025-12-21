package com.fino.app.domain.model

import java.time.LocalDate

/**
 * Daily spending summary for an event
 */
data class DailySpending(
    val date: LocalDate,
    val amount: Double,
    val transactionCount: Int
)
