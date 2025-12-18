package com.fino.app.service.sms

import android.content.Context
import android.provider.Telephony
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android implementation of SmsReader that reads from the system SMS inbox.
 */
@Singleton
class AndroidSmsReader @Inject constructor(
    @ApplicationContext private val context: Context
) : SmsReader {

    companion object {
        private const val TAG = "AndroidSmsReader"
    }

    override suspend fun readSmsForMonth(yearMonth: YearMonth): List<SmsMessage> {
        val messages = mutableListOf<SmsMessage>()

        try {
            // Calculate start and end timestamps for the month
            val startOfMonth = yearMonth.atDay(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val endOfMonth = yearMonth.atEndOfMonth()
                .atTime(23, 59, 59)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            // Query SMS inbox
            val projection = arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE
            )

            val selection = "${Telephony.Sms.DATE} >= ? AND ${Telephony.Sms.DATE} <= ?"
            val selectionArgs = arrayOf(startOfMonth.toString(), endOfMonth.toString())
            val sortOrder = "${Telephony.Sms.DATE} DESC"

            context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
                val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)

                while (cursor.moveToNext()) {
                    val id = cursor.getString(idIndex)
                    val sender = cursor.getString(addressIndex) ?: continue
                    val body = cursor.getString(bodyIndex) ?: continue
                    val timestamp = cursor.getLong(dateIndex)

                    messages.add(
                        SmsMessage(
                            id = id,
                            sender = sender,
                            body = body,
                            timestamp = timestamp
                        )
                    )
                }
            }

            Log.d(TAG, "Read ${messages.size} SMS messages for $yearMonth")

        } catch (e: SecurityException) {
            Log.e(TAG, "SMS permission not granted", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading SMS", e)
        }

        return messages
    }
}
