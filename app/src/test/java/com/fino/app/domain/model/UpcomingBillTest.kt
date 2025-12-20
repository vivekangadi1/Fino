package com.fino.app.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class UpcomingBillTest {

    // Test 1: Status calculation - OVERDUE when due date is past and not paid
    @Test
    fun `calculateStatus returns OVERDUE when dueDate is past and not paid`() {
        val pastDate = LocalDate.now().minusDays(5)
        val status = BillStatus.calculateStatus(pastDate, isPaid = false)
        assertEquals(BillStatus.OVERDUE, status)
    }

    // Test 2: Status calculation - DUE_TODAY when due date is today
    @Test
    fun `calculateStatus returns DUE_TODAY when dueDate is today`() {
        val today = LocalDate.now()
        val status = BillStatus.calculateStatus(today, isPaid = false)
        assertEquals(BillStatus.DUE_TODAY, status)
    }

    // Test 3: Status calculation - DUE_TOMORROW when due date is tomorrow
    @Test
    fun `calculateStatus returns DUE_TOMORROW when dueDate is tomorrow`() {
        val tomorrow = LocalDate.now().plusDays(1)
        val status = BillStatus.calculateStatus(tomorrow, isPaid = false)
        assertEquals(BillStatus.DUE_TOMORROW, status)
    }

    // Test 4: Status calculation - DUE_THIS_WEEK when within 7 days
    @Test
    fun `calculateStatus returns DUE_THIS_WEEK when dueDate is within 7 days`() {
        val inFiveDays = LocalDate.now().plusDays(5)
        val status = BillStatus.calculateStatus(inFiveDays, isPaid = false)
        assertEquals(BillStatus.DUE_THIS_WEEK, status)
    }

    // Test 5: Status calculation - UPCOMING for future dates beyond this week
    @Test
    fun `calculateStatus returns UPCOMING for future dates beyond this week`() {
        val inTwoWeeks = LocalDate.now().plusDays(14)
        val status = BillStatus.calculateStatus(inTwoWeeks, isPaid = false)
        assertEquals(BillStatus.UPCOMING, status)
    }

    // Test 6: Status is not OVERDUE when bill is paid
    @Test
    fun `calculateStatus returns UPCOMING when bill is paid even if date is past`() {
        val pastDate = LocalDate.now().minusDays(5)
        val status = BillStatus.calculateStatus(pastDate, isPaid = true)
        // Paid bills should show as UPCOMING (completed), not OVERDUE
        assertEquals(BillStatus.UPCOMING, status)
    }

    // Test 7: Generate unique ID from source and sourceId
    @Test
    fun `generateBillId creates unique id from source and sourceId`() {
        val id1 = UpcomingBill.generateBillId(BillSource.RECURRING_RULE, 123L)
        val id2 = UpcomingBill.generateBillId(BillSource.CREDIT_CARD, 123L)
        val id3 = UpcomingBill.generateBillId(BillSource.RECURRING_RULE, 456L)

        assertEquals("RECURRING_RULE_123", id1)
        assertEquals("CREDIT_CARD_123", id2)
        assertEquals("RECURRING_RULE_456", id3)
        assertNotEquals(id1, id2)
        assertNotEquals(id1, id3)
    }

    // Test 8: Credit card bills have creditCardLastFour populated
    @Test
    fun `credit card bill has creditCardLastFour populated`() {
        val bill = createTestBill(
            source = BillSource.CREDIT_CARD,
            creditCardLastFour = "4523"
        )
        assertEquals("4523", bill.creditCardLastFour)
        assertEquals(BillSource.CREDIT_CARD, bill.source)
    }

    // Test 9: Pattern suggestions have confidence less than 1.0
    @Test
    fun `pattern suggestion bill has confidence less than 1`() {
        val bill = createTestBill(
            source = BillSource.PATTERN_SUGGESTION,
            confidence = 0.85f,
            isUserConfirmed = false
        )
        assertTrue(bill.confidence < 1.0f)
        assertFalse(bill.isUserConfirmed)
    }

    // Test 10: Recurring rules have isUserConfirmed true
    @Test
    fun `recurring rule bill has isUserConfirmed true`() {
        val bill = createTestBill(
            source = BillSource.RECURRING_RULE,
            isUserConfirmed = true,
            confidence = 1.0f
        )
        assertTrue(bill.isUserConfirmed)
        assertEquals(1.0f, bill.confidence)
    }

    // Test 11: Amount variance is null for credit card bills
    @Test
    fun `amountVariance is null for credit card bills`() {
        val bill = createTestBill(
            source = BillSource.CREDIT_CARD,
            amountVariance = null
        )
        assertNull(bill.amountVariance)
    }

    // Test 12: Frequency is null for credit card bills
    @Test
    fun `frequency is null for credit card bills`() {
        val bill = createTestBill(
            source = BillSource.CREDIT_CARD,
            frequency = null
        )
        assertNull(bill.frequency)
    }

    // Test 13: CategoryId can be null for uncategorized bills
    @Test
    fun `categoryId can be null for uncategorized bills`() {
        val bill = createTestBill(categoryId = null)
        assertNull(bill.categoryId)
    }

    // Test 14: Equals and hashCode work correctly
    @Test
    fun `equals and hashCode work correctly for bill comparison`() {
        val bill1 = createTestBill(sourceId = 100L)
        val bill2 = createTestBill(sourceId = 100L)
        val bill3 = createTestBill(sourceId = 200L)

        assertEquals(bill1, bill2)
        assertEquals(bill1.hashCode(), bill2.hashCode())
        assertNotEquals(bill1, bill3)
    }

    // Test 15: Bill with all fields populated
    @Test
    fun `creates bill with all required fields`() {
        val dueDate = LocalDate.now().plusDays(10)
        val bill = UpcomingBill(
            id = "RECURRING_RULE_1",
            source = BillSource.RECURRING_RULE,
            merchantName = "Netflix",
            displayName = "Netflix Subscription",
            amount = 649.0,
            amountVariance = 0.0f,
            dueDate = dueDate,
            frequency = RecurringFrequency.MONTHLY,
            categoryId = 5L,
            status = BillStatus.UPCOMING,
            isPaid = false,
            isUserConfirmed = true,
            confidence = 1.0f,
            creditCardLastFour = null,
            sourceId = 1L
        )

        assertEquals("RECURRING_RULE_1", bill.id)
        assertEquals(BillSource.RECURRING_RULE, bill.source)
        assertEquals("Netflix", bill.merchantName)
        assertEquals(649.0, bill.amount, 0.01)
        assertEquals(dueDate, bill.dueDate)
        assertEquals(RecurringFrequency.MONTHLY, bill.frequency)
        assertFalse(bill.isPaid)
    }

    // Helper function to create test bills
    private fun createTestBill(
        source: BillSource = BillSource.RECURRING_RULE,
        sourceId: Long = 1L,
        merchantName: String = "Test Merchant",
        amount: Double = 1000.0,
        dueDate: LocalDate = LocalDate.now().plusDays(7),
        frequency: RecurringFrequency? = RecurringFrequency.MONTHLY,
        categoryId: Long? = 1L,
        isPaid: Boolean = false,
        isUserConfirmed: Boolean = true,
        confidence: Float = 1.0f,
        creditCardLastFour: String? = null,
        amountVariance: Float? = 0.1f
    ): UpcomingBill {
        return UpcomingBill(
            id = UpcomingBill.generateBillId(source, sourceId),
            source = source,
            merchantName = merchantName,
            displayName = merchantName,
            amount = amount,
            amountVariance = amountVariance,
            dueDate = dueDate,
            frequency = frequency,
            categoryId = categoryId,
            status = BillStatus.calculateStatus(dueDate, isPaid),
            isPaid = isPaid,
            isUserConfirmed = isUserConfirmed,
            confidence = confidence,
            creditCardLastFour = creditCardLastFour,
            sourceId = sourceId
        )
    }
}

class BillSummaryTest {

    @Test
    fun `creates bill summary with correct totals`() {
        val thisMonth = MonthSummary(
            totalAmount = 8450.0,
            billCount = 3,
            month = YearMonth.now()
        )
        val nextMonth = MonthSummary(
            totalAmount = 16449.0,
            billCount = 5,
            month = YearMonth.now().plusMonths(1)
        )

        val summary = BillSummary(
            thisMonth = thisMonth,
            nextMonth = nextMonth,
            overdueCount = 1,
            dueTodayCount = 2
        )

        assertEquals(8450.0, summary.thisMonth.totalAmount, 0.01)
        assertEquals(3, summary.thisMonth.billCount)
        assertEquals(16449.0, summary.nextMonth.totalAmount, 0.01)
        assertEquals(5, summary.nextMonth.billCount)
        assertEquals(1, summary.overdueCount)
        assertEquals(2, summary.dueTodayCount)
    }

    @Test
    fun `month summary handles zero bills`() {
        val summary = MonthSummary(
            totalAmount = 0.0,
            billCount = 0,
            month = YearMonth.now()
        )

        assertEquals(0.0, summary.totalAmount, 0.01)
        assertEquals(0, summary.billCount)
    }
}

class BillGroupTest {

    @Test
    fun `creates bill group with correct type and label`() {
        val group = BillGroup(
            type = BillGroupType.TODAY,
            label = "Today",
            bills = emptyList()
        )

        assertEquals(BillGroupType.TODAY, group.type)
        assertEquals("Today", group.label)
        assertTrue(group.bills.isEmpty())
    }

    @Test
    fun `bill group type values are correct`() {
        val types = BillGroupType.values()
        assertEquals(5, types.size)
        assertTrue(types.contains(BillGroupType.TODAY))
        assertTrue(types.contains(BillGroupType.TOMORROW))
        assertTrue(types.contains(BillGroupType.THIS_WEEK))
        assertTrue(types.contains(BillGroupType.LATER_THIS_MONTH))
        assertTrue(types.contains(BillGroupType.NEXT_MONTH))
    }
}

class PatternSuggestionTest {

    @Test
    fun `creates pattern suggestion with all fields`() {
        val suggestion = PatternSuggestion(
            merchantPattern = "NETFLIX",
            displayName = "Netflix",
            averageAmount = 649.0,
            detectedFrequency = RecurringFrequency.MONTHLY,
            typicalDayOfPeriod = 15,
            occurrenceCount = 3,
            confidence = 0.85f,
            nextExpected = LocalDate.now().plusDays(10),
            categoryId = 5L
        )

        assertEquals("NETFLIX", suggestion.merchantPattern)
        assertEquals("Netflix", suggestion.displayName)
        assertEquals(649.0, suggestion.averageAmount, 0.01)
        assertEquals(RecurringFrequency.MONTHLY, suggestion.detectedFrequency)
        assertEquals(15, suggestion.typicalDayOfPeriod)
        assertEquals(3, suggestion.occurrenceCount)
        assertEquals(0.85f, suggestion.confidence)
        assertEquals(5L, suggestion.categoryId)
    }

    @Test
    fun `pattern suggestion categoryId can be null`() {
        val suggestion = PatternSuggestion(
            merchantPattern = "UNKNOWN_MERCHANT",
            displayName = "Unknown Merchant",
            averageAmount = 500.0,
            detectedFrequency = RecurringFrequency.MONTHLY,
            typicalDayOfPeriod = 1,
            occurrenceCount = 2,
            confidence = 0.7f,
            nextExpected = LocalDate.now().plusMonths(1),
            categoryId = null
        )

        assertNull(suggestion.categoryId)
    }
}
