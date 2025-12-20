package com.fino.app.presentation.viewmodel

import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.Category
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionSource
import com.fino.app.domain.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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

        whenever(mockTransactionRepository.getAllTransactionsFlow()).thenReturn(flowOf(testTransactions))
        whenever(mockCategoryRepository.getAllActive()).thenReturn(flowOf(testCategories))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): AnalyticsViewModel {
        return AnalyticsViewModel(
            transactionRepository = mockTransactionRepository,
            categoryRepository = mockCategoryRepository
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
}
