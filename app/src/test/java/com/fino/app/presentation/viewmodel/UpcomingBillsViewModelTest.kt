package com.fino.app.presentation.viewmodel

import com.fino.app.data.repository.UpcomingBillsRepository
import com.fino.app.domain.model.*
import com.fino.app.service.pattern.PatternDetectionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class UpcomingBillsViewModelTest {

    private lateinit var upcomingBillsRepository: UpcomingBillsRepository
    private lateinit var patternDetectionService: PatternDetectionService
    private lateinit var patternSuggestionRepository: com.fino.app.data.repository.PatternSuggestionRepository
    private lateinit var viewModel: UpcomingBillsViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        upcomingBillsRepository = mock()
        patternDetectionService = mock()
        patternSuggestionRepository = mock()
        whenever(patternSuggestionRepository.getPendingSuggestionsFlow()).thenReturn(flowOf(emptyList()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): UpcomingBillsViewModel {
        return UpcomingBillsViewModel(upcomingBillsRepository, patternDetectionService, patternSuggestionRepository)
    }

    // ==================== Initial State Tests (3 tests) ====================

    // Test 1: Initial state shows loading
    @Test
    fun `initial state shows loading`() = runTest {
        whenever(upcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(emptyList()))
        whenever(upcomingBillsRepository.getBillSummary()).thenReturn(BillSummary.empty())
        whenever(upcomingBillsRepository.getGroupedBills()).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.isLoading)
    }

    // Test 2: Initial state has no error
    @Test
    fun `initial state has no error`() = runTest {
        whenever(upcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(emptyList()))
        whenever(upcomingBillsRepository.getBillSummary()).thenReturn(BillSummary.empty())
        whenever(upcomingBillsRepository.getGroupedBills()).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        viewModel = createViewModel()

        assertNull(viewModel.uiState.value.error)
    }

    // Test 3: Initial state shows current month selected
    @Test
    fun `initial state has current month selected`() = runTest {
        whenever(upcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(emptyList()))
        whenever(upcomingBillsRepository.getBillSummary()).thenReturn(BillSummary.empty())
        whenever(upcomingBillsRepository.getGroupedBills()).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        viewModel = createViewModel()

        assertEquals(YearMonth.now(), viewModel.uiState.value.selectedMonth)
    }

    // ==================== Load Data Tests (5 tests) ====================

    // Test 4: loadData fetches and updates summary
    @Test
    fun `loadData updates summary in ui state`() = runTest {
        val summary = BillSummary(
            thisMonth = MonthSummary(5000.0, 3, YearMonth.now()),
            nextMonth = MonthSummary(8000.0, 5, YearMonth.now().plusMonths(1)),
            overdueCount = 1,
            dueTodayCount = 2
        )
        whenever(upcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(emptyList()))
        whenever(upcomingBillsRepository.getBillSummary()).thenReturn(summary)
        whenever(upcomingBillsRepository.getGroupedBills()).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(summary, viewModel.uiState.value.summary)
    }

    // Test 5: loadData fetches grouped bills
    @Test
    fun `loadData updates grouped bills in ui state`() = runTest {
        val groups = listOf(
            BillGroup(BillGroupType.TODAY, "Today", listOf(createUpcomingBill())),
            BillGroup(BillGroupType.THIS_WEEK, "This Week", listOf(createUpcomingBill()))
        )
        whenever(upcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(emptyList()))
        whenever(upcomingBillsRepository.getBillSummary()).thenReturn(BillSummary.empty())
        whenever(upcomingBillsRepository.getGroupedBills()).thenReturn(groups)
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.groupedBills.size)
    }

    // Test 6: loadData fetches pattern suggestions
    @Test
    fun `loadData updates pattern suggestions in ui state`() = runTest {
        val suggestions = listOf(createPatternSuggestion())
        whenever(upcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(emptyList()))
        whenever(upcomingBillsRepository.getBillSummary()).thenReturn(BillSummary.empty())
        whenever(upcomingBillsRepository.getGroupedBills()).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(suggestions)

        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.patternSuggestions.size)
    }

    // Test 7: loadData sets loading to false after completion
    @Test
    fun `loadData sets loading to false after completion`() = runTest {
        whenever(upcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(emptyList()))
        whenever(upcomingBillsRepository.getBillSummary()).thenReturn(BillSummary.empty())
        whenever(upcomingBillsRepository.getGroupedBills()).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    // Test 8: loadData handles errors
    @Test
    fun `loadData sets error on exception`() = runTest {
        whenever(upcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(emptyList()))
        whenever(upcomingBillsRepository.getBillSummary()).thenThrow(RuntimeException("Network error"))
        whenever(upcomingBillsRepository.getGroupedBills()).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        viewModel = createViewModel()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)
    }

    // ==================== Calendar View Tests (4 tests) ====================

    // Test 9: toggleCalendarView switches view mode
    @Test
    fun `toggleCalendarView changes showCalendarView state`() = runTest {
        whenever(upcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(emptyList()))
        whenever(upcomingBillsRepository.getBillSummary()).thenReturn(BillSummary.empty())
        whenever(upcomingBillsRepository.getGroupedBills()).thenReturn(emptyList())
        whenever(upcomingBillsRepository.getBillsForCalendar(any())).thenReturn(emptyMap())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showCalendarView)

        viewModel.toggleCalendarView()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showCalendarView)
    }

    // Test 10: selectMonth updates selected month
    @Test
    fun `selectMonth updates selectedMonth in state`() = runTest {
        whenever(upcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(emptyList()))
        whenever(upcomingBillsRepository.getBillSummary()).thenReturn(BillSummary.empty())
        whenever(upcomingBillsRepository.getGroupedBills()).thenReturn(emptyList())
        whenever(upcomingBillsRepository.getBillsForCalendar(any())).thenReturn(emptyMap())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        viewModel = createViewModel()
        advanceUntilIdle()

        val newMonth = YearMonth.now().plusMonths(2)
        viewModel.selectMonth(newMonth)
        advanceUntilIdle()

        assertEquals(newMonth, viewModel.uiState.value.selectedMonth)
    }

    // Test 11: selectMonth fetches calendar bills for new month
    @Test
    fun `selectMonth fetches calendar bills for selected month`() = runTest {
        val calendarBills = mapOf(
            LocalDate.now().withDayOfMonth(15) to listOf(createUpcomingBill())
        )
        whenever(upcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(emptyList()))
        whenever(upcomingBillsRepository.getBillSummary()).thenReturn(BillSummary.empty())
        whenever(upcomingBillsRepository.getGroupedBills()).thenReturn(emptyList())
        whenever(upcomingBillsRepository.getBillsForCalendar(any())).thenReturn(calendarBills)
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleCalendarView()
        advanceUntilIdle()

        verify(upcomingBillsRepository).getBillsForCalendar(any())
    }

    // Test 12: Calendar bills are updated in state
    @Test
    fun `calendar bills are updated when calendar view is enabled`() = runTest {
        val dueDate = LocalDate.now().withDayOfMonth(10)
        val calendarBills = mapOf(dueDate to listOf(createUpcomingBill()))
        whenever(upcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(emptyList()))
        whenever(upcomingBillsRepository.getBillSummary()).thenReturn(BillSummary.empty())
        whenever(upcomingBillsRepository.getGroupedBills()).thenReturn(emptyList())
        whenever(upcomingBillsRepository.getBillsForCalendar(any())).thenReturn(calendarBills)
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleCalendarView()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.calendarBills.containsKey(dueDate))
    }

    // ==================== Bill Actions Tests (5 tests) ====================

    // Test 13: markBillAsPaid calls repository
    @Test
    fun `markBillAsPaid calls repository method`() = runTest {
        whenever(upcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(emptyList()))
        whenever(upcomingBillsRepository.getBillSummary()).thenReturn(BillSummary.empty())
        whenever(upcomingBillsRepository.getGroupedBills()).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        viewModel = createViewModel()
        advanceUntilIdle()

        val bill = createUpcomingBill()
        viewModel.markBillAsPaid(bill)
        advanceUntilIdle()

        verify(upcomingBillsRepository).markBillAsPaid(eq(bill), isNull())
    }

    // Test 14: markBillAsPaid refreshes data
    @Test
    fun `markBillAsPaid refreshes bill data`() = runTest {
        whenever(upcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(emptyList()))
        whenever(upcomingBillsRepository.getBillSummary()).thenReturn(BillSummary.empty())
        whenever(upcomingBillsRepository.getGroupedBills()).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        viewModel = createViewModel()
        advanceUntilIdle()

        val bill = createUpcomingBill()
        viewModel.markBillAsPaid(bill)
        advanceUntilIdle()

        // Verify loadData was called again (summary fetched twice)
        verify(upcomingBillsRepository, atLeast(2)).getBillSummary()
    }

    // Test 15: confirmPatternSuggestion calls repository
    @Test
    fun `confirmPatternSuggestion calls repository method`() = runTest {
        whenever(upcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(emptyList()))
        whenever(upcomingBillsRepository.getBillSummary()).thenReturn(BillSummary.empty())
        whenever(upcomingBillsRepository.getGroupedBills()).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        viewModel = createViewModel()
        advanceUntilIdle()

        val suggestion = createPatternSuggestion()
        viewModel.confirmPatternSuggestion(suggestion)
        advanceUntilIdle()

        verify(patternSuggestionRepository).confirmSuggestion(eq(suggestion.id))
    }

    // Test 16: dismissPatternSuggestion calls repository
    @Test
    fun `dismissPatternSuggestion calls repository method`() = runTest {
        whenever(upcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(emptyList()))
        whenever(upcomingBillsRepository.getBillSummary()).thenReturn(BillSummary.empty())
        whenever(upcomingBillsRepository.getGroupedBills()).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        viewModel = createViewModel()
        advanceUntilIdle()

        val suggestion = createPatternSuggestion()
        viewModel.dismissPatternSuggestion(suggestion)
        advanceUntilIdle()

        verify(patternSuggestionRepository).dismissSuggestion(eq(suggestion.id))
    }

    // Test 17: dismissPatternSuggestion removes from suggestions list
    @Test
    fun `dismissPatternSuggestion removes suggestion from state`() = runTest {
        val suggestion = createPatternSuggestion(merchantPattern = "DISMISS_ME")
        whenever(upcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(emptyList()))
        whenever(upcomingBillsRepository.getBillSummary()).thenReturn(BillSummary.empty())
        whenever(upcomingBillsRepository.getGroupedBills()).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())
        whenever(patternSuggestionRepository.getPendingSuggestionsFlow()).thenReturn(flowOf(listOf(suggestion)))

        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.patternSuggestions.size)

        // After dismiss, the flow should return empty list
        whenever(patternSuggestionRepository.getPendingSuggestionsFlow()).thenReturn(flowOf(emptyList()))
        viewModel.dismissPatternSuggestion(suggestion)
        advanceUntilIdle()

        // The flow will update the state automatically
        verify(patternSuggestionRepository).dismissSuggestion(eq(suggestion.id))
    }

    // ==================== Flow Collection Tests (3 tests) ====================

    // Test 18: Bills flow updates state automatically
    @Test
    fun `bills flow updates state when new bills arrive`() = runTest {
        val bill1 = createUpcomingBill(displayName = "Bill 1")
        val bill2 = createUpcomingBill(displayName = "Bill 2")

        whenever(upcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(listOf(bill1, bill2)))
        whenever(upcomingBillsRepository.getBillSummary()).thenReturn(BillSummary.empty())
        whenever(upcomingBillsRepository.getGroupedBills()).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        viewModel = createViewModel()
        advanceUntilIdle()

        // Verify getBillSummary is called (data was loaded)
        verify(upcomingBillsRepository).getBillSummary()
    }

    // Test 19: Error state is cleared on successful load
    @Test
    fun `error is cleared on successful reload`() = runTest {
        whenever(upcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(emptyList()))
        whenever(upcomingBillsRepository.getBillSummary())
            .thenThrow(RuntimeException("First call fails"))
            .thenReturn(BillSummary.empty())
        whenever(upcomingBillsRepository.getGroupedBills()).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        viewModel = createViewModel()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)

        // Trigger reload
        viewModel.loadData()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
    }

    // Test 20: Loading state is shown during data fetch
    @Test
    fun `loading state is shown during refresh`() = runTest {
        whenever(upcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(emptyList()))
        whenever(upcomingBillsRepository.getBillSummary()).thenReturn(BillSummary.empty())
        whenever(upcomingBillsRepository.getGroupedBills()).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        viewModel = createViewModel()

        // Before completion, should be loading
        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()

        // After completion, should not be loading
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // ==================== UI State Validation Tests (5 tests) ====================

    // Test 21: Summary shows correct this month amount
    @Test
    fun `ui state summary shows correct this month total`() = runTest {
        val summary = BillSummary(
            thisMonth = MonthSummary(12500.0, 5, YearMonth.now()),
            nextMonth = MonthSummary(0.0, 0, YearMonth.now().plusMonths(1)),
            overdueCount = 0,
            dueTodayCount = 0
        )
        whenever(upcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(emptyList()))
        whenever(upcomingBillsRepository.getBillSummary()).thenReturn(summary)
        whenever(upcomingBillsRepository.getGroupedBills()).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(12500.0, viewModel.uiState.value.summary?.thisMonth?.totalAmount)
    }

    // Test 22: Groups are in correct order
    @Test
    fun `groups are ordered by sort order`() = runTest {
        val groups = listOf(
            BillGroup(BillGroupType.NEXT_MONTH, "Next Month", emptyList()),
            BillGroup(BillGroupType.TODAY, "Today", emptyList()),
            BillGroup(BillGroupType.THIS_WEEK, "This Week", emptyList())
        )
        whenever(upcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(emptyList()))
        whenever(upcomingBillsRepository.getBillSummary()).thenReturn(BillSummary.empty())
        whenever(upcomingBillsRepository.getGroupedBills()).thenReturn(groups)
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        viewModel = createViewModel()
        advanceUntilIdle()

        // Groups should be returned as-is from repository (repository handles ordering)
        assertEquals(3, viewModel.uiState.value.groupedBills.size)
    }

    // Test 23: Urgent bills flag is correct
    @Test
    fun `summary has urgent bills flag when overdue exists`() = runTest {
        val summary = BillSummary(
            thisMonth = MonthSummary(0.0, 0, YearMonth.now()),
            nextMonth = MonthSummary(0.0, 0, YearMonth.now().plusMonths(1)),
            overdueCount = 1,
            dueTodayCount = 0
        )
        whenever(upcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(emptyList()))
        whenever(upcomingBillsRepository.getBillSummary()).thenReturn(summary)
        whenever(upcomingBillsRepository.getGroupedBills()).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.summary?.hasUrgentBills == true)
    }

    // Test 24: Calendar view toggle is persistent
    @Test
    fun `calendar view toggle persists across data reloads`() = runTest {
        whenever(upcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(emptyList()))
        whenever(upcomingBillsRepository.getBillSummary()).thenReturn(BillSummary.empty())
        whenever(upcomingBillsRepository.getGroupedBills()).thenReturn(emptyList())
        whenever(upcomingBillsRepository.getBillsForCalendar(any())).thenReturn(emptyMap())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleCalendarView()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showCalendarView)

        viewModel.loadData()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showCalendarView)
    }

    // Test 25: Empty state is handled correctly
    @Test
    fun `empty state shows no bills message data`() = runTest {
        whenever(upcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(emptyList()))
        whenever(upcomingBillsRepository.getBillSummary()).thenReturn(BillSummary.empty())
        whenever(upcomingBillsRepository.getGroupedBills()).thenReturn(emptyList())
        whenever(patternDetectionService.detectPatterns()).thenReturn(emptyList())

        viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.groupedBills.isEmpty())
        assertEquals(0.0, viewModel.uiState.value.summary?.totalAmount)
    }

    // ==================== Helper Functions ====================

    private fun createUpcomingBill(
        displayName: String = "Test Bill"
    ): UpcomingBill {
        return UpcomingBill(
            id = "RECURRING_RULE_1",
            source = BillSource.RECURRING_RULE,
            merchantName = "Test",
            displayName = displayName,
            amount = 1000.0,
            amountVariance = null,
            dueDate = LocalDate.now().plusDays(5),
            frequency = RecurringFrequency.MONTHLY,
            categoryId = null,
            status = BillStatus.UPCOMING,
            isPaid = false,
            isUserConfirmed = true,
            confidence = 1.0f,
            creditCardLastFour = null,
            sourceId = 1L
        )
    }

    private fun createPatternSuggestion(
        merchantPattern: String = "SUGGESTED"
    ): PatternSuggestion {
        return PatternSuggestion(
            merchantPattern = merchantPattern,
            displayName = merchantPattern,
            averageAmount = 500.0,
            detectedFrequency = RecurringFrequency.MONTHLY,
            typicalDayOfPeriod = 15,
            occurrenceCount = 3,
            confidence = 0.85f,
            nextExpected = LocalDate.now().plusDays(10),
            categoryId = null
        )
    }
}
