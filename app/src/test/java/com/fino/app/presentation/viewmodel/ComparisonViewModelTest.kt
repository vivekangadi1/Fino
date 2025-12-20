package com.fino.app.presentation.viewmodel

import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.*
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class ComparisonViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockTransactionRepository: TransactionRepository
    private lateinit var mockCategoryRepository: CategoryRepository
    private lateinit var viewModel: ComparisonViewModel

    private val testCategories = listOf(
        Category(id = 1L, name = "Food", emoji = "🍔"),
        Category(id = 2L, name = "Transport", emoji = "🚗"),
        Category(id = 3L, name = "Entertainment", emoji = "🎬")
    )

    private val januaryTransactions = listOf(
        Transaction(
            id = 1L,
            amount = 50000.0,
            type = TransactionType.CREDIT,
            merchantName = "Salary",
            transactionDate = LocalDateTime.of(2024, 1, 1, 9, 0),
            source = TransactionSource.MANUAL
        ),
        Transaction(
            id = 2L,
            amount = 3000.0,
            type = TransactionType.DEBIT,
            merchantName = "Swiggy",
            categoryId = 1L,
            transactionDate = LocalDateTime.of(2024, 1, 15, 14, 30),
            source = TransactionSource.SMS
        ),
        Transaction(
            id = 3L,
            amount = 2000.0,
            type = TransactionType.DEBIT,
            merchantName = "Uber",
            categoryId = 2L,
            transactionDate = LocalDateTime.of(2024, 1, 20, 10, 15),
            source = TransactionSource.SMS
        )
    )

    private val februaryTransactions = listOf(
        Transaction(
            id = 4L,
            amount = 55000.0,
            type = TransactionType.CREDIT,
            merchantName = "Salary",
            transactionDate = LocalDateTime.of(2024, 2, 1, 9, 0),
            source = TransactionSource.MANUAL
        ),
        Transaction(
            id = 5L,
            amount = 4000.0,
            type = TransactionType.DEBIT,
            merchantName = "Zomato",
            categoryId = 1L,
            transactionDate = LocalDateTime.of(2024, 2, 10, 19, 0),
            source = TransactionSource.SMS
        ),
        Transaction(
            id = 6L,
            amount = 1500.0,
            type = TransactionType.DEBIT,
            merchantName = "Rapido",
            categoryId = 2L,
            transactionDate = LocalDateTime.of(2024, 2, 18, 16, 30),
            source = TransactionSource.SMS
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockTransactionRepository = mock()
        mockCategoryRepository = mock()

        whenever(mockCategoryRepository.getAllActive()).thenReturn(flowOf(testCategories))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        currentMonth: YearMonth = YearMonth.of(2024, 2),
        previousMonth: YearMonth = YearMonth.of(2024, 1)
    ): ComparisonViewModel {
        // Mock transactions for both periods
        whenever(mockTransactionRepository.getAllTransactionsFlow())
            .thenReturn(flowOf(januaryTransactions + februaryTransactions))

        val vm = ComparisonViewModel(
            transactionRepository = mockTransactionRepository,
            categoryRepository = mockCategoryRepository
        )

        // Set the months if different from default
        if (currentMonth != YearMonth.now() || previousMonth != YearMonth.now().minusMonths(1)) {
            vm.changeCurrentPeriod(currentMonth)
            vm.changePreviousPeriod(previousMonth)
        }

        return vm
    }

    // Test 37: Initial state is loading
    @Test
    fun `initial state is loading`() = runTest {
        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
        assertNull(state.comparison)
    }

    // Test 38: Load comparison data calculates correctly
    @Test
    fun `loadComparisonData calculates income change correctly`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertNotNull(state.comparison)

        // Feb income: 55000, Jan income: 50000 = +5000
        assertEquals(5000.0, state.comparison!!.incomeChange, 0.01)
    }

    // Test 39: Load comparison data calculates expenses change correctly
    @Test
    fun `loadComparisonData calculates expenses change correctly`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertNotNull(state.comparison)

        // Feb expenses: 5500 (4000 + 1500), Jan expenses: 5000 (3000 + 2000) = +500
        assertEquals(500.0, state.comparison!!.expensesChange, 0.01)
    }

    // Test 40: Category comparison identifies highest change
    @Test
    fun `category comparison identifies categories with highest changes`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertNotNull(state.comparison)

        val categoryComparisons = state.comparison!!.categoryComparisons
        assertTrue(categoryComparisons.isNotEmpty())

        // Food category should have a change (4000 in Feb vs 3000 in Jan = +1000)
        val foodComparison = categoryComparisons.find { it.categoryName == "Food" }
        assertNotNull(foodComparison)
        assertEquals(1000.0, foodComparison!!.change, 0.01)
    }

    // Test 41: Change current period updates comparison
    @Test
    fun `changeCurrentPeriod updates comparison data`() = runTest {
        viewModel = createViewModel(
            currentMonth = YearMonth.of(2024, 2),
            previousMonth = YearMonth.of(2024, 1)
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val stateBefore = viewModel.uiState.first()
        assertEquals(YearMonth.of(2024, 2), stateBefore.currentMonth)

        viewModel.changeCurrentPeriod(YearMonth.of(2024, 3))
        testDispatcher.scheduler.advanceUntilIdle()

        val stateAfter = viewModel.uiState.first()
        assertEquals(YearMonth.of(2024, 3), stateAfter.currentMonth)
    }

    // Test 42: Change previous period updates comparison
    @Test
    fun `changePreviousPeriod updates comparison data`() = runTest {
        viewModel = createViewModel(
            currentMonth = YearMonth.of(2024, 2),
            previousMonth = YearMonth.of(2024, 1)
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val stateBefore = viewModel.uiState.first()
        assertEquals(YearMonth.of(2024, 1), stateBefore.previousMonth)

        viewModel.changePreviousPeriod(YearMonth.of(2023, 12))
        testDispatcher.scheduler.advanceUntilIdle()

        val stateAfter = viewModel.uiState.first()
        assertEquals(YearMonth.of(2023, 12), stateAfter.previousMonth)
    }

    // Test 43: Comparison with empty previous period
    @Test
    fun `comparison handles empty previous period`() = runTest {
        // Mock only February transactions
        whenever(mockTransactionRepository.getAllTransactionsFlow())
            .thenReturn(flowOf(februaryTransactions))

        viewModel = createViewModel(
            currentMonth = YearMonth.of(2024, 2),
            previousMonth = YearMonth.of(2023, 12) // No transactions in this month
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertNotNull(state.comparison)

        // Previous period should have 0 income and expenses
        assertEquals(0.0, state.comparison!!.previousPeriod.totalIncome, 0.01)
        assertEquals(0.0, state.comparison!!.previousPeriod.totalExpenses, 0.01)
    }

    // Test 44: Period labels are formatted correctly
    @Test
    fun `period labels are formatted correctly`() = runTest {
        viewModel = createViewModel(
            currentMonth = YearMonth.of(2024, 2),
            previousMonth = YearMonth.of(2024, 1)
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals("Feb 2024", state.currentMonthLabel)
        assertEquals("Jan 2024", state.previousMonthLabel)
    }

    // Test 45: Net balance change calculation
    @Test
    fun `net balance change is calculated correctly`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertNotNull(state.comparison)

        // Feb net: 55000 - 5500 = 49500
        // Jan net: 50000 - 5000 = 45000
        // Change: 49500 - 45000 = 4500
        assertEquals(4500.0, state.comparison!!.netBalanceChange, 0.01)
    }

    // Test 46: Percentage changes are accurate
    @Test
    fun `percentage changes are calculated accurately`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertNotNull(state.comparison)

        // Income change: (55000 - 50000) / 50000 * 100 = 10%
        assertEquals(10.0f, state.comparison!!.incomeChangePercentage, 0.1f)

        // Expenses change: (5500 - 5000) / 5000 * 100 = 10%
        assertEquals(10.0f, state.comparison!!.expensesChangePercentage, 0.1f)
    }
}
