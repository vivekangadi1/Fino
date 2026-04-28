package com.fino.app.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fino.app.data.repository.CashbackRewardRepository
import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.CreditCardRepository
import com.fino.app.data.repository.NoticesRepository
import com.fino.app.data.repository.RecurringRuleRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.TransactionType
import com.fino.app.service.notices.NoticesComputer
import com.fino.app.util.DateUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * Daily background pass that recomputes Noticed cards for the current month and
 * refreshes RecurringRule.lastOccurrence markers so dormant-subscription
 * detection stays accurate for historic transactions that predate the
 * per-insert hook.
 */
@HiltWorker
class NoticesGeneratorWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val creditCardRepository: CreditCardRepository,
    private val cashbackRewardRepository: CashbackRewardRepository,
    private val noticesRepository: NoticesRepository,
    private val recurringRuleRepository: RecurringRuleRepository,
    private val noticesComputer: NoticesComputer
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "NoticesGeneratorWorker"
        const val WORK_NAME = "notices-generation"
    }

    override suspend fun doWork(): Result {
        return try {
            val allTransactions = transactionRepository.getAllTransactionsFlow().first()
            val categories = categoryRepository.getAllActive().first()
            val creditCards = creditCardRepository.getActiveCardsFlow().first()

            val today = LocalDate.now()
            val currentMonth = YearMonth.from(today)
            val previousMonth = currentMonth.minusMonths(1)
            val periodKey = currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))

            val current = allTransactions.filter {
                it.type == TransactionType.DEBIT && YearMonth.from(it.transactionDate) == currentMonth
            }
            val previous = allTransactions.filter {
                it.type == TransactionType.DEBIT && YearMonth.from(it.transactionDate) == previousMonth
            }
            val categoryNames = categories.associate { it.id to it.name }
            val cashbackTotal = cashbackRewardRepository.getTotalForPeriod(periodKey)

            val notices = noticesComputer.compute(
                current = current,
                previous = previous,
                categoryNames = categoryNames,
                period = NoticesComputer.Period.MONTH,
                selectedDate = today,
                creditCards = creditCards,
                allTransactions = allTransactions,
                cashbackTotal = cashbackTotal,
                periodKey = periodKey
            )
            noticesRepository.replaceForPeriod(periodKey, notices)
            Log.d(TAG, "Wrote ${notices.size} notices for $periodKey")

            refreshRecurringLastOccurrence(allTransactions)

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Notices generation failed", e)
            Result.retry()
        }
    }

    private suspend fun refreshRecurringLastOccurrence(
        allTransactions: List<com.fino.app.domain.model.Transaction>
    ) {
        val rules = recurringRuleRepository.getActiveRules()
        var updated = 0
        rules.forEach { rule ->
            val match = allTransactions
                .filter {
                    val key = it.merchantNormalized ?: it.merchantName
                    key.contains(rule.merchantPattern, ignoreCase = true)
                }
                .maxByOrNull { it.transactionDate }
                ?: return@forEach
            val newLastMillis = DateUtils.toEpochMillis(match.transactionDate)
            val storedMillis = rule.lastOccurrence
                ?.let { DateUtils.toEpochMillis(it.atStartOfDay()) }
            if (storedMillis == null || storedMillis < newLastMillis) {
                recurringRuleRepository.updateLastOccurrenceIfNewer(rule.id, newLastMillis)
                updated++
            }
        }
        if (updated > 0) Log.d(TAG, "Refreshed lastOccurrence for $updated rules")
    }
}
