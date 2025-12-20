package com.fino.app.presentation.viewmodel

import com.fino.app.data.repository.BudgetRepository
import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.Budget
import com.fino.app.domain.model.BudgetStatus
import com.fino.app.domain.model.Category
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionSource
import com.fino.app.domain.model.TransactionType
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
class AnalyticsViewModelTest {

    private lateinit var mockTransactionRepository: TransactionRepository
    private lateinit var mockCategoryRepository: CategoryRepository
    private lateinit var mockBudgetRepository: BudgetRepository
    private lateinit var mockExportService: com.fino.app.service.export.ExportService
    private lateinit var viewModel: AnalyticsViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val testCategories = listOf(
        Category(id = 1L, name = "Food", emoji = "🍔"),
        Category(id = 2L, name = "Transport", emoji = "🚗"),
        Category(id = 3L, name = "Shopping", emoji = "🛍️")
    )

    private val testTransactions = listOf(
        Transaction(
            id = 1L,
            amount = 500.0,
            type = TransactionType.DEBIT,
            merchantName = "Swiggy",
            categoryId = 1L,
            transactionDate = LocalDateTime.now().minusDays(1),
            source = TransactionSource.MANUAL
        ),
        Transaction(
            id = 2L,
            amount = 300.0,
            type = TransactionType.DEBIT,
            merchantName = "Zomato",
            categoryId = 1L,
            transactionDate = LocalDateTime.now().minusDays(2),
            source = TransactionSource.SMS
        ),
        Transaction(
            id = 3L,
            amount = 200.0,
            type = TransactionType.DEBIT,
            merchantName = "Uber",
            categoryId = 2L,
            transactionDate = LocalDateTime.now().minusDays(3),
            source = TransactionSource.MANUAL
        ),
        Transaction(
            id = 4L,
            amount = 1000.0,
            type = TransactionType.DEBIT,
            merchantName = "Amazon",
            categoryId = 3L,
            transactionDate = LocalDateTime.now().minusDays(4),
            source = TransactionSource.SMS
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockTransactionRepository = mock()
        mockCategoryRepository = mock()
        mockBudgetRepository = mock()
        mockExportService = mock()

        whenever(mockTransactionRepository.getAllTransactionsFlow()).thenReturn(flowOf(testTransactions))
        whenever(mockCategoryRepository.getAllActive()).thenReturn(flowOf(testCategories))

        // Default: return empty budgets
        runBlocking {
            whenever(mockBudgetRepository.getBudgetsForMonth(any())).thenReturn(emptyList())
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): AnalyticsViewModel {
        return AnalyticsViewModel(
            transactionRepository = mockTransactionRepository,
            categoryRepository = mockCategoryRepository,
            budgetRepository = mockBudgetRepository,
            exportService = mockExportService
        )
    }

    // Test 1: Initial state shows loading
    @Test
    fun `initial state shows loading`() = runTest {
        viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertTrue(state.isLoading)
    }

    // Test 2: Default period is MONTH
    @Test
    fun `default period is MONTH`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(AnalyticsPeriod.MONTH, state.selectedPeriod)
    }

    // Test 3: Calculates total spent correctly
    @Test
    fun `calculates total spent from DEBIT transactions`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        // 500 + 300 + 200 + 1000 = 2000
        assertEquals(2000.0, state.totalSpent, 0.01)
    }

    // Test 4: Counts transactions correctly
    @Test
    fun `counts transactions correctly`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(4, state.transactionCount)
    }

    // Test 5: Creates category breakdown
    @Test
    fun `creates category breakdown with spending per category`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertTrue(state.categoryBreakdown.isNotEmpty())
    }

    // Test 6: Category breakdown calculates spending correctly
    @Test
    fun `category breakdown calculates spending correctly`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        val foodCategory = state.categoryBreakdown.find { it.categoryName == "Food" }
        assertNotNull(foodCategory)
        assertEquals(800.0, foodCategory!!.amount, 0.01) // 500 + 300
    }

    // Test 7: Category breakdown calculates percentages
    @Test
    fun `category breakdown calculates percentage of total`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        val foodCategory = state.categoryBreakdown.find { it.categoryName == "Food" }
        assertNotNull(foodCategory)
        // Food is 800 out of 2000 total = 40%
        assertEquals(0.4f, foodCategory!!.percentage, 0.01f)
    }

    // Test 8: Category breakdown is sorted by amount descending
    @Test
    fun `category breakdown is sorted by amount descending`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        for (i in 0 until state.categoryBreakdown.size - 1) {
            assertTrue(
                state.categoryBreakdown[i].amount >= state.categoryBreakdown[i + 1].amount
            )
        }
    }

    // Test 9: setPeriod updates selected period
    @Test
    fun `setPeriod updates selected period`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setPeriod(AnalyticsPeriod.WEEK)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals(AnalyticsPeriod.WEEK, state.selectedPeriod)
    }

    // Test 10: Handles empty transactions
    @Test
    fun `handles empty transactions list`() = runTest {
        whenever(mockTransactionRepository.getAllTransactionsFlow()).thenReturn(flowOf(emptyList()))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(0.0, state.totalSpent, 0.01)
        assertEquals(0, state.transactionCount)
        assertTrue(state.categoryBreakdown.isEmpty())
    }

    // Test 11: Payment method breakdown - UPI grouping by bank
    @Test
    fun `payment method breakdown groups UPI by bank`() = runTest {
        val transactionsWithPaymentMethods = listOf(
            Transaction(
                id = 1L,
                amount = 500.0,
                type = TransactionType.DEBIT,
                merchantName = "Swiggy",
                categoryId = 1L,
                transactionDate = LocalDateTime.now().minusDays(1),
                source = TransactionSource.SMS,
                bankName = "HDFC",
                paymentMethod = "UPI"
            ),
            Transaction(
                id = 2L,
                amount = 300.0,
                type = TransactionType.DEBIT,
                merchantName = "Zomato",
                categoryId = 1L,
                transactionDate = LocalDateTime.now().minusDays(2),
                source = TransactionSource.SMS,
                bankName = "HDFC",
                paymentMethod = "UPI"
            ),
            Transaction(
                id = 3L,
                amount = 200.0,
                type = TransactionType.DEBIT,
                merchantName = "Uber",
                categoryId = 2L,
                transactionDate = LocalDateTime.now().minusDays(3),
                source = TransactionSource.SMS,
                bankName = "ICICI",
                paymentMethod = "UPI"
            )
        )

        whenever(mockTransactionRepository.getAllTransactionsFlow()).thenReturn(flowOf(transactionsWithPaymentMethods))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertNotNull(state.paymentMethodBreakdown)
        assertEquals(2, state.paymentMethodBreakdown!!.upiTransactions.size)

        // HDFC UPI should be first (800 = 500 + 300)
        val hdfcUpi = state.paymentMethodBreakdown!!.upiTransactions[0]
        assertEquals("HDFC", hdfcUpi.bankName)
        assertEquals(800.0, hdfcUpi.amount, 0.01)
        assertEquals(2, hdfcUpi.transactionCount)
        assertEquals("HDFC UPI", hdfcUpi.displayName)
    }

    // Test 12: Payment method breakdown - Credit card grouping by bank + last 4
    @Test
    fun `payment method breakdown groups credit cards by bank and card last four`() = runTest {
        val transactionsWithCreditCards = listOf(
            Transaction(
                id = 1L,
                amount = 1000.0,
                type = TransactionType.DEBIT,
                merchantName = "Amazon",
                categoryId = 3L,
                transactionDate = LocalDateTime.now().minusDays(1),
                source = TransactionSource.SMS,
                bankName = "HDFC",
                paymentMethod = "CREDIT_CARD",
                cardLastFour = "1234"
            ),
            Transaction(
                id = 2L,
                amount = 500.0,
                type = TransactionType.DEBIT,
                merchantName = "Flipkart",
                categoryId = 3L,
                transactionDate = LocalDateTime.now().minusDays(2),
                source = TransactionSource.SMS,
                bankName = "HDFC",
                paymentMethod = "CREDIT_CARD",
                cardLastFour = "5678"
            ),
            Transaction(
                id = 3L,
                amount = 300.0,
                type = TransactionType.DEBIT,
                merchantName = "Swiggy",
                categoryId = 1L,
                transactionDate = LocalDateTime.now().minusDays(3),
                source = TransactionSource.SMS,
                bankName = "ICICI",
                paymentMethod = "CREDIT_CARD",
                cardLastFour = "9012"
            )
        )

        whenever(mockTransactionRepository.getAllTransactionsFlow()).thenReturn(flowOf(transactionsWithCreditCards))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertNotNull(state.paymentMethodBreakdown)
        assertEquals(3, state.paymentMethodBreakdown!!.creditCardTransactions.size)

        // HDFC ****1234 should be first (1000)
        val hdfcCard1 = state.paymentMethodBreakdown!!.creditCardTransactions[0]
        assertEquals("HDFC", hdfcCard1.bankName)
        assertEquals("1234", hdfcCard1.cardLastFour)
        assertEquals(1000.0, hdfcCard1.amount, 0.01)
        assertEquals("HDFC ****1234", hdfcCard1.displayName)
    }

    // Test 13: Payment method breakdown - Handles unknown payment methods
    @Test
    fun `payment method breakdown handles unknown payment methods`() = runTest {
        val transactionsWithUnknown = listOf(
            Transaction(
                id = 1L,
                amount = 500.0,
                type = TransactionType.DEBIT,
                merchantName = "Cash Payment",
                categoryId = 1L,
                transactionDate = LocalDateTime.now().minusDays(1),
                source = TransactionSource.MANUAL,
                bankName = null,
                paymentMethod = null
            ),
            Transaction(
                id = 2L,
                amount = 300.0,
                type = TransactionType.DEBIT,
                merchantName = "Old Transaction",
                categoryId = 2L,
                transactionDate = LocalDateTime.now().minusDays(2),
                source = TransactionSource.SMS,
                bankName = null,
                paymentMethod = null
            )
        )

        whenever(mockTransactionRepository.getAllTransactionsFlow()).thenReturn(flowOf(transactionsWithUnknown))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertNotNull(state.paymentMethodBreakdown)
        assertNotNull(state.paymentMethodBreakdown!!.unknownTransactions)
        assertEquals(800.0, state.paymentMethodBreakdown!!.unknownTransactions!!.amount, 0.01)
        assertEquals(2, state.paymentMethodBreakdown!!.unknownTransactions!!.transactionCount)
        assertEquals("Unknown Payment Method", state.paymentMethodBreakdown!!.unknownTransactions!!.displayName)
    }

    // Test 14: Payment method breakdown - Calculates percentages correctly
    @Test
    fun `payment method breakdown calculates percentages correctly`() = runTest {
        val transactionsWithPaymentMethods = listOf(
            Transaction(
                id = 1L,
                amount = 500.0,
                type = TransactionType.DEBIT,
                merchantName = "Swiggy",
                categoryId = 1L,
                transactionDate = LocalDateTime.now().minusDays(1),
                source = TransactionSource.SMS,
                bankName = "HDFC",
                paymentMethod = "UPI"
            ),
            Transaction(
                id = 2L,
                amount = 500.0,
                type = TransactionType.DEBIT,
                merchantName = "Amazon",
                categoryId = 3L,
                transactionDate = LocalDateTime.now().minusDays(2),
                source = TransactionSource.SMS,
                bankName = "ICICI",
                paymentMethod = "CREDIT_CARD",
                cardLastFour = "1234"
            )
        )

        whenever(mockTransactionRepository.getAllTransactionsFlow()).thenReturn(flowOf(transactionsWithPaymentMethods))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertNotNull(state.paymentMethodBreakdown)

        // Total is 1000, each payment method is 500 = 50%
        val hdfcUpi = state.paymentMethodBreakdown!!.upiTransactions[0]
        assertEquals(0.5f, hdfcUpi.percentage, 0.01f)

        val iciciCard = state.paymentMethodBreakdown!!.creditCardTransactions[0]
        assertEquals(0.5f, iciciCard.percentage, 0.01f)
    }

    // Test 15: Payment method breakdown - Sorted by amount descending
    @Test
    fun `payment method breakdown is sorted by amount descending`() = runTest {
        val transactionsWithPaymentMethods = listOf(
            Transaction(
                id = 1L,
                amount = 200.0,
                type = TransactionType.DEBIT,
                merchantName = "Swiggy",
                categoryId = 1L,
                transactionDate = LocalDateTime.now().minusDays(1),
                source = TransactionSource.SMS,
                bankName = "HDFC",
                paymentMethod = "UPI"
            ),
            Transaction(
                id = 2L,
                amount = 500.0,
                type = TransactionType.DEBIT,
                merchantName = "Zomato",
                categoryId = 1L,
                transactionDate = LocalDateTime.now().minusDays(2),
                source = TransactionSource.SMS,
                bankName = "ICICI",
                paymentMethod = "UPI"
            ),
            Transaction(
                id = 3L,
                amount = 300.0,
                type = TransactionType.DEBIT,
                merchantName = "Uber",
                categoryId = 2L,
                transactionDate = LocalDateTime.now().minusDays(3),
                source = TransactionSource.SMS,
                bankName = "SBI",
                paymentMethod = "UPI"
            )
        )

        whenever(mockTransactionRepository.getAllTransactionsFlow()).thenReturn(flowOf(transactionsWithPaymentMethods))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertNotNull(state.paymentMethodBreakdown)

        // Check UPI transactions are sorted
        val upiTransactions = state.paymentMethodBreakdown!!.upiTransactions
        for (i in 0 until upiTransactions.size - 1) {
            assertTrue(upiTransactions[i].amount >= upiTransactions[i + 1].amount)
        }
    }

    // Test 16: Payment method breakdown - Totals are calculated correctly
    @Test
    fun `payment method breakdown calculates totals correctly`() = runTest {
        val transactionsWithPaymentMethods = listOf(
            Transaction(
                id = 1L,
                amount = 500.0,
                type = TransactionType.DEBIT,
                merchantName = "Swiggy",
                categoryId = 1L,
                transactionDate = LocalDateTime.now().minusDays(1),
                source = TransactionSource.SMS,
                bankName = "HDFC",
                paymentMethod = "UPI"
            ),
            Transaction(
                id = 2L,
                amount = 300.0,
                type = TransactionType.DEBIT,
                merchantName = "Zomato",
                categoryId = 1L,
                transactionDate = LocalDateTime.now().minusDays(2),
                source = TransactionSource.SMS,
                bankName = "ICICI",
                paymentMethod = "UPI"
            ),
            Transaction(
                id = 3L,
                amount = 1000.0,
                type = TransactionType.DEBIT,
                merchantName = "Amazon",
                categoryId = 3L,
                transactionDate = LocalDateTime.now().minusDays(3),
                source = TransactionSource.SMS,
                bankName = "HDFC",
                paymentMethod = "CREDIT_CARD",
                cardLastFour = "1234"
            ),
            Transaction(
                id = 4L,
                amount = 200.0,
                type = TransactionType.DEBIT,
                merchantName = "Cash",
                categoryId = 1L,
                transactionDate = LocalDateTime.now().minusDays(4),
                source = TransactionSource.MANUAL,
                bankName = null,
                paymentMethod = null
            )
        )

        whenever(mockTransactionRepository.getAllTransactionsFlow()).thenReturn(flowOf(transactionsWithPaymentMethods))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertNotNull(state.paymentMethodBreakdown)
        assertEquals(800.0, state.paymentMethodBreakdown!!.totalUpiSpend, 0.01)
        assertEquals(1000.0, state.paymentMethodBreakdown!!.totalCreditCardSpend, 0.01)
        assertEquals(200.0, state.paymentMethodBreakdown!!.totalUnknownSpend, 0.01)
    }

    // Test 17: Period navigation - previous month
    @Test
    fun `navigating to previous month updates selectedDate`() = runTest {
        val startDate = LocalDate.of(2024, 12, 15)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Manually set date to Dec 2024
        viewModel.updateSelectedDate(startDate)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.navigateToPreviousPeriod()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals(YearMonth.of(2024, 11), YearMonth.from(state.selectedDate))
    }

    // Test 18: Period navigation - cannot navigate to future
    @Test
    fun `cannot navigate beyond current period`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val stateBefore = viewModel.uiState.first()

        // Try to navigate forward when already at current month
        viewModel.navigateToNextPeriod()
        testDispatcher.scheduler.advanceUntilIdle()

        val stateAfter = viewModel.uiState.first()

        // Should remain at current month
        assertEquals(stateBefore.selectedDate, stateAfter.selectedDate)
        assertFalse(stateAfter.canNavigateForward)
    }

    // Test 19: Period label formatting
    @Test
    fun `period label formats correctly for month`() = runTest {
        whenever(mockTransactionRepository.getAllTransactionsFlow())
            .thenReturn(flowOf(emptyList()))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        // Should be like "December 2024"
        assertTrue(state.periodLabel.contains("202"))  // Contains year
        assertTrue(state.periodLabel.isNotEmpty())
    }

    // Test 20: Switching period type preserves date
    @Test
    fun `switching from month to year preserves same timeframe`() = runTest {
        val testDate = LocalDate.of(2024, 6, 15)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Set to June 2024
        viewModel.updateSelectedDate(testDate)
        testDispatcher.scheduler.advanceUntilIdle()

        // Switch to YEAR view
        viewModel.setPeriod(AnalyticsPeriod.YEAR)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        // Should still be viewing 2024 (same year)
        assertEquals(2024, state.selectedDate.year)
        assertEquals(AnalyticsPeriod.YEAR, state.selectedPeriod)
    }

    // Test 21: Navigate to current period resets to today
    @Test
    fun `navigateToCurrentPeriod jumps to today`() = runTest {
        val oldDate = LocalDate.of(2023, 1, 1)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateSelectedDate(oldDate)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.navigateToCurrentPeriod()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        // Should be current month/year
        val currentMonth = YearMonth.now()
        val stateMonth = YearMonth.from(state.selectedDate)
        assertEquals(currentMonth, stateMonth)
    }

    // Test 22: Swipe navigation - swipe left navigates to next period when allowed
    @Test
    fun `swipe left navigates to next period when allowed`() = runTest {
        val pastDate = LocalDate.of(2024, 6, 15)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Set to June 2024 (past date, so forward navigation is allowed)
        viewModel.updateSelectedDate(pastDate)
        testDispatcher.scheduler.advanceUntilIdle()

        val stateBefore = viewModel.uiState.first()

        // Simulate swipe left (should navigate forward if allowed)
        if (stateBefore.canNavigateForward) {
            viewModel.navigateToNextPeriod()
            testDispatcher.scheduler.advanceUntilIdle()

            val stateAfter = viewModel.uiState.first()
            assertNotEquals(stateBefore.selectedDate, stateAfter.selectedDate)
            assertEquals(YearMonth.of(2024, 7), YearMonth.from(stateAfter.selectedDate))
        }
    }

    // Test 23: Swipe navigation - swipe right navigates to previous period
    @Test
    fun `swipe right navigates to previous period`() = runTest {
        val testDate = LocalDate.of(2024, 6, 15)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Set to June 2024
        viewModel.updateSelectedDate(testDate)
        testDispatcher.scheduler.advanceUntilIdle()

        val monthBefore = YearMonth.from(viewModel.uiState.first().selectedDate)

        viewModel.navigateToPreviousPeriod()
        testDispatcher.scheduler.advanceUntilIdle()

        val monthAfter = YearMonth.from(viewModel.uiState.first().selectedDate)
        assertEquals(monthBefore.minusMonths(1), monthAfter)
        assertEquals(YearMonth.of(2024, 5), monthAfter)
    }

    // Test 24: Swipe navigation - swipe left at current period does not navigate to future
    @Test
    fun `swipe left at current period does not navigate to future`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Already at current month
        val stateBefore = viewModel.uiState.first()
        assertFalse(stateBefore.canNavigateForward)

        viewModel.navigateToNextPeriod()
        testDispatcher.scheduler.advanceUntilIdle()

        val stateAfter = viewModel.uiState.first()
        assertEquals(stateBefore.selectedDate, stateAfter.selectedDate)
    }

    // Test 25: Jump shortcuts - jumpToLastMonth navigates to previous month
    @Test
    fun `jumpToLastMonth navigates to previous month`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val expectedMonth = YearMonth.now().minusMonths(1)
        viewModel.jumpToLastMonth()
        testDispatcher.scheduler.advanceUntilIdle()

        val actualMonth = YearMonth.from(viewModel.uiState.first().selectedDate)
        assertEquals(expectedMonth, actualMonth)
    }

    // Test 26: Jump shortcuts - jumpTo3MonthsAgo navigates 3 months back
    @Test
    fun `jumpTo3MonthsAgo navigates 3 months back`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val expectedMonth = YearMonth.now().minusMonths(3)
        viewModel.jumpTo3MonthsAgo()
        testDispatcher.scheduler.advanceUntilIdle()

        val actualMonth = YearMonth.from(viewModel.uiState.first().selectedDate)
        assertEquals(expectedMonth, actualMonth)
    }

    // Test 27: Trend analysis - loadTrendData for 6 months calculates correctly
    @Test
    fun `loadTrendData for 6 months calculates correctly`() = runTest {
        // Setup mock data for 6 months
        val months = (0..5).map { YearMonth.now().minusMonths(it.toLong()) }
        val testTransactionsForMonths = mutableListOf<List<Transaction>>()

        months.forEach { month ->
            val amount = 1000.0 * (months.indexOf(month) + 1)
            val monthTransactions = listOf(
                Transaction(
                    id = months.indexOf(month).toLong(),
                    amount = amount,
                    type = TransactionType.DEBIT,
                    merchantName = "Test Merchant",
                    categoryId = 1L,
                    transactionDate = month.atDay(1).atStartOfDay(),
                    source = TransactionSource.MANUAL
                )
            )
            testTransactionsForMonths.add(monthTransactions)
            whenever(mockTransactionRepository.getTransactionsForMonth(month))
                .thenReturn(monthTransactions)
        }

        viewModel = createViewModel()
        viewModel.loadTrendData(periodCount = 6)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertNotNull(state.spendingTrend)
        assertEquals(6, state.spendingTrend!!.periods.size)
    }

    // Test 28: Trend analysis - trend direction is INCREASING when spending goes up
    @Test
    fun `trend direction is INCREASING when spending goes up`() = runTest {
        // Current month: 1500, Previous month: 1000
        val currentMonth = YearMonth.now()
        val previousMonth = currentMonth.minusMonths(1)

        val currentMonthTransactions = listOf(
            Transaction(
                id = 1L,
                amount = 1500.0,
                type = TransactionType.DEBIT,
                merchantName = "Test Merchant",
                categoryId = 1L,
                transactionDate = currentMonth.atDay(1).atStartOfDay(),
                source = TransactionSource.MANUAL
            )
        )

        val previousMonthTransactions = listOf(
            Transaction(
                id = 2L,
                amount = 1000.0,
                type = TransactionType.DEBIT,
                merchantName = "Test Merchant",
                categoryId = 1L,
                transactionDate = previousMonth.atDay(1).atStartOfDay(),
                source = TransactionSource.MANUAL
            )
        )

        whenever(mockTransactionRepository.getTransactionsForMonth(currentMonth))
            .thenReturn(currentMonthTransactions)
        whenever(mockTransactionRepository.getTransactionsForMonth(previousMonth))
            .thenReturn(previousMonthTransactions)

        viewModel = createViewModel()
        viewModel.loadTrendData(periodCount = 2)
        testDispatcher.scheduler.advanceUntilIdle()

        val trend = viewModel.uiState.first().spendingTrend
        assertNotNull(trend)
        assertEquals(com.fino.app.domain.model.TrendDirection.INCREASING, trend!!.trendDirection)
    }

    // Test 29: Trend analysis - average spending is calculated correctly across periods
    @Test
    fun `average spending is calculated correctly across periods`() = runTest {
        val testData = listOf(1000.0, 1200.0, 800.0, 1400.0)
        val expectedAverage = (1000.0 + 1200.0 + 800.0 + 1400.0) / 4

        // Mock repository to return test data
        testData.forEachIndexed { index, amount ->
            val month = YearMonth.now().minusMonths(index.toLong())
            val monthTransactions = listOf(
                Transaction(
                    id = index.toLong(),
                    amount = amount,
                    type = TransactionType.DEBIT,
                    merchantName = "Test Merchant",
                    categoryId = 1L,
                    transactionDate = month.atDay(1).atStartOfDay(),
                    source = TransactionSource.MANUAL
                )
            )
            whenever(mockTransactionRepository.getTransactionsForMonth(month))
                .thenReturn(monthTransactions)
        }

        viewModel = createViewModel()
        viewModel.loadTrendData(periodCount = 4)
        testDispatcher.scheduler.advanceUntilIdle()

        val trend = viewModel.uiState.first().spendingTrend
        assertNotNull(trend)
        assertEquals(expectedAverage, trend!!.averageSpending, 0.01)
    }

    // Test 30: Trend analysis - trend direction is DECREASING when spending goes down
    @Test
    fun `trend direction is DECREASING when spending goes down`() = runTest {
        // Current month: 500, Previous month: 1000 (50% decrease)
        val currentMonth = YearMonth.now()
        val previousMonth = currentMonth.minusMonths(1)

        val currentMonthTransactions = listOf(
            Transaction(
                id = 1L,
                amount = 500.0,
                type = TransactionType.DEBIT,
                merchantName = "Test Merchant",
                categoryId = 1L,
                transactionDate = currentMonth.atDay(1).atStartOfDay(),
                source = TransactionSource.MANUAL
            )
        )

        val previousMonthTransactions = listOf(
            Transaction(
                id = 2L,
                amount = 1000.0,
                type = TransactionType.DEBIT,
                merchantName = "Test Merchant",
                categoryId = 1L,
                transactionDate = previousMonth.atDay(1).atStartOfDay(),
                source = TransactionSource.MANUAL
            )
        )

        whenever(mockTransactionRepository.getTransactionsForMonth(currentMonth))
            .thenReturn(currentMonthTransactions)
        whenever(mockTransactionRepository.getTransactionsForMonth(previousMonth))
            .thenReturn(previousMonthTransactions)

        viewModel = createViewModel()
        viewModel.loadTrendData(periodCount = 2)
        testDispatcher.scheduler.advanceUntilIdle()

        val trend = viewModel.uiState.first().spendingTrend
        assertNotNull(trend)
        assertEquals(com.fino.app.domain.model.TrendDirection.DECREASING, trend!!.trendDirection)
    }

    // Test 31: Trend analysis - trend direction is STABLE when change is small
    @Test
    fun `trend direction is STABLE when change is small`() = runTest {
        // Current month: 1030, Previous month: 1000 (3% increase - within STABLE range)
        val currentMonth = YearMonth.now()
        val previousMonth = currentMonth.minusMonths(1)

        val currentMonthTransactions = listOf(
            Transaction(
                id = 1L,
                amount = 1030.0,
                type = TransactionType.DEBIT,
                merchantName = "Test Merchant",
                categoryId = 1L,
                transactionDate = currentMonth.atDay(1).atStartOfDay(),
                source = TransactionSource.MANUAL
            )
        )

        val previousMonthTransactions = listOf(
            Transaction(
                id = 2L,
                amount = 1000.0,
                type = TransactionType.DEBIT,
                merchantName = "Test Merchant",
                categoryId = 1L,
                transactionDate = previousMonth.atDay(1).atStartOfDay(),
                source = TransactionSource.MANUAL
            )
        )

        whenever(mockTransactionRepository.getTransactionsForMonth(currentMonth))
            .thenReturn(currentMonthTransactions)
        whenever(mockTransactionRepository.getTransactionsForMonth(previousMonth))
            .thenReturn(previousMonthTransactions)

        viewModel = createViewModel()
        viewModel.loadTrendData(periodCount = 2)
        testDispatcher.scheduler.advanceUntilIdle()

        val trend = viewModel.uiState.first().spendingTrend
        assertNotNull(trend)
        assertEquals(com.fino.app.domain.model.TrendDirection.STABLE, trend!!.trendDirection)
    }

    // Test 32: Trend analysis - handles months with no transactions
    @Test
    fun `trend analysis handles months with no transactions`() = runTest {
        val months = (0..2).map { YearMonth.now().minusMonths(it.toLong()) }

        // Month 0 (current): 1000, Month 1: 0, Month 2: 500
        val testData = listOf(1000.0, 0.0, 500.0)

        months.forEachIndexed { index, month ->
            val amount = testData[index]
            val monthTransactions = if (amount > 0) {
                listOf(
                    Transaction(
                        id = index.toLong(),
                        amount = amount,
                        type = TransactionType.DEBIT,
                        merchantName = "Test Merchant",
                        categoryId = 1L,
                        transactionDate = month.atDay(1).atStartOfDay(),
                        source = TransactionSource.MANUAL
                    )
                )
            } else {
                emptyList()
            }
            whenever(mockTransactionRepository.getTransactionsForMonth(month))
                .thenReturn(monthTransactions)
        }

        viewModel = createViewModel()
        viewModel.loadTrendData(periodCount = 3)
        testDispatcher.scheduler.advanceUntilIdle()

        val trend = viewModel.uiState.first().spendingTrend
        assertNotNull(trend)
        assertEquals(3, trend!!.periods.size)

        // Check that month 1 has 0 spending
        val monthWithNoTransactions = trend.periods.find { it.yearMonth == months[1] }
        assertNotNull(monthWithNoTransactions)
        assertEquals(0.0, monthWithNoTransactions!!.totalSpent, 0.01)
        assertEquals(0, monthWithNoTransactions.transactionCount)
    }

    // Budget Tracking Tests - Phase 2.3

    // Test 33: Budget progress calculation for current period
    @Test
    fun `budget progress calculates correctly for current period`() = runTest {
        val currentMonth = YearMonth.now()

        // Food category: budget 1000, spent 800 (80%)
        val foodBudget = Budget(
            id = 1L,
            categoryId = 1L,
            monthlyLimit = 1000.0,
            month = currentMonth
        )

        // Transport category: budget 500, spent 200 (40%)
        val transportBudget = Budget(
            id = 2L,
            categoryId = 2L,
            monthlyLimit = 500.0,
            month = currentMonth
        )

        runBlocking {
            whenever(mockBudgetRepository.getBudgetsForMonth(currentMonth))
                .thenReturn(listOf(foodBudget, transportBudget))
        }

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(2, state.budgetProgress.size)

        // Check Food category
        val foodProgress = state.budgetProgress.find { it.budget.categoryId == 1L }
        assertNotNull(foodProgress)
        assertEquals("Food", foodProgress!!.categoryName)
        assertEquals(1000.0, foodProgress.budget.monthlyLimit, 0.01)
        assertEquals(800.0, foodProgress.spent, 0.01)
        assertEquals(200.0, foodProgress.remaining, 0.01)
        assertEquals(0.8f, foodProgress.percentage, 0.01f)
        assertEquals(BudgetStatus.APPROACHING_LIMIT, foodProgress.status)

        // Check Transport category
        val transportProgress = state.budgetProgress.find { it.budget.categoryId == 2L }
        assertNotNull(transportProgress)
        assertEquals("Transport", transportProgress!!.categoryName)
        assertEquals(500.0, transportProgress.budget.monthlyLimit, 0.01)
        assertEquals(200.0, transportProgress.spent, 0.01)
        assertEquals(300.0, transportProgress.remaining, 0.01)
        assertEquals(0.4f, transportProgress.percentage, 0.01f)
        assertEquals(BudgetStatus.UNDER_BUDGET, transportProgress.status)
    }

    // Test 34: Budget alert at 75% threshold
    @Test
    fun `budget alert shows when reaching 75 percent threshold`() = runTest {
        val currentMonth = YearMonth.now()

        // Food category: budget 1000, spent 750 (75% - exactly at threshold)
        val foodBudget = Budget(
            id = 1L,
            categoryId = 1L,
            monthlyLimit = 1000.0,
            month = currentMonth,
            alertAt75 = true
        )

        runBlocking {
            whenever(mockBudgetRepository.getBudgetsForMonth(currentMonth))
                .thenReturn(listOf(foodBudget))
        }

        // Create transactions for 75% spending
        val transactions75Percent = listOf(
            Transaction(
                id = 1L,
                amount = 750.0,
                type = TransactionType.DEBIT,
                merchantName = "Test",
                categoryId = 1L,
                transactionDate = LocalDateTime.now(),
                source = TransactionSource.MANUAL
            )
        )

        whenever(mockTransactionRepository.getAllTransactionsFlow()).thenReturn(flowOf(transactions75Percent))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        val foodProgress = state.budgetProgress.find { it.budget.categoryId == 1L }
        assertNotNull(foodProgress)
        assertEquals(0.75f, foodProgress!!.percentage, 0.01f)
        assertEquals(BudgetStatus.APPROACHING_LIMIT, foodProgress.status)
        assertTrue(state.showBudgetAlert)
        assertEquals(1L, state.budgetAlertForCategory)
    }

    // Test 35: Budget alert at 100% threshold
    @Test
    fun `budget alert shows when reaching 100 percent threshold`() = runTest {
        val currentMonth = YearMonth.now()

        // Food category: budget 1000, spent 1000 (100%)
        val foodBudget = Budget(
            id = 1L,
            categoryId = 1L,
            monthlyLimit = 1000.0,
            month = currentMonth,
            alertAt100 = true
        )

        runBlocking {
            whenever(mockBudgetRepository.getBudgetsForMonth(currentMonth))
                .thenReturn(listOf(foodBudget))
        }

        // Create transactions for 100% spending
        val transactions100Percent = listOf(
            Transaction(
                id = 1L,
                amount = 1000.0,
                type = TransactionType.DEBIT,
                merchantName = "Test",
                categoryId = 1L,
                transactionDate = LocalDateTime.now(),
                source = TransactionSource.MANUAL
            )
        )

        whenever(mockTransactionRepository.getAllTransactionsFlow()).thenReturn(flowOf(transactions100Percent))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        val foodProgress = state.budgetProgress.find { it.budget.categoryId == 1L }
        assertNotNull(foodProgress)
        assertEquals(1.0f, foodProgress!!.percentage, 0.01f)
        assertEquals(BudgetStatus.OVER_BUDGET, foodProgress.status)
        assertTrue(state.showBudgetAlert)
        assertEquals(1L, state.budgetAlertForCategory)
    }

    // Test 36: Multiple category budgets
    @Test
    fun `handles multiple category budgets correctly`() = runTest {
        val currentMonth = YearMonth.now()

        val budgets = listOf(
            Budget(id = 1L, categoryId = 1L, monthlyLimit = 1000.0, month = currentMonth), // Food
            Budget(id = 2L, categoryId = 2L, monthlyLimit = 500.0, month = currentMonth),  // Transport
            Budget(id = 3L, categoryId = 3L, monthlyLimit = 2000.0, month = currentMonth)  // Shopping
        )

        runBlocking {
            whenever(mockBudgetRepository.getBudgetsForMonth(currentMonth))
                .thenReturn(budgets)
        }

        // testTransactions: Food=800, Transport=200, Shopping=1000
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(3, state.budgetProgress.size)

        // Verify all three budgets are tracked
        val foodProgress = state.budgetProgress.find { it.budget.categoryId == 1L }
        val transportProgress = state.budgetProgress.find { it.budget.categoryId == 2L }
        val shoppingProgress = state.budgetProgress.find { it.budget.categoryId == 3L }

        assertNotNull(foodProgress)
        assertNotNull(transportProgress)
        assertNotNull(shoppingProgress)

        // Food: 800/1000 = 80%
        assertEquals(BudgetStatus.APPROACHING_LIMIT, foodProgress!!.status)

        // Transport: 200/500 = 40%
        assertEquals(BudgetStatus.UNDER_BUDGET, transportProgress!!.status)

        // Shopping: 1000/2000 = 50%
        assertEquals(BudgetStatus.UNDER_BUDGET, shoppingProgress!!.status)
    }
}
