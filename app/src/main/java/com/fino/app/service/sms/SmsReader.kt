package com.fino.app.service.sms

import java.time.YearMonth

/**
 * Interface for reading SMS messages from the device.
 * Abstracted for testability.
 */
interface SmsReader {
    /**
     * Read all SMS messages for a given month.
     */
    suspend fun readSmsForMonth(yearMonth: YearMonth): List<SmsMessage>
}
