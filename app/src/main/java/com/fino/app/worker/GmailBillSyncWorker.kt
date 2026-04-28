package com.fino.app.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fino.app.BuildConfig
import com.fino.app.data.repository.BillRepository
import com.fino.app.domain.model.Bill
import com.fino.app.domain.model.BillEntitySource
import com.fino.app.domain.model.BillEntityStatus
import com.fino.app.service.gmail.GmailAuthManager
import com.fino.app.service.gmail.GmailBillFetcher
import com.fino.app.service.gmail.GmailBillParser
import com.fino.app.service.security.SecureTokenStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration
import java.time.Instant

/**
 * 12-hourly sweep that pulls new CC statement emails and upserts BillEntity
 * rows. Gated behind BuildConfig.ENABLE_GMAIL so the rest of the app can ship
 * while OAuth consent verification is pending.
 */
@HiltWorker
class GmailBillSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val gmailAuthManager: GmailAuthManager,
    private val gmailBillFetcher: GmailBillFetcher,
    private val gmailBillParser: GmailBillParser,
    private val billRepository: BillRepository,
    private val secureTokenStore: SecureTokenStore
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "GmailBillSyncWorker"
        const val WORK_NAME = "gmail-bill-sync"
        private val DEFAULT_LOOKBACK = Duration.ofDays(30)
    }

    override suspend fun doWork(): Result {
        if (!BuildConfig.ENABLE_GMAIL) return Result.success()
        val email = gmailAuthManager.getConnectedEmail() ?: return Result.success()

        return try {
            val since = secureTokenStore.getString(SecureTokenStore.KEY_GMAIL_LAST_SYNC)
                ?.toLongOrNull()
                ?: (Instant.now().minus(DEFAULT_LOOKBACK).toEpochMilli())

            val emails = gmailBillFetcher.fetchStatementEmails(email, since)
            Log.d(TAG, "Gmail sync fetched ${emails.size} candidate emails")

            var upserts = 0
            emails.forEach { fetched ->
                val parsed = gmailBillParser.parse(
                    bank = fetched.bank,
                    from = fetched.from,
                    subject = fetched.subject,
                    htmlOrText = fetched.body
                ) ?: return@forEach

                val bill = Bill(
                    accountId = null,
                    creditCardId = null,
                    cycleStart = parsed.cycleStartMillis ?: parsed.cycleEndMillis,
                    cycleEnd = parsed.cycleEndMillis,
                    dueDate = parsed.dueDateMillis,
                    totalDue = parsed.totalDue,
                    minDue = parsed.minDue,
                    status = BillEntityStatus.PENDING,
                    source = BillEntitySource.EMAIL,
                    payeeVpa = parsed.payeeVpa,
                    payeeName = parsed.payeeName ?: "${parsed.bank} Credit Card"
                )
                billRepository.upsertForCycle(bill)
                upserts++
            }

            secureTokenStore.putString(
                SecureTokenStore.KEY_GMAIL_LAST_SYNC,
                System.currentTimeMillis().toString()
            )
            Log.d(TAG, "Gmail sync upserted $upserts bills")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Gmail sync failed", e)
            Result.retry()
        }
    }
}
