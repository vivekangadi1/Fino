package com.fino.app.service.sms

/**
 * Represents a raw SMS message read from the device inbox.
 */
data class SmsMessage(
    val id: String,
    val sender: String,
    val body: String,
    val timestamp: Long
)
