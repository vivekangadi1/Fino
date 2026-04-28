package com.fino.app.service.notices

import com.fino.app.domain.model.CreditCard
import com.fino.app.domain.model.Notice
import com.fino.app.domain.model.NoticeType
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pure computation of the Noticed-this-month cards. Takes pre-loaded inputs and emits
 * a ranked List<Notice>. The result is persisted by NoticesGeneratorWorker and
 * deserialized by AnalyticsViewModel for UI rendering.
 *
 * Serialization formats kept intentionally simple (no JSON lib):
 *   routeJson → "TYPE[|arg1[|arg2]]"
 *   chartDataJson → "f1,f2,f3,..."
 */
@Singleton
class NoticesComputer @Inject constructor() {

    enum class Period { WEEK, MONTH, THREE_MONTHS, YEAR, CUSTOM }

    fun compute(
        current: List<Transaction>,
        previous: List<Transaction>,
        categoryNames: Map<Long, String>,
        period: Period,
        selectedDate: LocalDate,
        creditCards: List<CreditCard>,
        allTransactions: List<Transaction>,
        cashbackTotal: Double,
        periodKey: String = YearMonth.from(selectedDate)
            .format(DateTimeFormatter.ofPattern("yyyy-MM")),
        customBounds: Pair<LocalDate, LocalDate>? = null
    ): List<Notice> {
        val staged = mutableListOf<StagedNotice>()
        val now = System.currentTimeMillis()

        val curTotal = current.sumOf { it.amount }
        val prevTotal = previous.sumOf { it.amount }

        val currentByCat = current.groupBy { it.categoryId }.mapValues { entry -> entry.value.sumOf { it.amount } }
        val previousByCat = previous.groupBy { it.categoryId }.mapValues { entry -> entry.value.sumOf { it.amount } }

        // 1. Biggest category change
        val biggestChange = currentByCat.mapNotNull { (catId, amt) ->
            val prev = previousByCat[catId] ?: return@mapNotNull null
            if (prev < 500.0 || amt < 500.0) return@mapNotNull null
            val change = ((amt - prev) / prev) * 100
            if (kotlin.math.abs(change) < 15) return@mapNotNull null
            Triple(catId, change, amt)
        }.maxByOrNull { kotlin.math.abs(it.second) }

        if (biggestChange != null) {
            val (catId, change, amt) = biggestChange
            val catName = catId?.let { categoryNames[it] } ?: "Uncategorized"
            val direction = if (change > 0) "up" else "down"
            val pctAbs = kotlin.math.abs(change).toInt()
            val catTxns = current.filter { it.categoryId == catId }
            val chart = bucketDailyValues(catTxns, period, selectedDate, buckets = 12, customBounds = customBounds)
            staged += StagedNotice(
                type = NoticeType.CATEGORY_CHANGE,
                title = "$catName is $direction $pctAbs%",
                body = "₹${formatIndianAmount(amt)} across ${catTxns.size} ${if (catTxns.size == 1) "transaction" else "transactions"}.",
                isWarn = false,
                route = catId?.let { "SUBCATEGORY|$it|$catName" },
                chartData = chart
            )
        }

        // 2. Merchant-level rise
        val merchantCurrent = current.groupBy { normalizeMerchantKey(it.merchantName) }
            .mapValues { it.value.sumOf { t -> t.amount } }
        val merchantPrevious = previous.groupBy { normalizeMerchantKey(it.merchantName) }
            .mapValues { it.value.sumOf { t -> t.amount } }
        val biggestMerchantRise = merchantCurrent.mapNotNull { (key, amt) ->
            if (key.isBlank()) return@mapNotNull null
            val prev = merchantPrevious[key] ?: return@mapNotNull null
            if (prev < 500.0 || amt < 500.0) return@mapNotNull null
            val change = ((amt - prev) / prev) * 100
            if (change < 20) return@mapNotNull null
            Triple(key, change, amt)
        }.maxByOrNull { it.second }

        if (biggestMerchantRise != null) {
            val (key, change, amt) = biggestMerchantRise
            val pct = change.toInt()
            val displayName = current.firstOrNull { normalizeMerchantKey(it.merchantName) == key }
                ?.merchantName ?: key.replaceFirstChar { it.uppercase() }
            staged += StagedNotice(
                type = NoticeType.MERCHANT_RISE,
                title = "$displayName up $pct%",
                body = "₹${formatIndianAmount(amt)} this period vs ₹${formatIndianAmount(merchantPrevious[key] ?: 0.0)} last.",
                isWarn = false,
                route = "MERCHANT|$key",
                chartData = null
            )
        }

        // 3. Largest single transaction
        val topTxn = current.maxByOrNull { it.amount }
        if (topTxn != null && topTxn.amount >= 1000.0) {
            val catName = topTxn.categoryId?.let { categoryNames[it] } ?: "Uncategorized"
            val dateStr = topTxn.transactionDate.toLocalDate().format(DateTimeFormatter.ofPattern("MMM d"))
            staged += StagedNotice(
                type = NoticeType.LARGEST_TXN,
                title = "${topTxn.merchantName.take(32)} top spend",
                body = "₹${formatIndianAmount(topTxn.amount)} on $dateStr · $catName.",
                isWarn = false,
                route = "MERCHANT|${normalizeMerchantKey(topTxn.merchantName)}",
                chartData = null
            )
        }

        // 3b. Spike day — single day that vastly exceeds the period's median daily spend
        val dailyTotals = current.groupBy { it.transactionDate.toLocalDate() }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
        if (dailyTotals.size >= 3) {
            val sortedVals = dailyTotals.values.sorted()
            val median = sortedVals[sortedVals.size / 2]
            val spikeEntry = dailyTotals.maxByOrNull { it.value }
            if (spikeEntry != null && median > 0 && spikeEntry.value > median * 2.5 && spikeEntry.value >= 1000.0) {
                val spikeDate = spikeEntry.key
                val spikeAmt = spikeEntry.value
                val dateStr = spikeDate.format(DateTimeFormatter.ofPattern("MMM d"))
                val txnCount = current.count { it.transactionDate.toLocalDate() == spikeDate }
                val txnLabel = if (txnCount == 1) "transaction" else "transactions"
                staged += StagedNotice(
                    type = NoticeType.SPIKE_DAY,
                    title = "₹${formatIndianAmount(spikeAmt)} on $dateStr",
                    body = "Biggest day this period · $txnCount $txnLabel.",
                    isWarn = false,
                    route = "DAY|${spikeDate.toEpochDay()}",
                    chartData = null
                )
            }
        }

        // 4. Credit card bill due within 10 days
        val today = LocalDate.now()
        val unpaidBills = creditCards.mapNotNull { card ->
            val due = card.effectiveDueDate ?: return@mapNotNull null
            if (card.isPaid) return@mapNotNull null
            val days = ChronoUnit.DAYS.between(today, due).toInt()
            if (days < 0 || days > 10) return@mapNotNull null
            Triple(card, due, days)
        }.minByOrNull { it.third }

        if (unpaidBills != null) {
            val (card, due, days) = unpaidBills
            val amt = card.effectiveDueAmount
            val whenStr = when {
                days == 0 -> "today"
                days == 1 -> "tomorrow"
                else -> "in $days days"
            }
            val dueStr = due.format(DateTimeFormatter.ofPattern("MMM d"))
            staged += StagedNotice(
                type = NoticeType.BILL_DUE,
                title = "${card.bankName} bill due $whenStr",
                body = "₹${formatIndianAmount(amt)} on $dueStr · ****${card.lastFourDigits}.",
                isWarn = days < 3,
                route = "BILL|${card.id}",
                chartData = null
            )
        }

        // 5. Subscriptions steady
        val recurring = current.filter { it.isRecurring }
        if (recurring.isNotEmpty()) {
            val total = recurring.sumOf { it.amount }
            staged += StagedNotice(
                type = NoticeType.SUBS,
                title = "Subscriptions steady",
                body = "₹${formatIndianAmount(total)} across ${recurring.size} recurring ${if (recurring.size == 1) "charge" else "charges"}.",
                isWarn = false,
                route = "SUBSCRIPTIONS",
                chartData = null
            )
        }

        // 6. New merchants
        val periodRange = periodDateRange(period, selectedDate, customBounds)
        val newMerchantSet = buildNewMerchantSet(allTransactions, current, periodRange)
        if (newMerchantSet.isNotEmpty()) {
            val newTotal = current
                .filter { normalizeMerchantKey(it.merchantName) in newMerchantSet }
                .sumOf { it.amount }
            staged += StagedNotice(
                type = NoticeType.NEW_MERCHANTS,
                title = "${newMerchantSet.size} new ${if (newMerchantSet.size == 1) "merchant" else "merchants"}",
                body = "₹${formatIndianAmount(newTotal)} at places you haven't used before.",
                isWarn = false,
                route = "NEW_MERCHANTS",
                chartData = null
            )
        }

        // 7. Weekend vs weekday ratio
        val weekendRatio = weekendSpendRatio(current)
        if (weekendRatio != null && weekendRatio >= 1.5) {
            staged += StagedNotice(
                type = NoticeType.WEEKEND,
                title = "Weekends cost ${"%.1f".format(weekendRatio)}× weekdays",
                body = "Sat–Sun daily avg runs higher than Mon–Fri.",
                isWarn = false,
                route = "WEEKEND",
                chartData = null
            )
        }

        // 8. Cashback
        if (cashbackTotal > 0) {
            staged += StagedNotice(
                type = NoticeType.CASHBACK,
                title = "Cashback earned",
                body = "₹${formatIndianAmount(cashbackTotal)} credited so far this period.",
                isWarn = false,
                route = null,
                chartData = null
            )
        }

        // 9. Pace warning
        if (prevTotal > 0 && staged.none { it.isWarn }) {
            val pace = ((curTotal - prevTotal) / prevTotal) * 100
            if (pace > 25) {
                staged += StagedNotice(
                    type = NoticeType.PACE,
                    title = "Watch your pace",
                    body = "You're tracking ${pace.toInt()}% higher than the previous period.",
                    isWarn = true,
                    route = "COMPARE",
                    chartData = null
                )
            }
        }

        if (staged.isEmpty() && current.isNotEmpty()) {
            staged += StagedNotice(
                type = NoticeType.PACE,
                title = "Too early to call it",
                body = "Log a few more transactions and insights will appear here.",
                isWarn = false,
                route = null,
                chartData = null
            )
        }

        return staged.take(8).mapIndexed { idx, s ->
            Notice(
                period = periodKey,
                type = s.type,
                title = s.title,
                body = s.body,
                isWarn = s.isWarn,
                routeJson = s.route,
                chartDataJson = s.chartData?.joinToString(","),
                rankOrder = idx,
                computedAt = now
            )
        }
    }

    private data class StagedNotice(
        val type: NoticeType,
        val title: String,
        val body: String,
        val isWarn: Boolean,
        val route: String?,
        val chartData: List<Float>?
    )

    private fun normalizeMerchantKey(raw: String): String {
        return raw.trim().lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun periodDateRange(
        period: Period,
        ref: LocalDate,
        customBounds: Pair<LocalDate, LocalDate>? = null
    ): Pair<LocalDate, LocalDate> {
        return when (period) {
            Period.WEEK -> {
                val wf = WeekFields.of(Locale.getDefault())
                val start = ref.with(wf.dayOfWeek(), 1)
                start to start.plusDays(6)
            }
            Period.MONTH -> {
                val ym = YearMonth.from(ref)
                ym.atDay(1) to ym.atEndOfMonth()
            }
            Period.THREE_MONTHS -> {
                val end = YearMonth.from(ref)
                end.minusMonths(2).atDay(1) to end.atEndOfMonth()
            }
            Period.YEAR -> {
                LocalDate.of(ref.year, 1, 1) to LocalDate.of(ref.year, 12, 31)
            }
            Period.CUSTOM -> {
                customBounds ?: (ref to ref)
            }
        }
    }

    private fun buildNewMerchantSet(
        all: List<Transaction>,
        current: List<Transaction>,
        periodRange: Pair<LocalDate, LocalDate>
    ): Set<String> {
        val (start, _) = periodRange
        val firstSeen = mutableMapOf<String, LocalDate>()
        all.filter { it.type == TransactionType.DEBIT }.forEach { txn ->
            val key = normalizeMerchantKey(txn.merchantName)
            if (key.isBlank()) return@forEach
            val date = txn.transactionDate.toLocalDate()
            val existing = firstSeen[key]
            if (existing == null || date.isBefore(existing)) {
                firstSeen[key] = date
            }
        }
        val currentKeys = current.map { normalizeMerchantKey(it.merchantName) }.toSet()
        return currentKeys.filter { key ->
            val seen = firstSeen[key]
            seen != null && !seen.isBefore(start)
        }.toSet()
    }

    private fun weekendSpendRatio(current: List<Transaction>): Double? {
        if (current.isEmpty()) return null
        val byDay = current.groupBy { it.transactionDate.toLocalDate() }
        val weekendDays = byDay.filterKeys {
            it.dayOfWeek == DayOfWeek.SATURDAY || it.dayOfWeek == DayOfWeek.SUNDAY
        }
        val weekdayDays = byDay.filterKeys {
            it.dayOfWeek != DayOfWeek.SATURDAY && it.dayOfWeek != DayOfWeek.SUNDAY
        }
        if (weekendDays.isEmpty() || weekdayDays.isEmpty()) return null
        val weekendAvg = weekendDays.values.map { it.sumOf { t -> t.amount } }.average()
        val weekdayAvg = weekdayDays.values.map { it.sumOf { t -> t.amount } }.average()
        if (weekdayAvg <= 0) return null
        return weekendAvg / weekdayAvg
    }

    private fun bucketDailyValues(
        transactions: List<Transaction>,
        period: Period,
        referenceDate: LocalDate,
        buckets: Int,
        customBounds: Pair<LocalDate, LocalDate>? = null
    ): List<Float> {
        if (transactions.isEmpty()) return List(buckets) { 0f }
        val (startDate, endDate) = periodDateRange(period, referenceDate, customBounds)
        val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
        val daysPerBucket = (totalDays.toFloat() / buckets).coerceAtLeast(1f)
        val values = MutableList(buckets) { 0f }
        transactions.forEach { txn ->
            val txDate = txn.transactionDate.toLocalDate()
            if (txDate.isBefore(startDate) || txDate.isAfter(endDate)) return@forEach
            val daysFromStart = ChronoUnit.DAYS.between(startDate, txDate).toInt()
            val idx = (daysFromStart / daysPerBucket).toInt().coerceIn(0, buckets - 1)
            values[idx] = values[idx] + txn.amount.toFloat()
        }
        return values
    }

    private fun formatIndianAmount(value: Double): String {
        val formatter = java.text.NumberFormat.getInstance(Locale("en", "IN"))
        return formatter.format(value.toLong())
    }
}
