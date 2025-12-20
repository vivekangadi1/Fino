package com.fino.app.data.repository

import com.fino.app.domain.model.*
import com.fino.app.service.pattern.PatternDetectionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for aggregating upcoming bills from all sources:
 * - Recurring rules (user-confirmed)
 * - Credit card due dates
 * - Pattern suggestions (AI-detected)
 */
@Singleton
class UpcomingBillsRepository @Inject constructor(
    private val recurringRuleRepository: RecurringRuleRepository,
    private val creditCardRepository: CreditCardRepository,
    private val patternDetectionService: PatternDetectionService,
    private val transactionRepository: TransactionRepository
) {
    /**
     * Get a flow of upcoming bills from recurring rules and credit cards.
     * Pattern suggestions are not included in the flow (they require explicit refresh).
     */
    fun getUpcomingBillsFlow(): Flow<List<UpcomingBill>> {
        return combine(
            recurringRuleRepository.getActiveRulesFlow(),
            creditCardRepository.getActiveCardsFlow()
        ) { rules, cards ->
            val bills = mutableListOf<UpcomingBill>()

            // Add bills from recurring rules
            rules.filter { it.nextExpected != null }.forEach { rule ->
                bills.add(mapRecurringRuleToBill(rule))
            }

            // Add bills from credit cards (only if they have a due amount)
            cards.filter { it.previousDue > 0 && it.previousDueDate != null }.forEach { card ->
                bills.add(mapCreditCardToBill(card))
            }

            bills.sortedBy { it.dueDate }
        }
    }

    /**
     * Get upcoming bills within a date range, including pattern suggestions.
     */
    suspend fun getUpcomingBills(startDate: LocalDate, endDate: LocalDate): List<UpcomingBill> {
        val bills = mutableListOf<UpcomingBill>()
        val merchantsWithRules = mutableSetOf<String>()

        // Get recurring rules in range
        val rules = recurringRuleRepository.getUpcomingRules(startDate, endDate)
        rules.forEach { rule ->
            merchantsWithRules.add(rule.merchantPattern.uppercase())
            bills.add(mapRecurringRuleToBill(rule))
        }

        // Get credit card bills
        val daysDiff = ChronoUnit.DAYS.between(startDate, endDate).toInt()
        val ccBills = creditCardRepository.getUpcomingBills(daysDiff)
        ccBills.filter { it.dueDate in startDate..endDate }.forEach { ccBill ->
            bills.add(mapCreditCardBillToBill(ccBill))
        }

        // Get pattern suggestions (filtered by date range and excluding merchants with rules)
        val suggestions = patternDetectionService.detectPatterns()
        suggestions
            .filter { it.nextExpected in startDate..endDate }
            .filter { it.merchantPattern.uppercase() !in merchantsWithRules }
            .forEach { suggestion ->
                bills.add(mapPatternSuggestionToBill(suggestion))
            }

        return bills.sortedBy { it.dueDate }
    }

    /**
     * Get a summary of upcoming bills for home screen display.
     */
    suspend fun getBillSummary(): BillSummary {
        val today = LocalDate.now()
        val thisMonth = YearMonth.now()
        val nextMonth = thisMonth.plusMonths(1)

        // Get all bills for the next 60 days to cover both months
        val endDate = nextMonth.atEndOfMonth()
        val allBills = getUpcomingBills(today.minusDays(30), endDate)

        var thisMonthTotal = 0.0
        var thisMonthCount = 0
        var nextMonthTotal = 0.0
        var nextMonthCount = 0
        var overdueCount = 0
        var dueTodayCount = 0

        allBills.forEach { bill ->
            val billMonth = YearMonth.from(bill.dueDate)

            when (bill.status) {
                BillStatus.OVERDUE -> {
                    overdueCount++
                    thisMonthTotal += bill.amount
                    thisMonthCount++
                }
                BillStatus.DUE_TODAY -> {
                    dueTodayCount++
                    thisMonthTotal += bill.amount
                    thisMonthCount++
                }
                else -> {
                    when (billMonth) {
                        thisMonth -> {
                            thisMonthTotal += bill.amount
                            thisMonthCount++
                        }
                        nextMonth -> {
                            nextMonthTotal += bill.amount
                            nextMonthCount++
                        }
                    }
                }
            }
        }

        return BillSummary(
            thisMonth = MonthSummary(thisMonthTotal, thisMonthCount, thisMonth),
            nextMonth = MonthSummary(nextMonthTotal, nextMonthCount, nextMonth),
            overdueCount = overdueCount,
            dueTodayCount = dueTodayCount
        )
    }

    /**
     * Get bills grouped by time period (TODAY, TOMORROW, THIS_WEEK, etc.)
     */
    suspend fun getGroupedBills(): List<BillGroup> {
        val today = LocalDate.now()
        val endDate = today.plusMonths(2)
        val bills = getUpcomingBills(today.minusDays(7), endDate)

        val groupedBills = bills.groupBy { bill ->
            BillGroupType.fromBillStatus(bill.status).let { suggestedType ->
                // For UPCOMING bills, determine more specific grouping
                if (suggestedType == BillGroupType.LATER_THIS_MONTH || bill.status == BillStatus.UPCOMING) {
                    val billMonth = YearMonth.from(bill.dueDate)
                    val thisMonth = YearMonth.now()
                    if (billMonth == thisMonth.plusMonths(1)) {
                        BillGroupType.NEXT_MONTH
                    } else if (billMonth == thisMonth) {
                        BillGroupType.LATER_THIS_MONTH
                    } else {
                        suggestedType
                    }
                } else {
                    suggestedType
                }
            }
        }

        return BillGroupType.values()
            .filter { groupedBills.containsKey(it) }
            .map { type ->
                BillGroup.fromType(type, groupedBills[type] ?: emptyList())
            }
            .sortedBy { it.type.sortOrder }
    }

    /**
     * Get bills for calendar view for a specific month.
     */
    suspend fun getBillsForCalendar(yearMonth: YearMonth): Map<LocalDate, List<UpcomingBill>> {
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()
        val bills = getUpcomingBills(startDate, endDate)

        return bills
            .filter { YearMonth.from(it.dueDate) == yearMonth }
            .groupBy { it.dueDate }
    }

    /**
     * Mark a bill as paid.
     * Only affects recurring rules - updates occurrence tracking.
     */
    suspend fun markBillAsPaid(bill: UpcomingBill, transactionId: Long?) {
        if (bill.source != BillSource.RECURRING_RULE) return

        val today = LocalDate.now()
        val nextDate = when (bill.frequency) {
            RecurringFrequency.WEEKLY -> today.plusWeeks(1)
            RecurringFrequency.MONTHLY -> today.plusMonths(1)
            RecurringFrequency.YEARLY -> today.plusYears(1)
            null -> today.plusMonths(1)
        }

        recurringRuleRepository.recordOccurrence(bill.sourceId, today, nextDate)
    }

    /**
     * Refresh pattern suggestions from transaction history.
     */
    suspend fun refreshPatternSuggestions(): List<PatternSuggestion> {
        return patternDetectionService.detectPatterns()
    }

    // ==================== Mapping Functions ====================

    /**
     * Convert a RecurringRule to an UpcomingBill
     */
    fun mapRecurringRuleToBill(rule: RecurringRule): UpcomingBill {
        val dueDate = rule.nextExpected ?: LocalDate.now()
        return UpcomingBill(
            id = UpcomingBill.generateBillId(BillSource.RECURRING_RULE, rule.id),
            source = BillSource.RECURRING_RULE,
            merchantName = rule.merchantPattern,
            displayName = rule.merchantPattern,
            amount = rule.expectedAmount,
            amountVariance = rule.amountVariance,
            dueDate = dueDate,
            frequency = rule.frequency,
            categoryId = rule.categoryId,
            status = BillStatus.calculateStatus(dueDate, false),
            isPaid = false,
            isUserConfirmed = rule.isUserConfirmed,
            confidence = 1.0f,
            creditCardLastFour = null,
            sourceId = rule.id
        )
    }

    /**
     * Convert a CreditCard to an UpcomingBill
     */
    fun mapCreditCardToBill(card: CreditCard): UpcomingBill {
        val dueDate = card.previousDueDate ?: LocalDate.now()
        return UpcomingBill(
            id = UpcomingBill.generateBillId(BillSource.CREDIT_CARD, card.id),
            source = BillSource.CREDIT_CARD,
            merchantName = "${card.bankName} Credit Card",
            displayName = "${card.bankName} Credit Card",
            amount = card.previousDue,
            amountVariance = null,
            dueDate = dueDate,
            frequency = null,
            categoryId = null,
            status = BillStatus.calculateStatus(dueDate, false),
            isPaid = false,
            isUserConfirmed = true,
            confidence = 1.0f,
            creditCardLastFour = card.lastFourDigits,
            sourceId = card.id
        )
    }

    /**
     * Convert a CreditCardBill to an UpcomingBill
     */
    private fun mapCreditCardBillToBill(ccBill: CreditCardBill): UpcomingBill {
        return UpcomingBill(
            id = "CREDIT_CARD_${ccBill.cardLastFour}",
            source = BillSource.CREDIT_CARD,
            merchantName = "${ccBill.bankName ?: "Credit"} Card",
            displayName = "${ccBill.bankName ?: "Credit"} Card ****${ccBill.cardLastFour}",
            amount = ccBill.totalDue,
            amountVariance = null,
            dueDate = ccBill.dueDate,
            frequency = null,
            categoryId = null,
            status = BillStatus.calculateStatus(ccBill.dueDate, false),
            isPaid = false,
            isUserConfirmed = true,
            confidence = 1.0f,
            creditCardLastFour = ccBill.cardLastFour,
            sourceId = 0L // CreditCardBill doesn't have an ID
        )
    }

    /**
     * Convert a PatternSuggestion to an UpcomingBill
     */
    fun mapPatternSuggestionToBill(suggestion: PatternSuggestion): UpcomingBill {
        return UpcomingBill(
            id = "PATTERN_${suggestion.merchantPattern.hashCode()}",
            source = BillSource.PATTERN_SUGGESTION,
            merchantName = suggestion.merchantPattern,
            displayName = suggestion.displayName,
            amount = suggestion.averageAmount,
            amountVariance = suggestion.amountVariance,
            dueDate = suggestion.nextExpected,
            frequency = suggestion.detectedFrequency,
            categoryId = suggestion.categoryId,
            status = BillStatus.calculateStatus(suggestion.nextExpected, false),
            isPaid = false,
            isUserConfirmed = false,
            confidence = suggestion.confidence,
            creditCardLastFour = null,
            sourceId = 0L // Suggestions don't have persistent IDs
        )
    }
}
