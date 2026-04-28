package com.fino.app.domain.usecase

import com.fino.app.domain.model.CategorySegment
import com.fino.app.domain.model.EventSettleRow
import com.fino.app.domain.model.EventSummary
import com.fino.app.domain.model.FeaturedEventData
import com.fino.app.domain.model.Transaction
import kotlin.math.abs

object EventInsightsCalculator {
    private const val SELF_LABEL = "Self"
    private const val TOP_N_CATEGORIES = 3
    private const val EPSILON = 0.5

    fun build(
        summary: EventSummary,
        eventTxns: List<Transaction>,
        categoryNames: Map<Long, String>
    ): FeaturedEventData {
        val paidByBuckets = eventTxns.groupBy { normalizePayer(it) }
        val participantCount = paidByBuckets.keys.size.coerceAtLeast(1)

        val yourShare = eventTxns
            .filter { normalizePayer(it) == SELF_LABEL }
            .sumOf { it.amount }

        return FeaturedEventData(
            summary = summary,
            participantCount = participantCount,
            yourShare = yourShare,
            categorySegments = computeCategorySegments(eventTxns, categoryNames),
            settleRows = computeSettleRows(paidByBuckets)
        )
    }

    private fun normalizePayer(t: Transaction): String {
        val raw = t.paidBy?.trim()
        return if (raw.isNullOrBlank() || raw.equals("self", ignoreCase = true)) SELF_LABEL else raw
    }

    private fun computeCategorySegments(
        txns: List<Transaction>,
        categoryNames: Map<Long, String>
    ): List<CategorySegment> {
        if (txns.isEmpty()) return emptyList()
        val total = txns.sumOf { it.amount }
        if (total <= 0) return emptyList()

        val byCategory = txns
            .groupBy { it.categoryId }
            .map { (catId, list) ->
                val name = catId?.let { categoryNames[it] } ?: "Uncategorized"
                name to list.sumOf { it.amount }
            }
            .sortedByDescending { it.second }

        val top = byCategory.take(TOP_N_CATEGORIES)
        val rest = byCategory.drop(TOP_N_CATEGORIES).sumOf { it.second }

        val segments = top.mapIndexed { idx, (name, amt) ->
            CategorySegment(
                categoryName = name,
                amount = amt,
                fraction = (amt / total).toFloat(),
                paletteIndex = idx
            )
        }

        return if (rest > EPSILON) {
            segments + CategorySegment(
                categoryName = "Other",
                amount = rest,
                fraction = (rest / total).toFloat(),
                paletteIndex = TOP_N_CATEGORIES
            )
        } else {
            segments
        }
    }

    private fun computeSettleRows(
        paidByBuckets: Map<String, List<Transaction>>
    ): List<EventSettleRow> {
        if (paidByBuckets.size < 2) return emptyList()

        val paid = paidByBuckets.mapValues { (_, list) -> list.sumOf { it.amount } }
        val total = paid.values.sum()
        if (total <= 0) return emptyList()

        val n = paid.size
        val fairShare = total / n
        val balances = paid.mapValues { (_, amt) -> amt - fairShare }

        val selfBal = balances[SELF_LABEL] ?: return emptyList()
        val creditors = balances.filterValues { it > EPSILON }
        val debtors = balances.filterValues { it < -EPSILON }
        val totalCredit = creditors.values.sum()
        if (totalCredit <= 0) return emptyList()

        return when {
            selfBal > EPSILON -> {
                val selfShareOfCredit = selfBal / totalCredit
                debtors
                    .map { (name, bal) ->
                        EventSettleRow(
                            who = name,
                            owesYou = true,
                            amount = abs(bal) * selfShareOfCredit
                        )
                    }
                    .filter { it.amount >= 1.0 }
                    .sortedByDescending { it.amount }
            }
            selfBal < -EPSILON -> {
                creditors
                    .filterKeys { it != SELF_LABEL }
                    .map { (name, credit) ->
                        EventSettleRow(
                            who = name,
                            owesYou = false,
                            amount = abs(selfBal) * (credit / totalCredit)
                        )
                    }
                    .filter { it.amount >= 1.0 }
                    .sortedByDescending { it.amount }
            }
            else -> emptyList()
        }
    }
}
