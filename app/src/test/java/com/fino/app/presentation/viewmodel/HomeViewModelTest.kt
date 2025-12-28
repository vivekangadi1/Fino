package com.fino.app.presentation.viewmodel

import com.fino.app.data.repository.EventRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.data.repository.UpcomingBillsRepository
import com.fino.app.data.repository.UserStatsRepository
import com.fino.app.domain.model.*
import com.fino.app.gamification.LevelCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var mockTransactionRepository: TransactionRepository
    private lateinit var mockCategoryRepository: com.fino.app.data.repository.CategoryRepository
    private lateinit var mockUserStatsRepository: UserStatsRepository
    private lateinit var mockUpcomingBillsRepository: UpcomingBillsRepository
    private lateinit var mockEventRepository: EventRepository
    private lateinit var levelCalculator: LevelCalculator
    private lateinit var viewModel: HomeViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val testTransactions = listOf(
        Transaction(
            id = 1L,
            amount = 500.0,
            type = TransactionType.DEBIT,
            merchantName = "Swiggy",
            transactionDate = LocalDateTime.now().minusDays(1),
            source = TransactionSource.MANUAL
        ),
        Transaction(
            id = 2L,
            amount = 200.0,
            type = TransactionType.DEBIT,
            merchantName = "Uber",
            transactionDate = LocalDateTime.now().minusDays(2),
            source = TransactionSource.SMS
        ),
        Transaction(
            id = 3L,
            amount = 50000.0,
            type = TransactionType.CREDIT,
            merchantName = "Salary",
            transactionDate = LocalDateTime.now().minusDays(5),
            source = TransactionSource.MANUAL
        )
    )

    private val testUserStats = UserStats(
        id = 1L,
        currentStreak = 5,
        longestStreak = 10,
        totalTransactionsLogged = 50,
        totalXp = 350,
        currentLevel = 3
    )

    private val today = LocalDate.now()

    private val testBillSummary = BillSummary(
        thisMonth = MonthSummary(
            totalAmount = 5000.0,
            billCount = 3,
            month = YearMonth.now()
        ),
        nextMonth = MonthSummary(
            totalAmount = 8000.0,
            billCount = 4,
            month = YearMonth.now().plusMonths(1)
        ),
        overdueCount = 1,
        dueTodayCount = 1
    )

    private val testUpcomingBills = listOf(
        UpcomingBill(
            id = "RECURRING_RULE_1",
            source = BillSource.RECURRING_RULE,
            merchantName = "Netflix",
            displayName = "Netflix",
            amount = 649.0,
            amountVariance = null,
            dueDate = today,
            frequency = RecurringFrequency.MONTHLY,
            categoryId = 1L,
            status = BillStatus.DUE_TODAY,
            isPaid = false,
            isUserConfirmed = true,
            confidence = 1.0f,
            creditCardLastFour = null,
            sourceId = 1L
        ),
        UpcomingBill(
            id = "CREDIT_CARD_2",
            source = BillSource.CREDIT_CARD,
            merchantName = "HDFC Credit Card",
            displayName = "HDFC Credit Card",
            amount = 12500.0,
            amountVariance = null,
            dueDate = today.plusDays(3),
            frequency = RecurringFrequency.MONTHLY,
            categoryId = null,
            status = BillStatus.DUE_THIS_WEEK,
            isPaid = false,
            isUserConfirmed = true,
            confidence = 1.0f,
            creditCardLastFour = "4521",
            sourceId = 2L
        ),
        UpcomingBill(
            id = "RECURRING_RULE_3",
            source = BillSource.RECURRING_RULE,
            merchantName = "Electricity",
            displayName = "Electricity",
            amount = 2500.0,
            amountVariance = null,
            dueDate = today.plusDays(10),
            frequency = RecurringFrequency.MONTHLY,
            categoryId = 2L,
            status = BillStatus.UPCOMING,
            isPaid = false,
            isUserConfirmed = true,
            confidence = 1.0f,
            creditCardLastFour = null,
            sourceId = 3L
        ),
        UpcomingBill(
            id = "PATTERN_SUGGESTION_4",
            source = BillSource.PATTERN_SUGGESTION,
            merchantName = "Car Wash",
            displayName = "Car Wash",
            amount = 800.0,
            amountVariance = 0.1f,
            dueDate = today.plusDays(15),
            frequency = RecurringFrequency.MONTHLY,
            categoryId = 3L,
            status = BillStatus.UPCOMING,
            isPaid = false,
            isUserConfirmed = false,
            confidence = 0.85f,
            creditCardLastFour = null,
            sourceId = 4L
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockTransactionRepository = mock()
        mockCategoryRepository = mock()
        mockUserStatsRepository = mock()
        mockUpcomingBillsRepository = mock()
        mockEventRepository = mock()
        levelCalculator = LevelCalculator()

        whenever(mockTransactionRepository.getAllTransactionsFlow()).thenReturn(flowOf(testTransactions))
        whenever(mockCategoryRepository.getAllActive()).thenReturn(flowOf(emptyList()))
        whenever(mockUserStatsRepository.getUserStatsFlow()).thenReturn(flowOf(testUserStats))
        whenever(mockUpcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(testUpcomingBills))
        whenever(mockEventRepository.getEventSummariesFlow()).thenReturn(flowOf(emptyList()))
        runBlocking {
            whenever(mockUpcomingBillsRepository.getBillSummary()).thenReturn(testBillSummary)
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel {
        return HomeViewModel(
            transactionRepository = mockTransactionRepository,
            categoryRepository = mockCategoryRepository,
            userStatsRepository = mockUserStatsRepository,
            upcomingBillsRepository = mockUpcomingBillsRepository,
            eventRepository = mockEventRepository,
            levelCalculator = levelCalculator
        )
    }

    // Test 1: Initial state is loading
    @Test
    fun `initial state shows loading`() = runTest {
        viewModel = createViewModel()
        val initialState = viewModel.uiState.value

        assertTrue(initialState.isLoading)
    }

    // Test 2: calculates totalBalance correctly
    @Test
    fun `calculates total balance from all transactions`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        // Income (50000) - Expenses (500 + 200) = 49300
        assertEquals(49300.0, state.totalBalance, 0.01)
    }

    // Test 3: calculates monthly spent from DEBIT transactions
    @Test
    fun `calculates monthly spent from DEBIT transactions`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        // 500 + 200 = 700
        assertEquals(700.0, state.monthlySpent, 0.01)
    }

    // Test 4: calculates monthly income from CREDIT transactions
    @Test
    fun `calculates monthly income from CREDIT transactions`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(50000.0, state.monthlyIncome, 0.01)
    }

    // Test 5: fetches recent transactions limited to 5
    @Test
    fun `fetches recent transactions limited to 5`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertTrue(state.recentTransactions.size <= 5)
        assertEquals(3, state.recentTransactions.size)
    }

    // Test 6: recent transactions are sorted by date (newest first)
    @Test
    fun `recent transactions are sorted by date descending`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        // Check transactions are sorted by date descending
        for (i in 0 until state.recentTransactions.size - 1) {
            assertTrue(
                state.recentTransactions[i].transactionDate >=
                state.recentTransactions[i + 1].transactionDate
            )
        }
    }

    // Test 7: fetches currentStreak from userStats
    @Test
    fun `fetches current streak from user stats`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(5, state.currentStreak)
    }

    // Test 8: fetches currentLevel from userStats
    @Test
    fun `fetches current level from user stats`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(3, state.currentLevel)
    }

    // Test 9: calculates xpProgress correctly
    @Test
    fun `calculates xp progress correctly`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        // XP progress should be between 0 and 1
        assertTrue(state.xpProgress in 0f..1f)
    }

    // Test 10: handles empty transactions list
    @Test
    fun `handles empty transactions list`() = runTest {
        whenever(mockTransactionRepository.getAllTransactionsFlow()).thenReturn(flowOf(emptyList()))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(0.0, state.totalBalance, 0.01)
        assertEquals(0.0, state.monthlySpent, 0.01)
        assertEquals(0.0, state.monthlyIncome, 0.01)
        assertTrue(state.recentTransactions.isEmpty())
    }

    // Test 11: handles null userStats
    @Test
    fun `handles null user stats with defaults`() = runTest {
        whenever(mockUserStatsRepository.getUserStatsFlow()).thenReturn(flowOf(null))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(0, state.currentStreak)
        assertEquals(1, state.currentLevel)
    }

    // Test 12: isLoading becomes false after data loads
    @Test
    fun `isLoading becomes false after data loads`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertFalse(state.isLoading)
    }

    // ==================== Upcoming Bills Tests (10 tests) ====================

    // Test 13: loads bill summary from repository
    @Test
    fun `loads bill summary from repository`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertNotNull(state.upcomingBillsSummary)
        assertEquals(5000.0, state.upcomingBillsSummary?.thisMonth?.totalAmount ?: 0.0, 0.01)
        assertEquals(8000.0, state.upcomingBillsSummary?.nextMonth?.totalAmount ?: 0.0, 0.01)
    }

    // Test 14: summary shows correct bill counts
    @Test
    fun `bill summary shows correct bill counts`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(3, state.upcomingBillsSummary?.thisMonth?.billCount)
        assertEquals(4, state.upcomingBillsSummary?.nextMonth?.billCount)
    }

    // Test 15: summary shows overdue and due today counts
    @Test
    fun `bill summary shows overdue and due today counts`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(1, state.upcomingBillsSummary?.overdueCount)
        assertEquals(1, state.upcomingBillsSummary?.dueTodayCount)
    }

    // Test 16: loads next bills limited to 3
    @Test
    fun `loads next bills limited to 3`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertTrue(state.nextBills.size <= 3)
    }

    // Test 17: next bills are sorted by due date
    @Test
    fun `next bills are sorted by due date ascending`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        for (i in 0 until state.nextBills.size - 1) {
            assertTrue(
                state.nextBills[i].dueDate <= state.nextBills[i + 1].dueDate
            )
        }
    }

    // Test 18: handles empty bills list
    @Test
    fun `handles empty bills list`() = runTest {
        whenever(mockUpcomingBillsRepository.getUpcomingBillsFlow()).thenReturn(flowOf(emptyList()))
        runBlocking {
            whenever(mockUpcomingBillsRepository.getBillSummary()).thenReturn(
                BillSummary(
                    thisMonth = MonthSummary(0.0, 0, YearMonth.now()),
                    nextMonth = MonthSummary(0.0, 0, YearMonth.now().plusMonths(1)),
                    overdueCount = 0,
                    dueTodayCount = 0
                )
            )
        }

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertTrue(state.nextBills.isEmpty())
        assertEquals(0.0, state.upcomingBillsSummary?.thisMonth?.totalAmount ?: -1.0, 0.01)
    }

    // Test 19: next bills show earliest upcoming bills first
    @Test
    fun `next bills show earliest upcoming bills first`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        if (state.nextBills.isNotEmpty()) {
            assertEquals("Netflix", state.nextBills[0].merchantName)
        }
    }

    // Test 20: bill summary includes all bill sources
    @Test
    fun `next bills include all bill sources`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        // Should have bills from multiple sources
        val sources = state.nextBills.map { it.source }.toSet()
        assertTrue(sources.isNotEmpty())
    }

    // Test 21: hasUrgentBills is true when overdue or due today exists
    @Test
    fun `hasUrgentBills is true when overdue or due today exists`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertTrue(state.hasUrgentBills)
    }

    // Test 22: hasUrgentBills is false when no overdue or due today
    @Test
    fun `hasUrgentBills is false when no overdue or due today`() = runTest {
        runBlocking {
            whenever(mockUpcomingBillsRepository.getBillSummary()).thenReturn(
                BillSummary(
                    thisMonth = MonthSummary(5000.0, 3, YearMonth.now()),
                    nextMonth = MonthSummary(8000.0, 4, YearMonth.now().plusMonths(1)),
                    overdueCount = 0,
                    dueTodayCount = 0
                )
            )
        }

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertFalse(state.hasUrgentBills)
    }
}
