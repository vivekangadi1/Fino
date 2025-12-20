package com.fino.app.data.repository

import com.fino.app.domain.model.*
import com.fino.app.service.pattern.PatternDetectionService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class UpcomingBillsRepositoryTest {

    private lateinit var recurringRuleRepository: RecurringRuleRepository
    private lateinit var creditCardRepository: CreditCardRepository
    private lateinit var patternDetectionService: PatternDetectionService
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var repository: UpcomingBillsRepository

    @Before
    fun setup() {
        recurringRuleRepository = mock()
        creditCardRepository = mock()
        patternDetectionService = mock()
        transactionRepository = mock()
        repository = UpcomingBillsRepository(
            recurringRuleRepository,
            creditCardRepository,
            patternDetectionService,
            transactionRepository
        )
    }

    // ==================== Bill Flow Tests (5 tests) ====================

    // Test 1: getUpcomingBillsFlow returns combined bills from all sources
    @Test
    fun `getUpcomingBillsFlow returns bills from all sources`() = runTest {
        val rule = createRecurringRule(id = 1L, merchantPattern = "Netflix")
        whenever(recurringRuleRepository.getActiveRulesFlow()).thenReturn(flowOf(listOf(rule)))
        whenever(creditCardRepository.getActiveCardsFlow()).thenReturn(flowOf(emptyList()))

        val bills = repository.getUpcomingBillsFlow().first()

        assertTrue(bills.isNotEmpty())
        assertEquals(BillSource.RECURRING_RULE, bills[0].source)
    }

    // Test 2: getUpcomingBillsFlow includes credit card bills
    @Test
    fun `getUpcomingBillsFlow includes credit card bills`() = runTest {
        val card = createCreditCard(id = 1L, lastFour = "1234", previousDue = 5000.0)
        whenever(recurringRuleRepository.getActiveRulesFlow()).thenReturn(flowOf(emptyList()))
        whenever(creditCardRepository.getActiveCardsFlow()).thenReturn(flowOf(listOf(card)))

        val bills = repository.getUpcomingBillsFlow().first()

        assertTrue(bills.any { it.source == BillSource.CREDIT_CARD })
    }

    // Test 3: getUpcomingBillsFlow filters cards with no due amount
    @Test
    fun `getUpcomingBillsFlow filters credit cards with zero due`() = runTest {
        val card = createCreditCard(id = 1L, previousDue = 0.0)
        whenever(recurringRuleRepository.getActiveRulesFlow()).thenReturn(flowOf(emptyList()))
        whenever(creditCardRepository.getActiveCardsFlow()).thenReturn(flowOf(listOf(card)))

        val bills = repository.getUpcomingBillsFlow().first()

        assertTrue(bills.isEmpty())
    }

    // Test 4: getUpcomingBillsFlow returns empty for no bills
    @Test
    fun `getUpcomingBillsFlow returns empty list when no bills`() = runTest {
        whenever(recurringRuleRepository.getActiveRulesFlow()).thenReturn(flowOf(emptyList()))
        whenever(creditCardRepository.getActiveCardsFlow()).thenReturn(flowOf(emptyList()))

        val bills = repository.getUpcomingBillsFlow().first()

        assertTrue(bills.isEmpty())
    }

    // Test 5: Bills are sorted by due date
    @Test
    fun `getUpcomingBillsFlow returns bills sorted by due date`() = runTest {
        val rule1 = createRecurringRule(
            id = 1L,
            merchantPattern = "Later",
            nextExpected = LocalDate.now().plusDays(10)
        )
        val rule2 = createRecurringRule(
            id = 2L,
            merchantPattern = "Sooner",
            nextExpected = LocalDate.now().plusDays(3)
        )
        whenever(recurringRuleRepository.getActiveRulesFlow()).thenReturn(flowOf(listOf(rule1, rule2)))
        whenever(creditCardRepository.getActiveCardsFlow()).thenReturn(flowOf(emptyList()))

        val bills = repository.getUpcomingBillsFlow().first()

        assertEquals(2, bills.size)
        assertTrue(bills[0].dueDate <= bills[1].dueDate)
    }

    // ==================== Suspend Function Tests (5 tests) ====================

    // Test 6: getUpcomingBills returns bills in date range
    @Test
    fun `getUpcomingBills returns bills in date range`() = runTest {
        val startDate = LocalDate.now()
        val endDate = LocalDate.now().plusDays(30)
        val rule = createRecurringRule(nextExpected = LocalDate.now().plusDays(15))
        whenever(recurringRuleRepository.getUpcomingRules(startDate, endDate)).thenReturn(listOf(rule))
        whenever(creditCardRepository.getUpcomingBills(30)).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        val bills = repository.getUpcomingBills(startDate, endDate)

        assertEquals(1, bills.size)
    }

    // Test 7: getUpcomingBills includes pattern suggestions
    @Test
    fun `getUpcomingBills includes pattern suggestions`() = runTest {
        val startDate = LocalDate.now()
        val endDate = LocalDate.now().plusDays(30)
        val suggestion = createPatternSuggestion(nextExpected = LocalDate.now().plusDays(10))
        whenever(recurringRuleRepository.getUpcomingRules(startDate, endDate)).thenReturn(emptyList())
        whenever(creditCardRepository.getUpcomingBills(any())).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(listOf(suggestion))

        val bills = repository.getUpcomingBills(startDate, endDate)

        assertTrue(bills.any { it.source == BillSource.PATTERN_SUGGESTION })
    }

    // Test 8: getUpcomingBills excludes suggestions outside date range
    @Test
    fun `getUpcomingBills excludes suggestions outside date range`() = runTest {
        val startDate = LocalDate.now()
        val endDate = LocalDate.now().plusDays(15)
        val suggestion = createPatternSuggestion(nextExpected = LocalDate.now().plusDays(30))
        whenever(recurringRuleRepository.getUpcomingRules(startDate, endDate)).thenReturn(emptyList())
        whenever(creditCardRepository.getUpcomingBills(any())).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(listOf(suggestion))

        val bills = repository.getUpcomingBills(startDate, endDate)

        assertTrue(bills.none { it.source == BillSource.PATTERN_SUGGESTION })
    }

    // Test 9: getUpcomingBills deduplicates same merchant from rule and suggestion
    @Test
    fun `getUpcomingBills prefers rules over suggestions for same merchant`() = runTest {
        val startDate = LocalDate.now()
        val endDate = LocalDate.now().plusDays(30)
        val rule = createRecurringRule(merchantPattern = "NETFLIX", nextExpected = LocalDate.now().plusDays(15))
        val suggestion = createPatternSuggestion(merchantPattern = "NETFLIX", nextExpected = LocalDate.now().plusDays(15))
        whenever(recurringRuleRepository.getUpcomingRules(startDate, endDate)).thenReturn(listOf(rule))
        whenever(creditCardRepository.getUpcomingBills(any())).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(listOf(suggestion))

        val bills = repository.getUpcomingBills(startDate, endDate)

        // Should only have the rule, not both
        assertEquals(1, bills.size)
        assertEquals(BillSource.RECURRING_RULE, bills[0].source)
    }

    // Test 10: getUpcomingBills includes credit card bills
    @Test
    fun `getUpcomingBills includes credit card bills in range`() = runTest {
        val startDate = LocalDate.now()
        val endDate = LocalDate.now().plusDays(30)
        val ccBill = CreditCardBill(
            cardLastFour = "1234",
            bankName = "HDFC",
            totalDue = 12500.0,
            minimumDue = 1250.0,
            dueDate = LocalDate.now().plusDays(10)
        )
        whenever(recurringRuleRepository.getUpcomingRules(startDate, endDate)).thenReturn(emptyList())
        whenever(creditCardRepository.getUpcomingBills(any())).thenReturn(listOf(ccBill))
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        val bills = repository.getUpcomingBills(startDate, endDate)

        assertEquals(1, bills.size)
        assertEquals(BillSource.CREDIT_CARD, bills[0].source)
        assertEquals("1234", bills[0].creditCardLastFour)
    }

    // ==================== Bill Summary Tests (5 tests) ====================

    // Test 11: getBillSummary calculates this month totals
    @Test
    fun `getBillSummary calculates this month totals correctly`() = runTest {
        val thisMonthRule = createRecurringRule(
            expectedAmount = 1000.0,
            nextExpected = LocalDate.now().plusDays(5)
        )
        whenever(recurringRuleRepository.getUpcomingRules(any(), any())).thenReturn(listOf(thisMonthRule))
        whenever(creditCardRepository.getUpcomingBills(any())).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        val summary = repository.getBillSummary()

        assertEquals(1000.0, summary.thisMonth.totalAmount, 0.01)
        assertEquals(1, summary.thisMonth.billCount)
    }

    // Test 12: getBillSummary calculates next month totals
    @Test
    fun `getBillSummary calculates next month totals correctly`() = runTest {
        val nextMonthDate = LocalDate.now().plusMonths(1).withDayOfMonth(15)
        val nextMonthRule = createRecurringRule(
            expectedAmount = 2000.0,
            nextExpected = nextMonthDate
        )
        whenever(recurringRuleRepository.getUpcomingRules(any(), any())).thenReturn(listOf(nextMonthRule))
        whenever(creditCardRepository.getUpcomingBills(any())).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        val summary = repository.getBillSummary()

        assertEquals(2000.0, summary.nextMonth.totalAmount, 0.01)
    }

    // Test 13: getBillSummary counts overdue bills
    @Test
    fun `getBillSummary counts overdue bills`() = runTest {
        val overdueRule = createRecurringRule(
            nextExpected = LocalDate.now().minusDays(5)
        )
        whenever(recurringRuleRepository.getUpcomingRules(any(), any())).thenReturn(listOf(overdueRule))
        whenever(creditCardRepository.getUpcomingBills(any())).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        val summary = repository.getBillSummary()

        assertEquals(1, summary.overdueCount)
    }

    // Test 14: getBillSummary counts due today
    @Test
    fun `getBillSummary counts bills due today`() = runTest {
        val todayRule = createRecurringRule(nextExpected = LocalDate.now())
        whenever(recurringRuleRepository.getUpcomingRules(any(), any())).thenReturn(listOf(todayRule))
        whenever(creditCardRepository.getUpcomingBills(any())).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        val summary = repository.getBillSummary()

        assertEquals(1, summary.dueTodayCount)
    }

    // Test 15: getBillSummary returns empty summary when no bills
    @Test
    fun `getBillSummary returns empty summary when no bills`() = runTest {
        whenever(recurringRuleRepository.getUpcomingRules(any(), any())).thenReturn(emptyList())
        whenever(creditCardRepository.getUpcomingBills(any())).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        val summary = repository.getBillSummary()

        assertEquals(0.0, summary.thisMonth.totalAmount, 0.01)
        assertEquals(0.0, summary.nextMonth.totalAmount, 0.01)
        assertEquals(0, summary.overdueCount)
        assertEquals(0, summary.dueTodayCount)
    }

    // ==================== Bill Grouping Tests (5 tests) ====================

    // Test 16: getGroupedBills groups by TODAY
    @Test
    fun `getGroupedBills groups bills due today`() = runTest {
        val todayRule = createRecurringRule(nextExpected = LocalDate.now())
        whenever(recurringRuleRepository.getUpcomingRules(any(), any())).thenReturn(listOf(todayRule))
        whenever(creditCardRepository.getUpcomingBills(any())).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        val groups = repository.getGroupedBills()

        assertTrue(groups.any { it.type == BillGroupType.TODAY })
    }

    // Test 17: getGroupedBills groups by TOMORROW
    @Test
    fun `getGroupedBills groups bills due tomorrow`() = runTest {
        val tomorrowRule = createRecurringRule(nextExpected = LocalDate.now().plusDays(1))
        whenever(recurringRuleRepository.getUpcomingRules(any(), any())).thenReturn(listOf(tomorrowRule))
        whenever(creditCardRepository.getUpcomingBills(any())).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        val groups = repository.getGroupedBills()

        assertTrue(groups.any { it.type == BillGroupType.TOMORROW })
    }

    // Test 18: getGroupedBills groups by THIS_WEEK
    @Test
    fun `getGroupedBills groups bills due this week`() = runTest {
        val thisWeekRule = createRecurringRule(nextExpected = LocalDate.now().plusDays(5))
        whenever(recurringRuleRepository.getUpcomingRules(any(), any())).thenReturn(listOf(thisWeekRule))
        whenever(creditCardRepository.getUpcomingBills(any())).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        val groups = repository.getGroupedBills()

        assertTrue(groups.any { it.type == BillGroupType.THIS_WEEK })
    }

    // Test 19: getGroupedBills groups by LATER_THIS_MONTH
    @Test
    fun `getGroupedBills groups bills later this month`() = runTest {
        // Get a date that's definitely later this month but not next month
        val laterThisMonth = LocalDate.now().plusDays(15)
        val laterRule = createRecurringRule(nextExpected = laterThisMonth)
        whenever(recurringRuleRepository.getUpcomingRules(any(), any())).thenReturn(listOf(laterRule))
        whenever(creditCardRepository.getUpcomingBills(any())).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        val groups = repository.getGroupedBills()

        // Should be in either THIS_WEEK or LATER_THIS_MONTH depending on current date
        assertTrue(groups.any {
            it.type == BillGroupType.THIS_WEEK ||
            it.type == BillGroupType.LATER_THIS_MONTH ||
            it.type == BillGroupType.NEXT_MONTH
        })
    }

    // Test 20: getGroupedBills groups by NEXT_MONTH
    @Test
    fun `getGroupedBills groups bills for next month`() = runTest {
        val nextMonthDate = LocalDate.now().plusMonths(1).withDayOfMonth(15)
        val nextMonthRule = createRecurringRule(nextExpected = nextMonthDate)
        whenever(recurringRuleRepository.getUpcomingRules(any(), any())).thenReturn(listOf(nextMonthRule))
        whenever(creditCardRepository.getUpcomingBills(any())).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        val groups = repository.getGroupedBills()

        assertTrue(groups.any { it.type == BillGroupType.NEXT_MONTH })
    }

    // ==================== Calendar View Tests (5 tests) ====================

    // Test 21: getBillsForCalendar returns bills for month
    @Test
    fun `getBillsForCalendar returns bills mapped by date`() = runTest {
        val dueDate = LocalDate.now().withDayOfMonth(15)
        val rule = createRecurringRule(nextExpected = dueDate)
        whenever(recurringRuleRepository.getUpcomingRules(any(), any())).thenReturn(listOf(rule))
        whenever(creditCardRepository.getUpcomingBills(any())).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        val calendarBills = repository.getBillsForCalendar(YearMonth.now())

        assertTrue(calendarBills.containsKey(dueDate))
    }

    // Test 22: getBillsForCalendar groups multiple bills on same date
    @Test
    fun `getBillsForCalendar groups bills on same date`() = runTest {
        val dueDate = LocalDate.now().withDayOfMonth(20)
        val rule1 = createRecurringRule(id = 1L, nextExpected = dueDate)
        val rule2 = createRecurringRule(id = 2L, nextExpected = dueDate)
        whenever(recurringRuleRepository.getUpcomingRules(any(), any())).thenReturn(listOf(rule1, rule2))
        whenever(creditCardRepository.getUpcomingBills(any())).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        val calendarBills = repository.getBillsForCalendar(YearMonth.now())

        assertEquals(2, calendarBills[dueDate]?.size)
    }

    // Test 23: getBillsForCalendar returns empty for month with no bills
    @Test
    fun `getBillsForCalendar returns empty map for month without bills`() = runTest {
        whenever(recurringRuleRepository.getUpcomingRules(any(), any())).thenReturn(emptyList())
        whenever(creditCardRepository.getUpcomingBills(any())).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        val calendarBills = repository.getBillsForCalendar(YearMonth.now().plusMonths(6))

        assertTrue(calendarBills.isEmpty())
    }

    // Test 24: getBillsForCalendar includes all bill sources
    @Test
    fun `getBillsForCalendar includes bills from all sources`() = runTest {
        val dueDate = LocalDate.now().withDayOfMonth(10)
        val rule = createRecurringRule(nextExpected = dueDate)
        val ccBill = CreditCardBill("1234", "HDFC", 5000.0, null, dueDate)
        whenever(recurringRuleRepository.getUpcomingRules(any(), any())).thenReturn(listOf(rule))
        whenever(creditCardRepository.getUpcomingBills(any())).thenReturn(listOf(ccBill))
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        val calendarBills = repository.getBillsForCalendar(YearMonth.now())

        assertEquals(2, calendarBills[dueDate]?.size)
    }

    // Test 25: getBillsForCalendar only returns bills for specified month
    @Test
    fun `getBillsForCalendar filters to specified month only`() = runTest {
        val thisMonth = YearMonth.now()
        val rule = createRecurringRule(nextExpected = thisMonth.atDay(15))
        whenever(recurringRuleRepository.getUpcomingRules(any(), any())).thenReturn(listOf(rule))
        whenever(creditCardRepository.getUpcomingBills(any())).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        val calendarBills = repository.getBillsForCalendar(thisMonth)

        calendarBills.keys.forEach { date ->
            assertEquals(thisMonth, YearMonth.from(date))
        }
    }

    // ==================== Mapping Tests (5 tests) ====================

    // Test 26: mapRecurringRuleToBill creates correct UpcomingBill
    @Test
    fun `mapRecurringRuleToBill creates bill with correct fields`() = runTest {
        val rule = createRecurringRule(
            id = 42L,
            merchantPattern = "NETFLIX",
            expectedAmount = 649.0,
            nextExpected = LocalDate.of(2024, 3, 15),
            categoryId = 5L
        )

        val bill = repository.mapRecurringRuleToBill(rule)

        assertEquals("RECURRING_RULE_42", bill.id)
        assertEquals(BillSource.RECURRING_RULE, bill.source)
        assertEquals("NETFLIX", bill.merchantName)
        assertEquals(649.0, bill.amount, 0.01)
        assertEquals(LocalDate.of(2024, 3, 15), bill.dueDate)
        assertEquals(5L, bill.categoryId)
        assertTrue(bill.isUserConfirmed)
        assertEquals(1.0f, bill.confidence)
    }

    // Test 27: mapCreditCardToBill creates correct UpcomingBill
    @Test
    fun `mapCreditCardToBill creates bill with credit card info`() = runTest {
        val card = createCreditCard(
            id = 10L,
            bankName = "HDFC",
            lastFour = "5678",
            previousDue = 12500.0,
            previousDueDate = LocalDate.of(2024, 3, 20)
        )

        val bill = repository.mapCreditCardToBill(card)

        assertEquals("CREDIT_CARD_10", bill.id)
        assertEquals(BillSource.CREDIT_CARD, bill.source)
        assertEquals("HDFC Credit Card", bill.displayName)
        assertEquals(12500.0, bill.amount, 0.01)
        assertEquals("5678", bill.creditCardLastFour)
        assertTrue(bill.isUserConfirmed)
        assertNull(bill.frequency)
    }

    // Test 28: mapPatternSuggestionToBill creates unconfirmed bill
    @Test
    fun `mapPatternSuggestionToBill creates unconfirmed bill`() = runTest {
        val suggestion = createPatternSuggestion(
            merchantPattern = "ELECTRICITY",
            averageAmount = 2500.0,
            confidence = 0.85f,
            nextExpected = LocalDate.of(2024, 3, 10)
        )

        val bill = repository.mapPatternSuggestionToBill(suggestion)

        assertEquals(BillSource.PATTERN_SUGGESTION, bill.source)
        assertEquals(2500.0, bill.amount, 0.01)
        assertEquals(0.85f, bill.confidence)
        assertFalse(bill.isUserConfirmed)
    }

    // Test 29: Bill status is calculated correctly
    @Test
    fun `mapped bills have correct status based on due date`() = runTest {
        val overdueRule = createRecurringRule(nextExpected = LocalDate.now().minusDays(3))
        val todayRule = createRecurringRule(nextExpected = LocalDate.now())
        val upcomingRule = createRecurringRule(nextExpected = LocalDate.now().plusDays(10))

        val overdueBill = repository.mapRecurringRuleToBill(overdueRule)
        val todayBill = repository.mapRecurringRuleToBill(todayRule)
        val upcomingBill = repository.mapRecurringRuleToBill(upcomingRule)

        assertEquals(BillStatus.OVERDUE, overdueBill.status)
        assertEquals(BillStatus.DUE_TODAY, todayBill.status)
        assertEquals(BillStatus.UPCOMING, upcomingBill.status)
    }

    // Test 30: Credit card bill without due date uses today
    @Test
    fun `mapCreditCardToBill handles null due date`() = runTest {
        val card = createCreditCard(previousDueDate = null, previousDue = 5000.0)

        val bill = repository.mapCreditCardToBill(card)

        assertNotNull(bill.dueDate)
    }

    // ==================== Payment Tracking Tests (3 tests) ====================

    // Test 31: markBillAsPaid updates recurring rule
    @Test
    fun `markBillAsPaid records occurrence for recurring rule`() = runTest {
        val bill = createUpcomingBill(
            source = BillSource.RECURRING_RULE,
            sourceId = 100L,
            dueDate = LocalDate.now()
        )

        repository.markBillAsPaid(bill, null)

        verify(recurringRuleRepository).recordOccurrence(eq(100L), any(), any())
    }

    // Test 32: markBillAsPaid does not affect credit card bills
    @Test
    fun `markBillAsPaid does nothing for credit card bills`() = runTest {
        val bill = createUpcomingBill(
            source = BillSource.CREDIT_CARD,
            sourceId = 50L
        )

        repository.markBillAsPaid(bill, null)

        verify(recurringRuleRepository, never()).recordOccurrence(any(), any(), any())
    }

    // Test 33: markBillAsPaid does not affect pattern suggestions
    @Test
    fun `markBillAsPaid does nothing for pattern suggestions`() = runTest {
        val bill = createUpcomingBill(
            source = BillSource.PATTERN_SUGGESTION,
            sourceId = 0L
        )

        repository.markBillAsPaid(bill, null)

        verify(recurringRuleRepository, never()).recordOccurrence(any(), any(), any())
    }

    // ==================== Pattern Refresh Tests (2 tests) ====================

    // Test 34: refreshPatternSuggestions returns new suggestions
    @Test
    fun `refreshPatternSuggestions returns detected patterns`() = runTest {
        val suggestion = createPatternSuggestion()
        whenever(patternDetectionService.detectPatterns()).thenReturn(listOf(suggestion))

        val suggestions = repository.refreshPatternSuggestions()

        assertEquals(1, suggestions.size)
    }

    // Test 35: refreshPatternSuggestions returns empty when no patterns
    @Test
    fun `refreshPatternSuggestions returns empty when no patterns detected`() = runTest {
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        val suggestions = repository.refreshPatternSuggestions()

        assertTrue(suggestions.isEmpty())
    }

    // ==================== Helper Functions ====================

    private fun createRecurringRule(
        id: Long = 1L,
        merchantPattern: String = "TEST_MERCHANT",
        categoryId: Long = 1L,
        expectedAmount: Double = 1000.0,
        frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
        nextExpected: LocalDate? = LocalDate.now().plusDays(7),
        isUserConfirmed: Boolean = true
    ): RecurringRule {
        return RecurringRule(
            id = id,
            merchantPattern = merchantPattern,
            categoryId = categoryId,
            expectedAmount = expectedAmount,
            frequency = frequency,
            nextExpected = nextExpected,
            isUserConfirmed = isUserConfirmed
        )
    }

    private fun createCreditCard(
        id: Long = 1L,
        bankName: String = "HDFC",
        lastFour: String = "1234",
        previousDue: Double = 10000.0,
        previousDueDate: LocalDate? = LocalDate.now().plusDays(10)
    ): CreditCard {
        return CreditCard(
            id = id,
            bankName = bankName,
            lastFourDigits = lastFour,
            previousDue = previousDue,
            previousDueDate = previousDueDate
        )
    }

    private fun createPatternSuggestion(
        merchantPattern: String = "SUGGESTED_MERCHANT",
        averageAmount: Double = 500.0,
        confidence: Float = 0.8f,
        nextExpected: LocalDate = LocalDate.now().plusDays(10)
    ): PatternSuggestion {
        return PatternSuggestion(
            merchantPattern = merchantPattern,
            displayName = merchantPattern,
            averageAmount = averageAmount,
            detectedFrequency = RecurringFrequency.MONTHLY,
            typicalDayOfPeriod = 15,
            occurrenceCount = 3,
            confidence = confidence,
            nextExpected = nextExpected,
            categoryId = null
        )
    }

    private fun createUpcomingBill(
        source: BillSource = BillSource.RECURRING_RULE,
        sourceId: Long = 1L,
        dueDate: LocalDate = LocalDate.now().plusDays(7)
    ): UpcomingBill {
        return UpcomingBill(
            id = UpcomingBill.generateBillId(source, sourceId),
            source = source,
            merchantName = "Test",
            displayName = "Test Bill",
            amount = 1000.0,
            amountVariance = null,
            dueDate = dueDate,
            frequency = RecurringFrequency.MONTHLY,
            categoryId = null,
            status = BillStatus.calculateStatus(dueDate, false),
            isPaid = false,
            isUserConfirmed = true,
            confidence = 1.0f,
            creditCardLastFour = null,
            sourceId = sourceId
        )
    }
}
