package com.fino.app.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fino.app.data.repository.BillRepository
import com.fino.app.domain.model.BillEntityStatus
import com.fino.app.service.notification.NotificationService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Daily sweep that fires one notification per bill due within the next two days
 * (plus anything already overdue). Dedup is handled by the per-bill notification
 * id in NotificationService, so reruns the same day just refresh the banner.
 */
@HiltWorker
class BillDueCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val billRepository: BillRepository,
    private val notificationService: NotificationService
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "BillDueCheckWorker"
        const val WORK_NAME = "bill-due-check"
        private const val NOTIFY_WITHIN_DAYS = 2
    }

    override suspend fun doWork(): Result {
        return try {
            val today = LocalDate.now()
            val bills = billRepository.getUpcomingBills(withinDays = NOTIFY_WITHIN_DAYS)
            val unpaid = bills.filter { bill ->
                bill.status == BillEntityStatus.PENDING ||
                    bill.status == BillEntityStatus.PARTIAL ||
                    bill.status == BillEntityStatus.OVERDUE
            }

            unpaid.forEach { bill ->
                val dueDate = Instant.ofEpochMilli(bill.dueDate)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val daysUntil = ChronoUnit.DAYS.between(today, dueDate)
                if (daysUntil <= NOTIFY_WITHIN_DAYS) {
                    notificationService.showBillDueNotification(bill)
                }
            }

            Log.d(TAG, "Checked ${bills.size} upcoming bills, notified ${unpaid.size} unpaid")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Bill due check failed", e)
            Result.retry()
        }
    }
}
